package com.ottershare.ottershare;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;


public class MapOSFragment extends Fragment implements OnMapReadyCallback{

    MapView mMapView;
    private GoogleMap mGoogleMap;
    private CameraUpdateFactory camUpdate;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    public MapOSFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();
        mMapView.getMapAsync(this);

        try{
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e){
            e.printStackTrace();
        }

        return view;

    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (mGoogleMap != null){
        }
    }

    //jumps camera to location

    public void changeCameraLocation(double lat, double lon, float zoom){
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),zoom));
    }

    //anamates camera to location

    //adds a marker to the map
    public void makeMarker(double lat, double lon){
        mGoogleMap.addMarker(new MarkerOptions()
        .position(new LatLng(lat,lon)));
    }


    //removes all markers,overlays and shapes
    public void removeAllFormating(){
        mGoogleMap.clear();
    }

    //turnes current markers into a heat map
    public void addHeatMap(ArrayList<LatLng> locations){

        int[] colors = {
                R.color.colorPrimary,
                R.color.colorPrimaryDark
        };

        float[] startPoints = {
                0.2f,1f
        };

        Gradient gradient = new Gradient(colors,startPoints);
        mProvider = new HeatmapTileProvider.Builder()
                .data(locations)
                //.gradient(gradient)
                .radius(15)
                .build();

        mProvider.setOpacity(1.0);
        mOverlay = mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        }

}
