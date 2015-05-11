package com.tytogroup.mobitrack.location;

import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tytogroup.mobitrack.R;
import com.tytogroup.mobitrack.user.AllUsers;
import com.tytogroup.mobitrack.user.User;

import java.util.ArrayList;

/**
 * Created by hsyn on 4/27/2015.
 */
public class FriendsOnMap extends Fragment {


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager loma;
    private ArrayList<Marker> markers=new ArrayList<>();

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
        if (mMap == null) {
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        //mMap.setMyLocationEnabled(true);
        AllUsers users=AllUsers.getInstance();
        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        for(int i=0;i<users.getCount();i++){
            User u=users.getUser(i);
            Marker m=mMap.addMarker(new MarkerOptions().position(u.location));
            markers.add(m);
            builder.include(u.location);
        }
        LatLngBounds bounds=builder.build();
        final CameraUpdate cu= CameraUpdateFactory.newLatLngBounds(bounds,50);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.animateCamera(cu);
            }
        });
        //CameraUpdate ca= CameraUpdateFactory.newLatLngZoom(ll, 15);
        //mMap.animateCamera(ca);
        //m=mMap.addMarker(new MarkerOptions().position(ll));
    }

    GoogleMap.OnMapClickListener mapClickListener=new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            /*if(m==null)
                m=mMap.addMarker(new MarkerOptions().position(latLng));
            else
                m.setPosition(latLng);
            */
        }
    };
}
