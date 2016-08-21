package com.eatstantly.eatstantly;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class DiscoverActivity extends AppCompatActivity {

    private static final String TAG = DiscoverActivity.class.getSimpleName();
    private static final String follows_query = "https://api.instagram.com/v1/users/self/follows?access_token=";
    private static final String followed_by_query = "https://api.instagram.com/v1/users/self/followed-by?access_token=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        SharedPreferences mPrefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String token = mPrefs.getString("token", "N/A");
        Log.e(TAG, " the token is " + token);
        String follows = null;
        String followed_by = null;
        try {
            follows = new makeRequest().execute(follows_query + token).get();
            followed_by = new makeRequest().execute(followed_by_query + token).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "" + follows);
        Log.e(TAG, "" + followed_by);
    }



    private class makeRequest extends AsyncTask<String, Void, String>

    {
        @Override
        protected String doInBackground(String... params) {
            String response;
            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();

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

            } catch (MalformedURLException e) {
                e.printStackTrace();
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
}
