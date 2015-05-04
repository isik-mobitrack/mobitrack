package com.tytogroup.mobitrack.user;

import com.google.android.gms.maps.model.LatLng;

public class User {
    public static final String PHONE="phone";
    public static final String USERNAME="username";
    public static final String LAST_LOCATION="lastLocation";

    public String username="";
    public String phone="";
    public LatLng location;
    public float[] distance=new float[1];

    public User() {}

    public User(String username, String phone, LatLng location) {
        this.username = username;
        this.phone = phone;
        this.location=location;
    }

    public String getDistanceString(){
        float meter=distance[0];
        if(meter<100){
            return (((int)(meter*10))/10)+"m";
        }else if(meter<1000){
            return ((int)meter)+"m";
        }else{
            return ((float)((int)(meter*10/1000))/10)+"km";
        }
    }
}
