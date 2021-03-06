package com.eatstantly.eatstantly;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by emilyhedlund on 8/28/16.
 */
public class Restaurant implements Parcelable {

    private static final double SECONDS_IN_YEAR = 31536000;
    String name;
    String address;
    String latitude;
    String longitude;
    double score;
    String rating;
    String price;
    String icon_1;
    String icon_2;
    String icon_3;
    String locID;
    String icon_1_small;

    public Restaurant(JSONObject js) {
        try {
            name = js.getString("name");
            address = js.getString("vicinity");
            latitude = js.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = js.getJSONObject("geometry").getJSONObject("location").getString("lng");
            score = 0;
            rating = js.getString("rating");
            price = js.getString("price_level");
            icon_1 = null;
            icon_2 = null;
            icon_3 = null;
            locID = null;
            icon_1_small = null;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // parcelling part
    public Restaurant(Parcel in){
        String[] data = new String[11];

        in.readStringArray(data);
        this.name = data[0];
        this.address = data[1];
        this.latitude = data[2];
        this.longitude = data[3];
        this.rating = data[4];
        this.price = data[5];
        this.icon_1 = data[6];
        this.icon_2 = data[7];
        this.icon_3 = data[8];
        this.locID = data[9];
        this.icon_1_small = data[10];
    }

    public void setScore(JSONArray photoResults) {
        int numPhotos = photoResults.length();
        for (int i = 0; i < numPhotos; i++) {
            try {
                JSONObject photo = photoResults.getJSONObject(i);
                this.score += getPhotoScore(photo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private double getPhotoScore(JSONObject photo) {
        long unixTime = System.currentTimeMillis() / 1000L;
        try {
            double timePosted = photo.getDouble("created_time");
            double exponential = (timePosted - unixTime) / SECONDS_IN_YEAR;
            double exponential_function = Math.max(.1, Math.pow(10, exponential));
            double num_of_likes = photo.getJSONObject("likes").getInt("count");
            return exponential_function * (num_of_likes + 1);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.name, this.address, this.latitude, this.longitude, this.rating, this.price, this.icon_1, this.icon_2, this.icon_3, this.locID, this.icon_1_small});
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

}