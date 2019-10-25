package com.example.myapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.example.myapplication.R;
import com.example.myapplication.classes.Places;
import com.example.myapplication.object.Text;
import com.example.myapplication.services.GPS_Services;
import com.example.myapplication.view.MapView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SearchView.OnQueryTextListener {

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    private ImageButton imageButtonGPS, imageButtonOther, imageButtonUp, imageButtonCancelRoute;
    private BroadcastReceiver broadcast;
    private GoogleApiClient googleApiClient;
    private float longitude =0, latitude =0;
    String lonlat[];
    SearchView searchView;
    MapView map;
    Places places;
    Button startRoute;

    boolean turnOnRoute = true;

    RelativeLayout routeLayout;
    //khai bao cho route
    ArrayAdapter<String> arrayAdapter;
    List<Text> route;
    List<String> namePlaces;
    //listview search trong phan info_route;
    ListView listPlaceSeacrh, routesListView;
    List<Text> list;

    float dYs,dYe;
    boolean start = false;
    @Override
    protected void onResume() {
        super.onResume();

        broadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String text = intent.getExtras().getString("coordinate");
                lonlat = text.split(" ");
                longitude =  Float.parseFloat(lonlat[0]);
                latitude = Float.parseFloat(lonlat[1]);
                //map.setLonLat(18,109f);
                map.setLonLat(latitude,longitude);
            }
        };
        IntentFilter filter =new IntentFilter("update_service");
        registerReceiver(broadcast,filter);

    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButtonGPS = findViewById(R.id.imagebutton_gps);
        map = findViewById(R.id.map);
        routesListView = findViewById(R.id.route_LV);

        //----------------info_layout--------//
        routeLayout = findViewById(R.id.route_layout);
        routeLayout.setVisibility(View.INVISIBLE);
        listPlaceSeacrh = findViewById(R.id.listplace);

        list = map.listPlace();

        //.............tao adpter.....
        places = new Places(this,list);
        listPlaceSeacrh.setAdapter(places);

        searchView = findViewById(R.id.searchview_place);
        searchView.setOnQueryTextListener(this);

        //##### su kien an vao item cua listview: 1.route  2.search //
        adapterListPlace();
        //1.search
        listPlaceSeacrh.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Text place = places.getItem(i);
                route.add(place);
                namePlaces.add(place.name);
                arrayAdapter.notifyDataSetChanged();
            }
        });
        //2.route
        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                route.remove(i);
                namePlaces.remove(i);
                arrayAdapter.notifyDataSetChanged();
            }
        });
        //su kien cancel route quay ve ban dau
        imageButtonCancelRoute = findViewById(R.id.imgbt_cancel_route);

        imageButtonCancelRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                routeLayout.setVisibility(View.INVISIBLE);
                cancelROute();
            }
        });
        // su kien bat dau lo trinh
        startRoute = findViewById(R.id.bt_startroute);
        startRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!start) {
                    map.drawRoute(route);
                    startRoute.setText("Dung lo trinh");
                    start = true;
                }else {
                    startRoute.setText("Bat dau");
                    start = false;
                    map.canceldrawRoute();
                }

            }
        });

        //.....................................
        imageButtonUp = findViewById(R.id.button_up);
        /// su kien di chuyen layout lo trinh //
        imageButtonUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    dYs = motionEvent.getY();
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
                    dYe = motionEvent.getY();
                    routeLayout.getLayoutParams().height += (int) dYs - (int)dYe;
                    int maxheight = routeLayout.getLayoutParams().height;
                        if(maxheight >=100 && maxheight <= 1300)
                            routeLayout.requestLayout();

                }
                return true;
            }
        });
        ///-----------------------------------//


        if(googleApiClient == null) {
            turnOnGPS();
        }

        Run_check_permission();

        imageButtonGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(googleApiClient == null)
                    turnOnGPS();
                else {
                    Intent intent = new Intent(getApplicationContext(), GPS_Services.class);
                    startService(intent);
                }
            }
        });

        navigationDrawer();
    }
    //---------------------------------------//

    private void navigationDrawer(){
        mDrawerLayout = findViewById(R.id.drawer_layout);
        imageButtonOther = findViewById(R.id.ibt_others);

        navigationView = findViewById(R.id.nav_view);

        //su kien an vao item
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                // set item as selected to persist highlight
                menuItem.setChecked(true);
                //Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                //startActivity(intent);
                // close drawer when item is tapped
                map.setEnabled(false);
                //hien view
                routeLayout.setVisibility(View.VISIBLE);

                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        // su kien mo drawer//
        imageButtonOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }
    /////////////---------------Drawer-----------------/////////////

    private boolean Run_check_permission() {

        if (Build.VERSION.SDK_INT>=23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED)
        {

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return true;

        }
        return false;
    }

    public void turnOnGPS(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(MainActivity.this).build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        // **************************
        builder.setAlwaysShow(true); // this is the key ingredient
        // **************************

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result
                        .getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be
                        // fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling
                            // startResolutionForResult(),
                            // and check the result in onActivityResult().
                            googleApiClient = null;
                            status.startResolutionForResult(MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have
                        // no way to fix the
                        // settings so we won't show the dialog.

                        break;
                }
            }
        });
    }

    @Override
    protected void onStop() {
        unregisterReceiver(broadcast);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==100){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                imageButtonGPS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(googleApiClient == null) {
                            turnOnGPS();
                        }
                        else {
                            Intent intent = new Intent(getApplicationContext(), GPS_Services.class);
                            startService(intent);
                        }
                    }
                });
            }
            else{
                Run_check_permission();
            }
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        places.filter(text);
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //ham setting for listview lo trinh
    private void adapterListPlace(){
        route = new ArrayList<Text>();
        namePlaces = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this,R.layout.places_view,R.id.tx_namePlace,namePlaces);
        routesListView.setAdapter(arrayAdapter);
    }

    //Ham dung lo trinh
    public void cancelROute(){
        route.clear();
        arrayAdapter.clear();
        arrayAdapter.notifyDataSetChanged();
    }
}
