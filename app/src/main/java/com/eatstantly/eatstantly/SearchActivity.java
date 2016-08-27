package com.eatstantly.eatstantly;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by emilyhedlund on 8/8/16.
 */
public class SearchActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // constants
    private static final String key = "AIzaSyAJssY_hmpCAb2NqUB7ZKFFahJEVifmExw";
    private static final double metersInMile = 1609.34;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final String TAG = SearchActivity.class.getSimpleName();

    // declare variables
    Button confirmSearch;
    CheckBox useCurrent;
    Place selectedLocation;
    Place currentLocation;
    PlaceAutocompleteFragment autocompleteFragment;
    Spinner distances;
    int distanceSelected;
    String reply;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // create client
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        // initialize UI elements
        confirmSearch = (Button) findViewById(R.id.confirmSearch);
        useCurrent = (CheckBox) findViewById(R.id.useCurrent);
        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        distances = (Spinner) findViewById(R.id.distances);

        // set up distance spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.distance_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distances.setAdapter(adapter);
        distances.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                switch (pos) {
                    case 0:  distanceSelected = 1;
                        break;
                    case 1:  distanceSelected = 5;
                        break;
                    case 2:  distanceSelected = 10;
                        break;
                    case 3:  distanceSelected = 25;
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                distanceSelected = 1;
            }
        });

        // autocomplete fragment
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                selectedLocation = place;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

        // check box action
        useCurrent.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        getLocation();
                    }
                }
        );

        // search button action
        confirmSearch.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        String baseURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

                        // use current location
                        if (useCurrent.isChecked()) {

                        }
                        // use selected location
                        else {
                            baseURL += "key=" + key + "&";

                            // get location
                            LatLng latAndLong = selectedLocation.getLatLng();
                            String fullString = latAndLong.toString();
                            int length = fullString.length();
                            String latLong = fullString.substring(10, length - 1);
                            baseURL += "location=" + latLong + "&";

                            // get radius
                            // double distInMeters = distanceSelected * metersInMile;
                            // baseURL += "radius=" + distInMeters + "&";

                            baseURL += "rankby=distance&";
                            baseURL += "type=restaurant";
                        }
                        // make request
                        new makeRequest().execute(baseURL);
                    }
                }
        );
    }

    // make request
    private class makeRequest extends AsyncTask<String, Void, String> {
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
            if (output != null) {
                reply = output;

                // launch results
                Intent listIntent = new Intent(SearchActivity.this, ListActivity.class);
                listIntent.putExtra("Reply", reply);
                SearchActivity.this.startActivity(listIntent);
            }
        }
    }

    // get current location
    @TargetApi(23)
    private void getLocation() {
        int hasPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "permissions good");
            // get location
            Thread apiCallThread = new Thread() {
                @Override
                public void run() {
                    Log.e(TAG, "on new thread");
                    PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                    Log.e(TAG, "API call made");
                    PlaceLikelihoodBuffer likelyPlaces = result.await();
                    Log.e(TAG, "await executed");
                    int size = likelyPlaces.getCount();
                    Log.e(TAG, "count: " + size);
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Log.e(TAG, "in loop");
                        Log.e(TAG, String.format("Place '%s' has likelihood: %g",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }
                    Log.e(TAG, "loop exited");
                    likelyPlaces.release();
                }
            };
            apiCallThread.start();
        }
        else {
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    // permission denied
                    Log.e(TAG, "ACCESS_FINE_LOCATION permission denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // necessary overrides for connecting/disconnecting
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    // necessary overrides for implementations
    @Override
    public void onConnectionSuspended(int i) {
        // TODO: something
    }

    @Override
    public void onConnectionFailed(ConnectionResult cr) {
        // TODO: something
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // TODO: something
    }
}