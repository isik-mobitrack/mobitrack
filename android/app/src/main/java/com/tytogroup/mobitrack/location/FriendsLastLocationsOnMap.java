package com.tytogroup.mobitrack.location;

import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.tytogroup.mobitrack.ParseManager;
import com.tytogroup.mobitrack.R;
import com.tytogroup.mobitrack.user.AllFriendships;
import com.tytogroup.mobitrack.user.AllUsers;
import com.tytogroup.mobitrack.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 13.05.2015.
 */
public class FriendsLastLocationsOnMap extends Fragment {
    private GoogleMap gMap;
    private LocationManager loma;
    private ArrayList<Marker> markers=new ArrayList<>();
    private ArrayList<LatLng> lastLocations=new ArrayList<>();
    public int position=-1;
    ParseManager manager=ParseManager.getInstance();
    AllFriendships friendships=AllFriendships.getInstance();

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_view, container, false);
    }
    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (lastLocations.size() == 0) {
            manager.getLastLocations(friendships.getFriendPhone(position), 100, new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if(parseObjects.size()==0){
                        Toast.makeText(getActivity(),"There is no last locations for this user",Toast.LENGTH_LONG).show();
                    }
                    else {
                        for (int i = 0; i < parseObjects.size(); i++) {
                            ParseObject p = parseObjects.get(i);
                            ParseGeoPoint po = p.getParseGeoPoint("locations");
                            lastLocations.add(new LatLng(po.getLatitude(), po.getLongitude()));
                        }
                        if (gMap == null) {
                            gMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
                            if (gMap != null) {
                                setUpMap();
                            }
                        }
                    }
                }
            });
        } else {

            if (gMap == null) {
                gMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
                if (gMap != null) {
                    setUpMap();
                }
            }
        }
    }
    private void setUpMap() {

        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        for(int i=0;i<lastLocations.size();i++){
            Marker m=gMap.addMarker(new MarkerOptions().position(lastLocations.get(i)));
            markers.add(m);
            builder.include(lastLocations.get(i));
        }
        LatLngBounds bounds=builder.build();
        final CameraUpdate cu= CameraUpdateFactory.newLatLngBounds(bounds, 50);
        gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                gMap.animateCamera(cu);
            }
        });
    }


    }

