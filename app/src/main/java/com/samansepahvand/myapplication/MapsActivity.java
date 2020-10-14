package com.samansepahvand.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.samansepahvand.myapplication.util.Constant;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap gMap;
    private LatLng prev;
    private static final String TAG = "TAGS";

    Toolbar toolbar;
    ImageView imgMenu;
    NavigationView nav;
    DrawerLayout drav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //   initMapReq();

        initView();
        setSupportActionBar(toolbar);
        setNavigationViewListener();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!drav.isShown()) {
                    drav.openDrawer(Gravity.RIGHT);
                }
            }
        });


    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        imgMenu = findViewById(R.id.img_menu);

        nav = findViewById(R.id.navigationView);
        drav = findViewById(R.id.drawer);

    }


    private void handlePermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            //  getCurrentLocation();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constant.LOCATION_PERMISSION);
            Toast.makeText(this, "for use location map you need this permission!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constant.LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handlePermission();
            } else {
                finish();
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;

        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);

        FollowMeLocationSource locationSource = new FollowMeLocationSource();
        locationSource.getBestAvailableProvider();
        gMap.setLocationSource(locationSource);
        handlePermission();


        gMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Double lat = gMap.getCameraPosition().target.latitude;
                Double lng = gMap.getCameraPosition().target.longitude;
                //    prev = new LatLng(33.57001265, 48.43169629);
                prev = new LatLng(lat, lng);


                goLocByLatLng(prev);
                setDirectaion(prev, prev);
                return false;
            }
        });

        if (prev != null) {
            Double lat = gMap.getCameraPosition().target.latitude;
            Double lng = gMap.getCameraPosition().target.longitude;
            prev = new LatLng(lat, lng);
            gMap.addMarker(new MarkerOptions()
                    .position(prev));
            setDirectaion(prev, prev);

        }


    }

    private void goLocByLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);
        try {

            List<Address> list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            Address address = list.get(0);

            double lat = address.getLatitude();
            double lng = address.getLongitude();

            gMap.addMarker(new MarkerOptions()
                    .title(address.getFeatureName()).position(new LatLng(lat, lng)));
        } catch (IOException e) {

            e.printStackTrace();
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void setDirectaion(LatLng preLatLng, LatLng latLng) {

        Polyline polyline = gMap.addPolyline(new PolylineOptions()
                .add(preLatLng, latLng)
                .width(10)
                .color(Color.BLACK));

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.item_doreh:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://faranesh.com/author/samansepahvand")));
                break;
            case R.id.item_sourcecod:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NeshanMaps/android-neshan-maps-starter")));
                break;

            case R.id.item_youtube:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NeshanMaps/android-neshan-maps-starter")));
                break;
            case R.id.item_about:




                break;

        }

    //close navigation drawer
        drav.closeDrawer(GravityCompat.START);
        return true;
}

    private void setNavigationViewListener() {
        nav.setNavigationItemSelectedListener(this);
    }


private class FollowMeLocationSource implements LocationSource, LocationListener {


    private OnLocationChangedListener mListener;
    private LocationManager locationManager;
    private final Criteria criteria = new Criteria();
    private String bestAvailableProvider;
    /* Updates are restricted to one every 10 seconds, and only when
     * movement of more than 10 meters has been detected.*/
    private final int minTime = 10000;     // minimum time interval between location updates, in milliseconds
    private final int minDistance = 10;    // minimum distance between location updates, in meters


    private FollowMeLocationSource() {
        // Get reference to Location Manager
        locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);

        // Specify Location Provider criteria
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
    }


    private void getBestAvailableProvider() {
        /* The preferred way of specifying the location provider (e.g. GPS, NETWORK) to use
         * is to ask the Location Manager for the one that best satisfies our criteria.
         * By passing the 'true' boolean we ask for the best available (enabled) provider. */
        bestAvailableProvider = locationManager.getBestProvider(criteria, true);
    }

    /* Activates this provider. This provider will notify the supplied listener
     * periodically, until you call deactivate().
     * This method is automatically invoked by enabling my-location layer. */
    @SuppressLint("MissingPermission")
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        // We need to keep a reference to my-location layer's listener so we can push forward
        // location updates to it when we receive them from Location Manager.
        mListener = listener;

        // Request location updates from Location Manager
        if (bestAvailableProvider != null) {
            locationManager.requestLocationUpdates(bestAvailableProvider, minTime, minDistance, this);
        } else {
        }
    }

    /* Deactivates this provider.
     * This method is automatically invoked by disabling my-location layer. */
    @Override
    public void deactivate() {
        // Remove location updates from Location Manager
        locationManager.removeUpdates(this);

        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        /* Push location updates to the registered listener..
         * (this ensures that my-location layer will set the blue dot at the new/received location) */

        if (mListener != null) {
            mListener.onLocationChanged(location);
        }
        /* ..and Animate camera to center on that location !
         * (the reason for we created this custom Location Source !) */
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20));

        if (prev == null) {
            setDirectaion(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(location.getLatitude(), location.getLongitude()));
        } else {
            setDirectaion(prev, new LatLng(location.getLatitude(), location.getLongitude()));
        }

        prev = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drav.isShown()) {
            drav.closeDrawer(Gravity.RIGHT);
        }
    }
}