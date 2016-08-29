package com.eatstantly.eatstantly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Toolbar myToolbar;
    private ArrayList<Restaurant> restaurants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // get restaurants
        restaurants = getIntent().getParcelableArrayListExtra("Restaurants");

        // show toolbar
        myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        // obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final int length = restaurants.size();
        final Bitmap[] icons = new Bitmap[length];

        // get icons
        Thread thread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < length; i++) {
                    Restaurant r = restaurants.get(i);
                    Bitmap bm = getBitmapFromURL(r.icon_1_small);
                    icons[i] = bm;
                }
            }
        };
        try {
            thread.start();
            thread.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        // create markers
        Double totalLat = 0.0;
        Double totalLong = 0.0;
        for (int i = 0; i < length; i++) {
            Restaurant r = restaurants.get(i);

            Double lat = Double.parseDouble(r.latitude);
            Double lng = Double.parseDouble(r.longitude);

            totalLat += lat;
            totalLong += lng;

            LatLng currentLL = new LatLng(lat, lng);

            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(icons[i]))
                    .position(currentLL));
        }

        // move camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(totalLat/length, totalLong/length), 16));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.display_menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.listView:
                item.setChecked(true);
                Intent listIntent = new Intent(MapsActivity.this, ListActivity.class);
                listIntent.putParcelableArrayListExtra("Restaurants", restaurants);
                MapsActivity.this.startActivity(listIntent);
                return true;

            case R.id.mapView:
                // do nothing
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}