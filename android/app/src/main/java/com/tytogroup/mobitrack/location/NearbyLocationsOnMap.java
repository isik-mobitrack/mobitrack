package com.tytogroup.mobitrack.location;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tytogroup.mobitrack.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by user on 05.05.2015.
 */
public class NearbyLocationsOnMap extends Fragment {
    private GoogleMap gMap;
    private LocationManager locManager;
    private ArrayList<Marker> markers = new ArrayList<>();
    double mLat = 0;
    double mLong = 0;
    Marker m;
    LatLngBounds.Builder builder=new LatLngBounds.Builder();
    LatLngBounds bounds;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_view, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (gMap == null) {
            gMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (gMap != null) {
                setUpMap();
            }
        }
    }
    private void setUpMap()  {
        PlacesTask placesTask = new PlacesTask();
        placesTask.execute();

    }
    private String downloadUrl() {
        Double myLat=MyLocation.getlatitude();
        Double myLong=MyLocation.getlongitude();
        String strUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+myLat+","+myLong+"&radius=5000&types=hospital|police&sensor=true&key=AIzaSyCRTPt0M4d9-26B_MsGLDeTJXdpjlk7hdE";
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while( ( line = br.readLine())!= null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
            iStream.close();
            urlConnection.disconnect();
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }
       return data;
    }

    private class PlacesTask extends AsyncTask<String, Integer, String>{

        String data = null;
        BitmapDescriptor map_icon;
        String title="";
        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl();
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            System.out.println("Data :- " + data);
            try {
                JSONArray ar=new JSONArray(new JSONObject(data).getString("results"));
                LatLngBounds.Builder builder=new LatLngBounds.Builder();
                for(int i=0;i<ar.length();i++) {
                    JSONObject o = new JSONObject(ar.getString(i));
                    double lat=o.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng=o.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    JSONArray type=o.getJSONArray("types");
                    String name=o.getString("name");
                    if(type.getString(0).contains("hospital")){
                       map_icon =BitmapDescriptorFactory.fromResource(R.drawable.hospital_icon);
                        title=name;
                    }else  {
                        map_icon =BitmapDescriptorFactory.fromResource(R.drawable.police_map_icon);
                        title=name;
                    }
                    m=gMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).icon(map_icon).title(title));
                    markers.add(m);
                    builder.include(new LatLng(lat, lng));
                }
                LatLngBounds bounds=builder.build();
                final CameraUpdate cu= CameraUpdateFactory.newLatLngBounds(bounds,50);
                gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        gMap.animateCamera(cu);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }


        }

    }
}
