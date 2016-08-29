package com.eatstantly.eatstantly;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by emilyhedlund on 8/28/16.
 */
public class Restaurant implements Parcelable {
    String name;
    String address;
    String latitude;
    String longitude;
    String icon_1;
    String icon_2;
    String icon_3;

    public Restaurant(JSONObject js) {
        try {
            name = js.getString("name");
            address = js.getString("vicinity");
            latitude = js.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = js.getJSONObject("geometry").getJSONObject("location").getString("lng");
            icon_1 = "none";
            icon_2 = "none";
            icon_3 = "none";
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // parcelling part
    public Restaurant(Parcel in){
        String[] data = new String[7];

        in.readStringArray(data);
        this.name = data[0];
        this.address = data[1];
        this.latitude = data[2];
        this.longitude = data[3];
        this.icon_1 = data[4];
        this.icon_2 = data[5];
        this.icon_3 = data[6];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.name, this.address, this.latitude, this.longitude, this.icon_1, this.icon_2, this.icon_3});
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