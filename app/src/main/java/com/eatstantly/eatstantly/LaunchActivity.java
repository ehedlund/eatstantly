package com.eatstantly.eatstantly;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

/**
 * Created by emilyhedlund on 8/8/16.
 */
public class LaunchActivity extends AppCompatActivity {
    // declare variables
    private static final String clientID = "11436d58cf5c425cb59a97bb61bf1789";
    private static final String redirect = "http://eatstantly.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // check for authentication
        SharedPreferences sp = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        if (sp.contains("token")) {
            // launch LandActivity
            String token = sp.getString("token", "error");
            if (! token.equals("error")) {
                Intent landIntent = new Intent(LaunchActivity.this, LandActivity.class);
                LaunchActivity.this.startActivity(landIntent);
            }
        }
        else {
            // authenticate
            String url = "https://api.instagram.com/oauth/authorize/?client_id=" + clientID + "&redirect_uri=" + redirect + "&response_type=token";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }
}
