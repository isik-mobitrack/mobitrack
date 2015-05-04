package com.tytogroup.mobitrack.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by hsyn on 4/29/2015.
 */
public class MyLocation implements LocationListener {
    private static MyLocation instance;
    private static final long MIN_TIME = 60000;
    private static final float MIN_DISTANCE = 200;

    public double longitude=0;
    public double latitude=0;
    private boolean isSet=false;
    private LocationManager loma;
    public LocationSet subscriber;
    private boolean called=false;

    private MyLocation(Context context){
        loma=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }


    public static MyLocation getMyLocation(Context context){
        if(instance==null){
            instance=new MyLocation(context);
        }
        return instance;
    }

    public void callHard(){
        isSet=false;
        called=false;
        callLocation();
    }

    public static double getlatitude(){
        return instance.latitude;
    }

    public static double getlongitude(){
        return instance.longitude;
    }

    public void callLocation(){
        if(called)
            return;
        called=true;
        if(loma.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            loma.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        }
        if(loma.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            loma.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        }
        if(loma.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
            loma.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        }

    }

    public static boolean checkLocationServices(Context context){
        MyLocation lo= MyLocation.getMyLocation(context);
        LocationManager loma=lo.loma;

        if(loma.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return false;
        }
        if(loma.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        if(loma.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
            return false;
        }
        return true;
    }

    public boolean isSet(){
        return isSet;
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude=location.getLongitude();
        latitude=location.getLatitude();
        called=false;
        loma.removeUpdates(this);
        if(!isSet) {
            isSet = true;
            if (subscriber != null) {
                subscriber.locationSet();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
