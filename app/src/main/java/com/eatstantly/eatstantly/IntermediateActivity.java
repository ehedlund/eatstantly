package com.eatstantly.eatstantly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by emilyhedlund on 8/12/16.
 */
public class IntermediateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        // get token
        String tokenURL = getIntent().getDataString();
        int start = tokenURL.lastIndexOf('=');
        int length = tokenURL.length();
        String token = tokenURL.substring(start + 1, length);

        // save token
        SharedPreferences sp = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", token);
        editor.commit();

        // launch LandActivity
        Intent landIntent = new Intent(IntermediateActivity.this, LandActivity.class);
        IntermediateActivity.this.startActivity(landIntent);
    }
}