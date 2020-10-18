package com.samansepahvand.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.samansepahvand.myapplication.util.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener{

    private GoogleMap gMap;
    private LatLng prev;
    private static final String TAG = "TAGS";

    Toolbar toolbar;
    ImageView imgMenu,imgItems;
    NavigationView nav;
    DrawerLayout drav;

    FloatingActionButton fab;

    ConstraintLayout root;
    Bitmap bitmap;


    String filePath;

    int mycolor=Color.BLACK;


    SharedPreferences preferences;
    private  static final String PREF="pref";
    private  static final String COLORS="color";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initShared();

        initView();
        setNavigationViewListener();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drav.openDrawer(Gravity.RIGHT);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureScreen();




            }
        });

        imgItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(imgItems);

            }
        });

    }

    private void initShared() {
        preferences=getSharedPreferences(PREF,MODE_PRIVATE);

        if (preferences.contains(COLORS)){
            mycolor=preferences.getInt(COLORS,Color.BLACK);
        }else{
            mycolor=preferences.getInt(COLORS,Color.BLACK);
        }
    }

    private  void saveShared(int color){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt(COLORS,color);
        editor.commit();
    }
    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        imgMenu = findViewById(R.id.img_menu);
        imgItems = findViewById(R.id.img_items);
        nav = findViewById(R.id.navigationView);
        drav = findViewById(R.id.drawer);
        fab=findViewById(R.id.fab);
        root = (ConstraintLayout)findViewById(R.id.root);

    }


    private void handlePermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constant.LOCATION_PERMISSION);
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
                prev = new LatLng(lat, lng);
                goLocByLatLng(prev);
                setDirectaion(prev, prev,colorPolyline(mycolor));
                return false;
            }
        });

        if (prev != null) {
            Double lat = gMap.getCameraPosition().target.latitude;
            Double lng = gMap.getCameraPosition().target.longitude;
            prev = new LatLng(lat, lng);
            gMap.addMarker(new MarkerOptions()
                    .position(prev));
            setDirectaion(prev, prev,colorPolyline(mycolor));

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


    private void setDirectaion(LatLng preLatLng, LatLng latLng,int color) {
       Plolyline(preLatLng,latLng,color);
    }
    private void Plolyline(LatLng preLatLng, LatLng latLng,int color){
        Polyline polyline = gMap.addPolyline(new PolylineOptions()
                .add(preLatLng, latLng)
                .width(10)
                .color(colorPolyline(color)));
    }
    private  int colorPolyline(int color){
        return color;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.item_doreh:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://faranesh.com/author/samansepahvand")));
                break;
            case R.id.item_sourcecod:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SamanSepahvand/TracerApp")));
                break;

            case R.id.item_youtube:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCAB72ugAZ09MfEONwCJX8Mg")));
                break;
            case R.id.item_about:
                DialogInfo();
                break;

        }

        //close navigation drawer
        drav.closeDrawer(GravityCompat.END);
        return true;
    }
    private void setNavigationViewListener() {
        nav.setNavigationItemSelectedListener(this);
    }
    private void DialogInfo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.info_app_part1));
        builder.setMessage(getString(R.string.info_app_part2));
        builder.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

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
                setDirectaion(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(location.getLatitude(), location.getLongitude()),colorPolyline(mycolor));
            } else {
                setDirectaion(prev, new LatLng(location.getLatitude(), location.getLongitude()),colorPolyline(mycolor));
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

        if (drav.isShown()) {
            drav.closeDrawer(Gravity.RIGHT);
        } else super.onBackPressed();
    }
    public void captureScreen()   {

        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback()
        {
            @Override
            public void onSnapshotReady(Bitmap snapshot)
            {
                // TODO Auto-generated method stub
                 bitmap = snapshot;
                Date now = new Date();
                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

                OutputStream fout = null;
                File yourAppDir = new File(Environment.getExternalStorageDirectory()+File.separator+"TracerApp");
                if(!yourAppDir.exists() && !yourAppDir.isDirectory())
                {
                    if (yourAppDir.mkdirs())
                    {
                         filePath =yourAppDir+"/"+now + ".png";

                    }else {
                        Log.w(TAG,"Unable to create app dir!");}
                }else {   filePath =yourAppDir+"/"+now + ".png";}
                try  {
                    FileOutputStream outputStream = new FileOutputStream(filePath);
                    int quality = 100;
                    bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
                    outputStream.flush();
                    outputStream.close();
                }
                catch (FileNotFoundException e)
                {
                    Log.d("ImageCapture", e.getMessage());
                    filePath = "";
                }
                catch (IOException e)
                {
                    Log.d("ImageCapture", e.getMessage());
                    filePath = "";
                }

                openShareImageDialog(filePath);

            }

        };
        gMap.snapshot(callback);

    }
    public void openShareImageDialog(String filePath)
    {
        File file = new File(filePath);

        if(!filePath.equals(""))
        {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            final Uri contentUriFile = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
            startActivity(Intent.createChooser(intent, "Share Image"));
        }
        else
        {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_home, menu);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_maptype:
                showDialogMapType();
                break;
            case R.id.item_mapclear:
                gMap.clear();
                break;
            case R.id.item_changepolylinecolor:
                showDialogChangeColor();
                break;
        }
        return  true;
    }

    private void showDialogChangeColor() {
        String[] values = {"Red", "Blue", "Black", "Green", "White"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("لطفا رنک خط مسیر یابی  را انتخاب کتید.");
        builder.setSingleChoiceItems(values, 2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                       mycolor=Color.RED;
                        saveShared(mycolor);
                        dialog.dismiss();
                        break;
                    case 1:
                        mycolor=Color.BLUE;
                        saveShared(mycolor);
                        dialog.dismiss();
                        break;
                    case 2:
                        mycolor=Color.BLACK;
                        saveShared(mycolor);
                        dialog.dismiss();
                        break;
                    case 3:
                        mycolor=Color.GREEN;
                        saveShared(mycolor);
                        dialog.dismiss();
                        break;
                    case 4:
                        mycolor=Color.WHITE;
                        saveShared(mycolor);
                        dialog.dismiss();
                        break;
                }
            }
        });

        builder.show();


    }
    private void showDialogMapType() {
        String[] values = {"Normal", "Hybrid", "Satellite", "Terrain", "None"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("لطفا نوع نقشه را انتخاب کتید.");
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        dialog.dismiss();
                        break;
                    case 1:
                        gMap.setMapType(4);
                        dialog.dismiss();
                        break;
                    case 2:
                        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        dialog.dismiss();
                        break;
                    case 3:
                        gMap.setMapType(3);
                        dialog.dismiss();
                        break;
                    case 4:
                        gMap.setMapType(0);
                        dialog.dismiss();
                        break;
                }
            }
        });

        builder.show();


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}

