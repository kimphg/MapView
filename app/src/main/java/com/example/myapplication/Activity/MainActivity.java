package com.example.myapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.classes.Places;
import com.example.myapplication.classes.ReadFile;
import com.example.myapplication.classes.StableArrayAdapter;
import com.example.myapplication.object.Text;
import com.example.myapplication.services.GPS_Services;
import com.example.myapplication.view.BuoyView;
import com.example.myapplication.view.DensityView;
import com.example.myapplication.view.DistancePTPView;
import com.example.myapplication.view.PolygonsView;
import com.example.myapplication.view.SeaMap;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SearchView.OnQueryTextListener {
    //dung dinh danh moi request
    public static final int REQUEST_INPUT = 1001;
    public static final int DISTANCE = 1002;
    public static final int ROUTE = 1003;

    public static DistancePTPView distancePTPView;
    //public BuoyView buoyView;
    public DensityView densityView;
    SeaMap map;
    ReadFile read;

    private Text textSearch;
    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    private ImageButton imageButtonGPS, imageButtonOther, imageButtonUp, imageBtnSearch, imageButtonDirection, imgButtonAddDirections, imageBtnLayer;
    private BroadcastReceiver broadcast;
    private GoogleApiClient googleApiClient;
    private float longitude =0, latitude =0;
    String lonlat[];
    SearchView searchView, search;
    Places places;
    Button startRoute;
    TextView _distance;

    public static int CHOOSE_BTN_LAYERS = 0;
    public static int CHOOSE_DISTANE_OR_ROUTE = 0;
    private int onViewMain = 0;
    private int REQUEST_SEARCH = 0;

    RelativeLayout routeLayout;
    FrameLayout frameLayout;
    //khai bao cho route
    private StableArrayAdapter arrayAdapter;
    private List<Text> route;
    private List<String> namePlaces;
    //listview search trong phan info_route;
    ListView listPlaceSeacrh, routesListView;
    private Places adapter;

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

                map.setLonLatMyLocation(latitude,longitude);
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

        imageButtonGPS = findViewById(R.id.ic_btn_location);
        //map = findViewById(R.id.map);
        routesListView = findViewById(R.id.route_listview);
        frameLayout = findViewById(R.id.content_frame);

        //----------------info_layout--------//
        listPlaceSeacrh = findViewById(R.id.listplace);
        routeLayout = findViewById(R.id.route_layout);
        routeLayout.setVisibility(View.INVISIBLE);

        read = new ReadFile(this);

        imageBtnSearch = findViewById(R.id.ic_btn_search);
        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                route.remove(i);
                namePlaces.remove(i);
                arrayAdapter.notifyDataSetChanged();
            }
        });
        // su kien bat dau lo trinh
        _distance = findViewById(R.id._distance);

        onDistancePTPView();

        //setting map;
        map = new SeaMap(this);
        frameLayout.addView(map, 0 );
                ///
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

        imageBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setAlpha(0.5f);
                Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivityForResult(searchIntent, REQUEST_INPUT);
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

        imageButtonDirection = findViewById(R.id.ic_btn_directions);
        imageButtonDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (REQUEST_SEARCH){
                    case 1:
                        //float
                        map.myLocationToDirection();
                        break;
                    case 0:
                        Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                        startActivityForResult(searchIntent, REQUEST_INPUT);
                        break;
                }
            }
        });

        onRoute();

//        onBuoy();
        onDensityView();
    }

    //---------------------------------------//

    private void onRoute(){
        List list = ReadFile.ListPlace;

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
                namePlaces.add(place.getName());
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onDistancePTPView(){
        imgButtonAddDirections = findViewById(R.id.icon_addDirect);
        imgButtonAddDirections.setVisibility(View.INVISIBLE);

        distancePTPView = new DistancePTPView(getApplicationContext());
        frameLayout.addView(distancePTPView);

        imgButtonAddDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (CHOOSE_DISTANE_OR_ROUTE) {
                    case DISTANCE: {

                        break;
                    }
                    case ROUTE: {
                        PointF p = SeaMap.ConvScrPointToWGS(SeaMap.scrCtX, SeaMap.scrCtY);
                        ArrayList<PointF> arr = distancePTPView.getListCoor();
                        arr.add(p);
                        distancePTPView.setListCoor(arr);
                        distancePTPView.invalidate();
                        break;
                    }
                }
            }
        });
        imgButtonAddDirections.setVisibility(View.INVISIBLE);
    }

//    public void onBuoy(){
//        buoyView = new BuoyView(this);
//        frameLayout.addView(buoyView);
//    }

    public void onDensityView(){
        densityView = new DensityView(getApplicationContext());
        imageBtnLayer = findViewById(R.id.ic_btn_layers);
        imageBtnLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (CHOOSE_BTN_LAYERS){
                    case 0:{
//                        read.readDensity();
                        frameLayout.removeView(map);
                        frameLayout.addView(densityView, 0);
                        CHOOSE_BTN_LAYERS = 1;
                        break;
                    }
                    case 1:{
                        frameLayout.removeView(densityView);
                        frameLayout.addView(map, 0);
                        CHOOSE_BTN_LAYERS = 0;
                        break;
                    }
                    default: break;
                }
            }
        });
    }

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

                switch (menuItem.getItemId()){
                    case R.id.nav_lotrinh: {
                        CHOOSE_DISTANE_OR_ROUTE = ROUTE;
                        onViewMain = 1;
                        imageButtonOther.setBackgroundResource(R.drawable.icon_back);
                        imgButtonAddDirections.setVisibility(View.VISIBLE);
                        routeLayout.setVisibility(View.VISIBLE);
                        break;
                    }
                    case R.id.nav_tinhkhoangcach: {
                        CHOOSE_DISTANE_OR_ROUTE = DISTANCE;
                        onViewMain = 1;
                        imageButtonOther.setBackgroundResource(R.drawable.icon_back);
                        imgButtonAddDirections.setVisibility(View.VISIBLE);
                        routeLayout.setVisibility(View.INVISIBLE);
                        break;
                    }
                    default:{
                        CHOOSE_DISTANE_OR_ROUTE = 0;
                        onViewMain  = 0;
                        break;
                    }
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        // su kien mo drawer//
        imageButtonOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (onViewMain) {
                    case 0: {
                        view.setAlpha(0.5f);
                        mDrawerLayout.openDrawer(GravityCompat.START);
                        break;
                    }
                    case 1:{
                        CHOOSE_DISTANE_OR_ROUTE = 0;
                        onViewMain = 0;
                        imageButtonOther.setBackgroundResource(R.drawable.icon_cancel);
                        routeLayout.setVisibility(View.INVISIBLE);
                        imgButtonAddDirections.setVisibility(View.INVISIBLE);
                        break;
                    }
                }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kiểm tra requestCode có trùng với REQUEST_CODE vừa dùng
        if(requestCode == REQUEST_INPUT) {

            // resultCode được set bởi DetailActivity
            // RESULT_OK chỉ ra rằng kết quả này đã thành công
            if(resultCode == Activity.RESULT_OK) {
                // Nhận dữ liệu từ Intent trả về
                textSearch = (Text) data.getSerializableExtra(SearchActivity.EXTRA_DATA);
                // Sử dụng kết quả result
                float mlon = (textSearch.getCoordinate()[0] + textSearch.getCoordinate()[2]) / 2;
                float mlat = (textSearch.getCoordinate()[1] + textSearch.getCoordinate()[3]) / 2;
                REQUEST_SEARCH = 1;
                map.setLonLatSearchPlace(mlat, mlon);
            } else {
                // DetailActivity không thành công, không có data trả về.
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
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //ham setting for listview lo trinh
    private void adapterListPlace(){
        route = new ArrayList<Text>();
        namePlaces = new ArrayList<String>();
        arrayAdapter = new StableArrayAdapter(this,R.layout.places_view,R.id.tx_namePlace,namePlaces);
        routesListView.setAdapter(arrayAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //arrayAdapter.filter(text);
        return false;
    }
}
