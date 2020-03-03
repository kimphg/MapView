package com.SeaMap.myapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.Places;
import com.SeaMap.myapplication.classes.ReadFile;
import com.SeaMap.myapplication.classes.StableArrayAdapter;
import com.SeaMap.myapplication.object.Text;
import com.SeaMap.myapplication.services.GPS_Services;
import com.SeaMap.myapplication.view.DensityView;
import com.SeaMap.myapplication.view.DistancePTPView;
import com.SeaMap.myapplication.view.PolygonsView;
import com.SeaMap.myapplication.view.SeaMap;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SearchView.OnQueryTextListener {
    //Todo: dung dinh danh moi request
    public static final int REQUEST_INPUT = 1001;
    public static final int DISTANCE = 1002;
    public static final int ROUTE = 1003;

    //Todo: khi click
    public static int CHOOSE_BTN_LAYERS = 0;
    public static int CHOOSE_DISTANE_OR_ROUTE = 0;
    //
    private int onViewMain = 0;
    private int REQUEST_SEARCH = 0;

    //Khi an 1 keo dai khung hien thi route
    //Khi an 0 thu nho khung hien thi route
    //up: 1| down: 0
    private int up_down_route = 0;

    //lay vi tri hien tai
    private float longitude =0, latitude =0;
    private String lonlat[];

    //Todo: cac view hien thi
    public static DistancePTPView distancePTPView;
    //public BuoyView buoyView;
    public DensityView densityView;
    public SeaMap map;
    private NavigationView navigationView;

    private SearchView searchView;

    //Todo: Doc du lieu dau vao
    private ReadFile read;

    private Text textSearch;
    private Places places;
    private TextView _distance;

    //Todo: Cac nut an va nhan su kien
    private ImageButton imageButtonGPS, imageButtonOther, imageBtnSearch, imageButtonDirection, imageBtnUp_Of_Route, imgButtonAddDirections, imageBtnLayer;
    private BroadcastReceiver broadcast;
    private GoogleApiClient googleApiClient;

    //Todo : layout main va layout route
    private FrameLayout frameLayout;
    private RelativeLayout route_layout;
    private DrawerLayout mDrawerLayout;

    //Todo: khai bao cho route
    private StableArrayAdapter arrayAdapter;
    private List<Text> route;
    private List<String> namePlaces;
    //listview search trong phan info_route;
    private ListView listPlaceSeacrh, routesListView;

    private Places adapter;
    //todo: thong so khac
    private float dYs,dYe;

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

        frameLayout = findViewById(R.id.content_frame);
        read = new ReadFile(getApplicationContext());
        // su kien bat dau lo trinh
        _distance = findViewById(R.id._distance);

        //setting map;
        map = new SeaMap(getApplicationContext());
        frameLayout.addView(map, 0 );
                ///
        if(googleApiClient == null) {
            turnOnGPS();
        }

        onDistancePTPView();
        Run_check_permission();
        navigationDrawer();
        onRoute();
        onDensityView();
        onScreecBtn_Direction_GPS_Search();
    }

    //Todo: init and add onclick for button on screen
    private void onScreecBtn_Direction_GPS_Search(){
        imageButtonGPS = findViewById(R.id.ic_btn_location);
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

        imageBtnSearch = findViewById(R.id.ic_btn_search);
        imageBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivityForResult(searchIntent, REQUEST_INPUT);
//                searchSet.start();

            }
        });
        ///-----------------------------------//


    }


    //Todo---------------- Set layout route cac su kien-----------------------//
    /*
    * Gom cac su kien click item
    *  va khoi tao
    * */
    private void onRoute(){
        //Khi khoi tao an di
        route_layout = findViewById(R.id.route_layout);
        route_layout.setVisibility(View.INVISIBLE);

        routesListView = findViewById(R.id.route_listview);
        listPlaceSeacrh = findViewById(R.id.listplace);
        searchView = findViewById(R.id.searchview_place);

        List list = ReadFile.ListPlace;
        //.............tao adpter.....
        places = new Places(this,list);
        listPlaceSeacrh.setAdapter(places);
        searchView.setOnQueryTextListener(this);
        //##### su kien an vao item cua listview: 1.route  2.search //
        adapterListPlace();

        //su kien khi an vao item cua list view search
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

        //list dia diem da chon
        //Su kien khi an vao dynamic ListView
        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                route.remove(i);
                namePlaces.remove(i);
                arrayAdapter.notifyDataSetChanged();
            }
        });


        // phong to thu nho route
        imageBtnUp_Of_Route = findViewById(R.id.button_up);
        imageBtnUp_Of_Route.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                switch (up_down_route){
                    //0: phong to
                    case 0:{
                        int height = PolygonsView.scrCtY * 5 / 4;
                        route_layout.getLayoutParams().height = height;
                        route_layout.requestLayout();
                        up_down_route = 1;
                        break;
                    }
                    //1: thu nho
                    case 1:{
                        int height = PolygonsView.scrCtY / 4;
                        route_layout.getLayoutParams().height = height;
                        route_layout.requestLayout();
                        up_down_route = 0;
                        break;
                    }
                }
            }

        });

//        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker, null);
    }

    // Todo -------------- -----------------------//
    private void onDistancePTPView(){
        imgButtonAddDirections = findViewById(R.id.icon_addDirect);
        imgButtonAddDirections.setVisibility(View.INVISIBLE);

        imgButtonAddDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (CHOOSE_DISTANE_OR_ROUTE) {
                    case DISTANCE: {

                        break;
                    }
                    case ROUTE: {
                        PointF p = SeaMap.ConvScrPointToWGS(PolygonsView.scrCtX, SeaMap.scrCtY);
                        float [] coor = {p.x, p.y};
                        String name_new_place = p.y + "'B , "+ p.x + "'Đ.";

                        Text new_place = new Text();
                        new_place.setCoordinate(coor);
                        new_place.setName( name_new_place);

                        route.add(new_place);
                        namePlaces.add(name_new_place);
                        arrayAdapter.notifyDataSetChanged();

                        distancePTPView.setListCoor(route);
                        distancePTPView.invalidate();
                        break;
                    }
                }
            }
        });
        imgButtonAddDirections.setVisibility(View.INVISIBLE);
    }


    //Todo : Khoi tao density va hien view khi an layer
    public void onDensityView(){
        densityView = new DensityView(getApplicationContext());
        imageBtnLayer = findViewById(R.id.ic_btn_layers);
        imageBtnLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (CHOOSE_BTN_LAYERS){
                    case 0:{
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
    //Todo ------------------------------------------//



    //Todo : Mo drawer
    /*
    * GOm cac su kien an item
    * Mo drawer
    * */
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

                        distancePTPView = new DistancePTPView(getApplicationContext());
                        frameLayout.addView(distancePTPView);
                        route_layout.setVisibility(View.VISIBLE);
                        imageButtonOther.setBackgroundResource(R.drawable.icon_back);
                        imgButtonAddDirections.setVisibility(View.VISIBLE);
                        break;
                    }
                    case R.id.nav_tinhkhoangcach: {
                        CHOOSE_DISTANE_OR_ROUTE = DISTANCE;
                        onViewMain = 1;
                        distancePTPView = new DistancePTPView(getApplicationContext());
                        frameLayout.addView(distancePTPView);
                        imageButtonOther.setBackgroundResource(R.drawable.icon_back);
                        imgButtonAddDirections.setVisibility(View.VISIBLE);


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
                        mDrawerLayout.openDrawer(GravityCompat.START);
                        break;
                    }
                    case 1:{
                        CHOOSE_DISTANE_OR_ROUTE = 0;
                        onViewMain = 0;
                        imageButtonOther.setBackgroundResource(R.drawable.icon_cancel);
                        imgButtonAddDirections.setVisibility(View.INVISIBLE);
                        navigationView.getCheckedItem().setChecked(false);
                        frameLayout.removeView(distancePTPView);
                        distancePTPView = null;
                        route_layout.setVisibility(View.INVISIBLE);
                        route_layout.getLayoutParams().height = 900;
                        route_layout.requestLayout();
                        namePlaces.clear();
                        arrayAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
    }
    // Todo///////////---------------Drawer-----------------/////////////

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

    //Todo: turn onGPS location

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
        arrayAdapter = new StableArrayAdapter(this,namePlaces);
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
