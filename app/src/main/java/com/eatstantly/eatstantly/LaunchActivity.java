package com.eatstantly.eatstantly;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

/**
 * Created by emilyhedlund on 8/8/16.
 */
public class LaunchActivity extends AppCompatActivity {
    // declare variables
    static final String clientID = "11436d58cf5c425cb59a97bb61bf1789";
    static final String redirect = "http://eatstantly.com";

    Button discover;
    Button search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // initialize variables
        discover = (Button) findViewById(R.id.discover);
        search = (Button) findViewById(R.id.search);

        // set up buttons
        search.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent searchIntent = new Intent(LaunchActivity.this, SearchActivity.class);
                        LaunchActivity.this.startActivity(searchIntent);
                    }
                }
        );

        /*
        // authenticate with instagram
        String uri = "https://api.instagram.com/oauth/authorize/?client_id=" + clientID + "&redirect_uri=" + redirect + "&response_type=token";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(browserIntent);
        */
    }

    /*
    // get the instagram access token
    @Override
    protected void onResume() {
        // parse redirect URI
        // TODO: get URI
        super.onResume();
    }
    */
}
