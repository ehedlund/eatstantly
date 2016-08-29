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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
    private ArrayList<Restaurant> restaurants;
    private ListView list;
    private Toolbar myToolbar;

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
                    // TODO: better name matching process?
                    String locID = null;
                    JSONArray placeResults = new JSONArray(new JSONObject(locationResponse).getString("data"));
                    int jsonLength = placeResults.length();
                    for (int j = 0; j < jsonLength; j++) {
                        JSONObject result = placeResults.getJSONObject(j);
                        String instaName = result.getString("name");
                        // strict match
                        if (instaName.equals(r.name)) {
                            // found location id
                            locID = result.getString("id");
                            break;
                        }
                        // near match
                        else if (instaName.contains(r.name) || r.name.contains(instaName)) {
                            // found location id
                            locID = result.getString("id");
                            break;
                        }
                    }

                    // place doesn't exist on instagram
                    if (locID == null) {
                        restaurants.remove(r);
                        length--;
                    }
                    // place exists on instagram
                    else {
                        baseURL = "https://api.instagram.com/v1/locations/";
                        baseURL += locID + "/media/recent?access_token=" + token;
                        String photoResponse;
                        try {
                            photoResponse = new MyAsyncTask().execute(baseURL).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            photoResponse = "error";
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            photoResponse = "error";
                        }
                        JSONArray photoResults = new JSONArray(new JSONObject(photoResponse).getString("data"));
                        // TODO: sort photos before setting icon!!
                        int numPhotos = photoResults.length();
                        switch (numPhotos) {
                            case 0:
                                r.icon_1 = defaultIcon;
                                r.icon_2 = defaultIcon;
                                r.icon_3 = defaultIcon;
                                break;
                            case 1:
                                r.icon_1 = photoResults.getJSONObject(0).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                r.icon_2 = defaultIcon;
                                r.icon_3 = defaultIcon;
                                break;
                            case 2:
                                r.icon_1 = photoResults.getJSONObject(0).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                r.icon_2 = photoResults.getJSONObject(1).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                r.icon_3 = defaultIcon;
                                break;
                            default:
                                r.icon_1 = photoResults.getJSONObject(0).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                r.icon_2 = photoResults.getJSONObject(1).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                r.icon_3 = photoResults.getJSONObject(2).getJSONObject("images").getJSONObject("low_resolution").getString("url");
                                break;
                        }
                        i++;
                    }
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        // TODO: sort restaurants in list view
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
                new GetLogo(restaurantIcon).execute(restaurants.get(position).icon_1);

                ImageView restaurantIcon2 = (ImageView)convertView.findViewById(R.id.restaurant_icon_2);
                new GetLogo(restaurantIcon2).execute(restaurants.get(position).icon_2);

                ImageView restaurantIcon3 = (ImageView)convertView.findViewById(R.id.restaurant_icon_3);
                new GetLogo(restaurantIcon3).execute(restaurants.get(position).icon_3);

                TextView restaurantAddress = (TextView) convertView.findViewById(R.id.restaurant_address);
                restaurantAddress.setText(restaurants.get(position).address);

                TextView restaurantName = (TextView) convertView.findViewById(R.id.restaurant_name);
                restaurantName.setText(restaurants.get(position).name);

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
                ListActivity.this.startActivity(mapsIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}