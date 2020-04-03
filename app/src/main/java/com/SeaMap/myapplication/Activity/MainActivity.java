package com.SeaMap.myapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.Places;
import com.SeaMap.myapplication.classes.ReadFile;
import com.SeaMap.myapplication.classes.StableArrayAdapter;
import com.SeaMap.myapplication.object.Text;
import com.SeaMap.myapplication.services.GpsService;
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

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SearchView.OnQueryTextListener {
    //Todo: dung dinh danh moi request
    public static final int REQUEST_INPUT = 1001;
    public static final int DISTANCE = 1002;
    public static final int ROUTE = 1003;
    public static final int REQUEST_CODE = 1004;

    //Todo: khi click
    public static int CHOOSE_BTN_LAYERS = 0;
    public static int CHOOSE_DISTANE_OR_ROUTE = 0;
    //0: search 1:direction
    public static int CHOOSE_SEARCH_OR_DIRECTION = 0;
    //
    private int onViewMain = 0;
    private int REQUEST_SEARCH = 0;

    //Khi an 1 keo dai khung hien thi route
    //Khi an 0 thu nho khung hien thi route
    //up: 1| down: 0
    private int up_down_route = 0;

    //lay vi tri hien tai
    private float longitude = 0, latitude = 0;
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
    private ImageButton getCurLocationButton, imageButtonOther, imageBtnSearch, imageButtonDirection, imageBtnUp_Of_Route, imgButtonAddDirections, imageBtnLayer;
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


    //Khai bao cho GPS
    //functional variables
    private Location curLocation;
    private double curVelocity;
    //system variables
    private BroadcastReceiver broadcastReceiver;

    //todo: thong so khac
    private float temp_Search_lon = 0, temp_Search_lat = 0;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent( getApplicationContext(), GpsService.class);
        startService( intent );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        Intent intent = new Intent(getApplicationContext(), GpsService.class);
        stopService(intent);
    }
    List<Location> nearbyShips = new ArrayList<Location>();
    @Override
    protected void onResume() {
        super.onResume();

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Location newLocation = intent.getParcelableExtra("newLocation");

                    if (newLocation != null) {
                        curLocation = newLocation;
//                        Toast.makeText(MainActivity.this, curLocation.getLatitude() + " , " + curLocation.getLongitude(), Toast.LENGTH_LONG).show();
                    }

                    for(int i = 0;i<10;i++)
                    {
                        Location ship = intent.getParcelableExtra("nearbyShips"+Integer.toString(i));
                        if(ship!=null)nearbyShips.add(ship);
                    }

                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
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
        frameLayout.addView(map, 0);
        ///
        if (!checkPermission()) {
            requestPermission();
        }
        else{
            enableButtons();
        }


        onDistancePTPView();
        navigationDrawer();
        onRoute();
        onDensityView();
        onScreecBtn_Direction_Search();
        //enableButtons();
    }

    //Todo: init and add onclick for button on screen
    private void enableButtons() {
        //enable buttons' functions
        getCurLocationButton = findViewById(R.id.get_curlocation_btn);
        getCurLocationButton.setOnClickListener(view -> {

            if (curLocation != null) {
                map.setLonLatMyLocation(
                        Float.parseFloat(Double.toString(curLocation.getLatitude())),
                        Float.parseFloat(Double.toString(curLocation.getLongitude()))
                );
                if((nearbyShips!=null)&&(!nearbyShips.isEmpty()))
                {
                    map.setNearbyShips(nearbyShips);
                    nearbyShips.clear();
                }
            } else {
                Toast.makeText(MainActivity.this, "Xin hãy kiên nhẫn, thiết bị đang lấy dữ liệu ...", Toast.LENGTH_LONG).show();
            }

        });
    }

    private void onScreecBtn_Direction_Search() {
        imageButtonDirection = findViewById(R.id.ic_btn_directions);
        imageButtonDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (REQUEST_SEARCH) {
                    case 1:
                        //float
                        //Khong can vi tri vi da biet vi tri search trc do
                        map.myLocationToDirection(0, 0,0);
                        break;
                    case 0:
                        CHOOSE_SEARCH_OR_DIRECTION = 1;
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
                CHOOSE_SEARCH_OR_DIRECTION = 0;
                Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivityForResult(searchIntent, REQUEST_INPUT);
            }
        });
        ///-----------------------------------//


    }


    //Todo---------------- Set layout route cac su kien-----------------------//
    /*
     * Gom cac su kien;
     *  va khoi tao
     * */
    private void onRoute() {
        //Khi khoi tao an di
        route_layout = findViewById(R.id.route_layout);
        route_layout.setVisibility(View.INVISIBLE);

        routesListView = findViewById(R.id.route_listview);
        listPlaceSeacrh = findViewById(R.id.listplace);
        searchView = findViewById(R.id.searchview_place);

        List list = ReadFile.ListPlace;
        //.............tao adpter.....
        places = new Places(this, list);
        listPlaceSeacrh.setAdapter(places);
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

                float[] coor = place.getCoordinate();
                map.setLonLatSearchPlace(coor[1], coor[0]);
                //thiet lap lai va ve
                distancePTPView.setListCoor(route);
                distancePTPView.invalidate();
            }
        });

        //list dia diem da chon
        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                route.remove(i);
                namePlaces.remove(i);
                arrayAdapter.notifyDataSetChanged();
                // thiet lap lai va ve
                distancePTPView.setListCoor(route);
                distancePTPView.invalidate();
            }
        });


        // phong to thu nho route
        imageBtnUp_Of_Route = findViewById(R.id.button_up);
        imageBtnUp_Of_Route.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (up_down_route) {
                    //0: phong to
                    case 0: {
                        int height = PolygonsView.scrCtY * 5 / 4;
                        route_layout.getLayoutParams().height = height;
                        route_layout.requestLayout();
                        up_down_route = 1;
                        break;
                    }
                    //1: thu nho
                    case 1: {
                        int height = PolygonsView.scrCtY / 4;
                        route_layout.getLayoutParams().height = height;
                        route_layout.requestLayout();
                        up_down_route = 0;
                        break;
                    }
                }
            }

        });
    }

    // Todo -------------- -----------------------//
    private void onDistancePTPView() {
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
                        PointF p = SeaMap.ConvScrPointToWGS(SeaMap.scrCtX, SeaMap.scrCtY);
                        String name_place = p.y + "'B , " + p.x + "'Đ";
                        float[] coor = {p.x, p.y};

                        Text text = new Text();
                        text.setName(name_place);
                        text.setCoordinate(coor);

                        route.add(text);
                        namePlaces.add(name_place);
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
    public void onDensityView() {
        densityView = new DensityView(getApplicationContext());
        imageBtnLayer = findViewById(R.id.ic_btn_layers);
        imageBtnLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (CHOOSE_BTN_LAYERS) {
                    case 0: {
                        frameLayout.removeView(map);
                        frameLayout.addView(densityView, 0);
                        CHOOSE_BTN_LAYERS = 1;
                        break;
                    }
                    case 1: {
                        frameLayout.removeView(densityView);
                        frameLayout.addView(map, 0);
                        CHOOSE_BTN_LAYERS = 0;
                        break;
                    }
                    default:
                        break;
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
    private void navigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        imageButtonOther = findViewById(R.id.ibt_others);

        navigationView = findViewById(R.id.nav_view);

        //su kien an vao item
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                // set item as selected to persist highlight
                menuItem.setChecked(true);

                switch (menuItem.getItemId()) {
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
                    default: {
                        CHOOSE_DISTANE_OR_ROUTE = 0;
                        onViewMain = 0;
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
                    case 1: {
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
                        route.clear();
                        arrayAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
    }
    // Todo///////////---------------Drawer-----------------/////////////

    /////////////////----------------Gps-------------------//////////////
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GpsService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            return (ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)&&
                    (ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED);
        }else{
            Snackbar.make( map, "Vui lòng cập nhật phiên bản mới của hệ điều hành", Snackbar.LENGTH_LONG).show();
            return false;
        }
    }

    public void requestPermission(){
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE);
    }

    private void showMessageBox(String message, DialogInterface.OnClickListener clickListener ){
        new AlertDialog.Builder( this)
                .setMessage( message )
                .setPositiveButton("OK", clickListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void turnOnGPS() {
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
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if( requestCode == REQUEST_CODE ){
            if( grantResults.length > 0){
                boolean accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if( accepted ){
                    Snackbar.make( map, "Cho phép sử dụng vị trí", Snackbar.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), GpsService.class);
                    startService(intent);
                    enableButtons();
                }
                else{
                    Snackbar.make( map, "Không có quyền sử dụng vị trí", Snackbar.LENGTH_LONG).show();

                    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if( shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                            showMessageBox("Bạn cần cho phép ứng dụng sử dụng vị trí",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions( new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},
                                                    REQUEST_CODE);
                                        }
                                    });
                            return;
                        }
                        else{
                            showMessageBox("Bạn cần cho phép ứng dụng sử dụng vị trí.\nHãy cấp quyền cho ứng dụng này trong Cài đặt", null);
                        }
                    }

                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kiểm tra requestCode có trùng với REQUEST_CODE vừa dùng
        if (requestCode == REQUEST_INPUT) {

            // resultCode được set bởi DetailActivity
            // RESULT_OK chỉ ra rằng kết quả này đã thành công
            if (resultCode == Activity.RESULT_OK) {
                // Nhận dữ liệu từ Intent trả về
                textSearch = (Text) data.getSerializableExtra(SearchActivity.EXTRA_DATA);
                // Sử dụng kết quả result
                float mlon = (textSearch.getCoordinate()[0] + textSearch.getCoordinate()[2]) / 2;
                float mlat = (textSearch.getCoordinate()[1] + textSearch.getCoordinate()[3]) / 2;
                REQUEST_SEARCH = 1;
                temp_Search_lat = mlat;
                temp_Search_lon = mlon;

                switch (CHOOSE_SEARCH_OR_DIRECTION){
                    case 0: {
                        map.setLonLatSearchPlace(mlat, mlon);
                        CHOOSE_SEARCH_OR_DIRECTION = 1;
                        break;
                    }
                    case 1: {
                        map.myLocationToDirection(1, mlat, mlon);
                        break;
                    }
                }

            } else {
                // DetailActivity không thành công, không có data trả về.
                if(temp_Search_lon !=0 && temp_Search_lat !=0) {
                    switch (CHOOSE_SEARCH_OR_DIRECTION) {
                        case 0: {
                            map.setLonLatSearchPlace(temp_Search_lat, temp_Search_lon);
                            CHOOSE_SEARCH_OR_DIRECTION = 1;
                            break;
                        }
                        case 1: {
                            map.myLocationToDirection(1, temp_Search_lat, temp_Search_lon);
                            break;
                        }
                    }
                }
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
    private void adapterListPlace() {
        route = new ArrayList<Text>();
        namePlaces = new ArrayList<String>();
        arrayAdapter = new StableArrayAdapter(this, namePlaces);
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
