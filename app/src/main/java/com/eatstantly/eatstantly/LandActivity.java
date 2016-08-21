package com.eatstantly.eatstantly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by emilyhedlund on 8/12/16.
 */
public class LandActivity extends AppCompatActivity {
    // declare variables
    Button discover;
    Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_land);

        // initialize variables
        discover = (Button) findViewById(R.id.discover);
        search = (Button) findViewById(R.id.search);

        // set up buttons
        search.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent searchIntent = new Intent(LandActivity.this, SearchActivity.class);
                        LandActivity.this.startActivity(searchIntent);
                    }
                }
        );

        discover.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent discoverIntent = new Intent(LandActivity.this, DiscoverActivity.class);
                        LandActivity.this.startActivity(discoverIntent);
                    }
                }
        );


    }
}