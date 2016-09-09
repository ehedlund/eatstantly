package com.eatstantly.eatstantly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Created by emilyhedlund on 8/8/16.
 */
public class ListActivity extends AppCompatActivity {
    // declare variables
    private static final String defaultIcon = "https://2.bp.blogspot.com/-7_0YNV5xoAY/V8OOvlW_mZI/AAAAAAAAGPQ/NP3JlgApad0YhaCC0djfG3FhD3Wh1mEsACLcB/s1600/default_icon.png";
    private static final String defaultIconSmall = "https://2.bp.blogspot.com/-Jh-79w7LRG8/V8SJfjk2H3I/AAAAAAAAGPg/Nf2_6N7-hgQIYjQU3E95MZyBYQA3qe11QCLcB/s1600/default_icon_small.png";
    private static final double SIMILARITY_THRESHOLD = 0.8;
    private static final double SECONDS_IN_YEAR = 31536000;
    private static final String TAG = ListActivity.class.getSimpleName();
    private ArrayList<Restaurant> restaurants;
    private ListView list;
    private Toolbar myToolbar;
    private String selectedLocation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // get access token
        SharedPreferences sp = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String token = sp.getString("token", "error");

        // initialize variables
        list = (ListView) findViewById(R.id.restaurant_list);
        myToolbar = (Toolbar) findViewById(R.id.myToolbar);

        // show toolbar
        setSupportActionBar(myToolbar);

        // get intent extras
        Bundle extras = getIntent().getExtras();
        String response = extras.getString("Reply");
        restaurants = extras.getParcelableArrayList("Restaurants");
        selectedLocation = extras.getString("LatLng");


        // coming from SearchActivity
        if (restaurants == null && response != null) {
            try {
                // parse response, create restaurant array
                restaurants = new ArrayList<Restaurant>();
                JSONArray googleResults = new JSONArray(new JSONObject(response).getString("results"));
                int length = googleResults.length();


                // create Restaurant objects
                for (int i = 0; i < length; i++) {
                    JSONObject result = googleResults.getJSONObject(i);
                    Restaurant r = new Restaurant(result);
                    restaurants.add(r);
                }

                // get possible instagram locations for each google result
                int i = 0;
                while (i < length) {
                    // get location id
                    String baseURL = "https://api.instagram.com/v1/locations/search?";
                    
                    Restaurant r = restaurants.get(i);
                    baseURL += "lat=" + r.latitude + "&lng=" + r.longitude;
                    baseURL += "&access_token=" + token;
                    String locationResponse = null;
                    try {
                        locationResponse = new MyAsyncTask().execute(baseURL).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    // match to correct location
                    JSONArray placeResults = new JSONArray(new JSONObject(locationResponse).getString("data"));
                    int jsonLength = placeResults.length();

                    // find closest match
                    JaroWinklerDistance distance = new JaroWinklerDistance();
                    double max = 0.0;
                    String maxID = null;

                    for (int j = 0; j < jsonLength; j++) {
                        JSONObject result = placeResults.getJSONObject(j);
                        String instaName = result.getString("name");

                        double jwd = distance.apply(r.name, instaName);
                        if (jwd > max) {
                            max = jwd;
                            maxID = result.getString("id");
                        }
                    }

                    // check if match is good
                    if (max > SIMILARITY_THRESHOLD) {
                        r.locID = maxID;
                    }

                    // place doesn't exist on instagram
                    if (r.locID == null) {
                        restaurants.remove(r);
                        length--;
                    }
                    // place exists on instagram
                    else {
                        baseURL = "https://api.instagram.com/v1/locations/";
                        baseURL += r.locID + "/media/recent?access_token=" + token;
                        String photoResponse;
                        try {
                            photoResponse = new MyAsyncTask().execute(baseURL).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            photoResponse = null;
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            photoResponse = null;
                        }

                        if (photoResponse == null) {
                            restaurants.remove(r);
                            length--;
                        }
                        else {
                            JSONArray photoResults = new JSONArray(new JSONObject(photoResponse).getString("data"));
                            ArrayList<JSONObject> photoResultsArray = new ArrayList<JSONObject>();
                            for (int j = 0; j < photoResults.length(); j++) {
                                photoResultsArray.add(photoResults.getJSONObject(j));
                            }

                            r.setScore(photoResults);

                            Collections.sort(photoResultsArray, new Comparator<JSONObject>() {
                                @Override
                                public int compare(JSONObject p1, JSONObject p2) {
                                    Double p1Score = new Double(getPhotoScore(p1));
                                    Double p2Score = new Double(getPhotoScore(p2));
                                    Log.e(TAG, p1Score.toString() + " vs " + p2Score.toString());
                                    return -p1Score.compareTo(p2Score);
                                }
                            });

                            int numPhotos = photoResultsArray.size();
                            switch (numPhotos) {
                                case 0:
                                    r.icon_1 = defaultIcon;
                                    r.icon_2 = defaultIcon;
                                    r.icon_3 = defaultIcon;
                                    r.icon_1_small = defaultIconSmall;
                                    break;
                                case 1:
                                    r.icon_1 = photoResultsArray.get(0).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                    r.icon_2 = defaultIcon;
                                    r.icon_3 = defaultIcon;
                                    r.icon_1_small = photoResultsArray.get(0).getJSONObject("images").getJSONObject("thumbnail").getString("url");
                                    break;
                                case 2:
                                    r.icon_1 = photoResultsArray.get(0).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                    r.icon_2 = photoResultsArray.get(1).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                    r.icon_3 = defaultIcon;
                                    r.icon_1_small = photoResultsArray.get(0).getJSONObject("images").getJSONObject("thumbnail").getString("url");
                                    break;
                                default:
                                    r.icon_1 = photoResultsArray.get(0).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                    r.icon_2 = photoResultsArray.get(1).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                    r.icon_3 = photoResultsArray.get(2).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                    r.icon_1_small = photoResultsArray.get(0).getJSONObject("images").getJSONObject("thumbnail").getString("url");
                                    break;
                            }
                            i++;
                        }
                    }
                }

            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        // TODO: sort restaurants in list view
        orderRestaurants();
        loadListView();
    }

    private class MyAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String response;
            String queryURL = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // create connection
                URL url = new URL(queryURL);
                urlConnection = (HttpURLConnection) url.openConnection();

                // get response
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    return null;
                }
                response = buffer.toString();
                return response;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String output) {
            super.onPostExecute(output);
        }
    }

    private void loadListView() {
        ArrayAdapter<Restaurant> adapter = new ArrayAdapter<Restaurant>(this, R.layout.list_item, restaurants) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                Restaurant r = restaurants.get(position);
                ImageView restaurantIcon = (ImageView)convertView.findViewById(R.id.restaurant_icon);
                new GetLogo(restaurantIcon).execute(r.icon_1);

                ImageView restaurantIcon2 = (ImageView)convertView.findViewById(R.id.restaurant_icon_2);
                new GetLogo(restaurantIcon2).execute(r.icon_2);

                ImageView restaurantIcon3 = (ImageView)convertView.findViewById(R.id.restaurant_icon_3);
                new GetLogo(restaurantIcon3).execute(r.icon_3);

                TextView restaurantAddress = (TextView) convertView.findViewById(R.id.restaurant_address);
                restaurantAddress.setText(r.address);

                TextView restaurantName = (TextView) convertView.findViewById(R.id.restaurant_name);
                restaurantName.setText(r.name);

                float rating = 0.0f;
                if (r.rating != null) {
                    rating = Float.parseFloat(r.rating);
                }

                RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.rating_bar);
                ratingBar.setRating(rating);

                String priceString = null;
                if (r.price != null) {
                    switch (Integer.parseInt(r.price)) {
                        case 0:  priceString = "free!";
                            break;
                        case 1:  priceString = "$";
                            break;
                        case 2:  priceString = "$$";
                            break;
                        case 3:  priceString = "$$$";
                            break;
                        case 4:  priceString = "$$$$";
                            break;
                    }
                }
                else {
                    priceString = "unknown";
                }

                TextView priceLabel = (TextView) convertView.findViewById(R.id.price_label);
                priceLabel.setText("Price: " + priceString);

                return convertView;
            }
        };
        list.setAdapter(adapter);
    }

    private class GetLogo extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public GetLogo(ImageView bmImage) { this.bmImage = bmImage; }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bm = null;
            try {
                InputStream in = new URL(url).openStream();
                bm = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bm;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.display_menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.listView:
                // do nothing
                return true;

            case R.id.mapView:
                item.setChecked(true);
                Intent mapsIntent = new Intent(ListActivity.this, MapsActivity.class);
                mapsIntent.putParcelableArrayListExtra("Restaurants", restaurants);
                mapsIntent.putExtra("LatLng", selectedLocation);
                ListActivity.this.startActivity(mapsIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void orderRestaurants() {
        Collections.sort(restaurants, new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                Double r1Score = new Double(r1.score);
                Double r2Score = new Double(r2.score);
                return -r1Score.compareTo(r2Score);
            }
        });
    }

    private double getPhotoScore(JSONObject p) {
        long unixTime = System.currentTimeMillis() / 1000L;
        try {
            double timePosted = p.getDouble("created_time");
            int likes = p.getJSONObject("likes").getInt("count");
            double exponential = (timePosted - unixTime) / SECONDS_IN_YEAR;
            double exponential_function = Math.max(.1, Math.pow(10, exponential));
            return exponential_function * (likes + 1);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onBackPressed() {
        Intent searchIntent = new Intent(ListActivity.this, SearchActivity.class);
        ListActivity.this.startActivity(searchIntent);
    }
}