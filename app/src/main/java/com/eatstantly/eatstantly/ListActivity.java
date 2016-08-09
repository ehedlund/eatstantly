package com.eatstantly.eatstantly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Created by emilyhedlund on 8/8/16.
 */
public class ListActivity extends AppCompatActivity {
    // declare variables
    // Button mapButton;
    ArrayList<Restaurant> restaurants;
    ListView list;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // initialize variables
        restaurants = new ArrayList<Restaurant>();
        // mapButton = (Button) findViewById(R.id.mapButton);
        list = (ListView) findViewById(R.id.restaurant_list);

        // get JSON response
        String response;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            response = extras.getString("Reply");
            if (response != null) {
                try {
                    // parse response, create initial restaurant array
                    JSONArray allResults = new JSONArray(new JSONObject(response).getString("results"));
                    int length = allResults.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject result = allResults.getJSONObject(i);
                        Restaurant r = new Restaurant(result);
                        restaurants.add(r);
                    }

                    // get instagram place information
                    /* for (int i = 0; i < length; i++) {
                        String baseURL = "https://api.instagram.com/v1/locations/search?";
                        Restaurant r = restaurants.get(i);
                        baseURL += "lat=" + r.latitude + "&lng=" + r.longitude;
                        baseURL += "&access_token=" + ACCESS_TOKEN;
                    } */

                    loadListView();
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }

        /* // set up buttons
        mapButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent mapsIntent = new Intent(ListActivity.this, MapsActivity.class);
                        ListActivity.this.startActivity(mapsIntent);
                    }
                }
        ); */
    }

    public class Restaurant {
        String name;
        String address;
        String icon;
        String latitude;
        String longitude;

        public Restaurant(JSONObject js) {
            try {
                name = js.getString("name");
                address = js.getString("vicinity");
                icon = js.getString("icon");
                latitude = js.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = js.getJSONObject("geometry").getJSONObject("location").getString("lng");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadListView(){
        ArrayAdapter<Restaurant> adapter = new ArrayAdapter<Restaurant>(this, R.layout.list_item, restaurants) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                // ImageView restaurantIcon = (ImageView)convertView.findViewById(R.id.restaurant_icon);
                // appIcon.setImageDrawable(restaurants.get(position).icon);

                TextView restaurantAddress = (TextView) convertView.findViewById(R.id.restaurant_address);
                restaurantAddress.setText(restaurants.get(position).address);

                TextView restaurantName = (TextView) convertView.findViewById(R.id.restaurant_name);
                restaurantName.setText(restaurants.get(position).name);

                return convertView;
            }
        };
        list.setAdapter(adapter);
    }
}