package com.SeaMap.myapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
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
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.Coordinate;
import com.SeaMap.myapplication.classes.Places;
import com.SeaMap.myapplication.classes.ReadFile;
import com.SeaMap.myapplication.classes.Route;
import com.SeaMap.myapplication.classes.StableArrayAdapter;
import com.SeaMap.myapplication.object.Text;
import com.SeaMap.myapplication.services.GpsService;
import com.SeaMap.myapplication.view.DistancePTPView;
import com.SeaMap.myapplication.view.PolygonsView;
import com.SeaMap.myapplication.view.SeaMap;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    //Todo: dung dinh danh moi request
    public static final int REQUEST_INPUT = 1001;
    public static final int DISTANCE = 1002;
    public static final int ROUTE = 1003;
    public static final int REQUEST_CODE = 1004;

    //Todo: khi click
    public static int CHOOSE_BTN_LAYERS = 0;
    public static int CHOOSE_DISTANE_OR_ROUTE = 0;
    //-1: reset 0: search 1:direction
    public static int CHOOSE_SEARCH_OR_DIRECTION = -1;
    //
    private int onViewMain = 0;
    private int REQUEST_SEARCH = 0;

    //Khi an 1 keo dai khung hien thi route
    //Khi an 2 tro ve vi tri ban dau
    //Khi an 0 thu nho khung hien thi route
    //up: 1| down: 0 | 2: reset
    private int up_down_reset_route = 0;

    //lay vi tri hien tai
    private float longitude = 0, latitude = 0;
    private String lonlat[];

    //Todo: cac view hien thi
    public static DistancePTPView distancePTPView;
    public PolygonsView map;
    private NavigationView navigationView;

    private SearchView searchView;

    //Todo: Doc du lieu dau vao
    private ReadFile read;

    private Text textSearch;
    private Places places;

    //Todo: Cac nut an va nhan su kien
    private ImageButton getCurLocationButton, imageButtonOther, imageBtnSearch, imageButtonDirection, imageBtnUp_Of_Route, addDestinationButton, imageBtnLayer, imageBtnBackRoute;
    private GoogleApiClient googleApiClient;

    //Todo : layout main va layout route
    private FrameLayout frameLayout_main, frameLayout_map;
    private RelativeLayout route_layout, route_result_layout;
    private CoordinatorLayout coordinatorLayout_second;
    private EditText first_Point, second_Point;
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
    //system variables
    private BroadcastReceiver broadcastReceiver;
    private TextView curLocationText;
    private TextView curVelocityText;
    private TextView curBearingText;

    private Route curRoute;
    int etaToNextDestination = -1;
    boolean arrived;
    boolean isCalculating = false;
    private TextView toNextDestinationDistanceText;
    private TextView etaToNextDestinationText;

    //todo: thong so khac
    private float temp_Search_lon = 0, temp_Search_lat = 0;
    private int heightScr, widthScr, heightScrUse;
    OrientationEventListener myOrientationEventListener;

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void StartLocationService() {
        if (!isMyServiceRunning(GpsService.class)) {
            Intent intent = new Intent(getApplicationContext(), GpsService.class);

            startService(intent);

        }
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    List<Location> nearbyShips = new ArrayList<Location>();
    double speedKmh = 0;
    @Override
    protected void onResume() {
        super.onResume();

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Location newLocation = intent.getParcelableExtra("newLocation");

                    if (newLocation != null) {
                        String[] dms;
                        dms = Coordinate.decimalToDMS(newLocation.getLongitude(), newLocation.getLatitude());
                        String dmsCoord = dms[0] + "/" + dms[1];
                        curLocationText.setText(dmsCoord);
                        speedKmh+= (newLocation.getSpeed() * 3600.0 / 1000.0-speedKmh)/3.0;
                        curVelocityText.setText(String.valueOf((int) speedKmh));
                        if (curLocation != null) {
                            curBearingText.setText(String.valueOf((int) newLocation.getBearing()));
                        }
                        curLocation = newLocation;
                        map.setLonLatMyLocation(
                                curLocation.getLatitude(),
                                curLocation.getLongitude(), false
                        );
//                        Toast.makeText(MainActivity.this, curLocation.getLatitude() + " , " + curLocation.getLongitude(), Toast.LENGTH_LONG).show();
                    }
                    if ((nearbyShips != null) && (!nearbyShips.isEmpty())) {
                        map.setNearbyShips(nearbyShips);
                        nearbyShips.clear();
                    }
                    for (int i = 0; i < 10; i++) {
                        Location ship = intent.getParcelableExtra("nearbyShips" + Integer.toString(i));
                        if (ship != null)
                            nearbyShips.add(ship);
                    }

                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        frameLayout_main = findViewById(R.id.content_frame);
        frameLayout_map = findViewById(R.id.frame_layout_map);
        read = new ReadFile(getApplicationContext());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                read.readDensity();
            }
        });
        // su kien bat dau lo trinh

        route_result_layout = findViewById(R.id.layout_route_result);
        coordinatorLayout_second = findViewById(R.id.layout_second_route_result);
        first_Point = findViewById(R.id.edit_text_first_point);
        second_Point = findViewById(R.id.edit_text_second_point);
        imageBtnBackRoute = findViewById(R.id.back_route);

        //setting map;
        map = new SeaMap(getApplicationContext());
        frameLayout_map.addView(map, 0);

        curLocationText = findViewById(R.id.curLocationText);
        curVelocityText = findViewById(R.id.curVelocityText);
        curBearingText = findViewById(R.id.curBearingText);

        toNextDestinationDistanceText = findViewById(R.id._distance);
        etaToNextDestinationText = findViewById(R.id._timeRun);
        ///
        if (!checkPermission()) {
            requestPermission();
            enableButtons();
        } else {
            enableButtons();
            StartLocationService();
        }

        //Lay thong so man hinh
        getDisplayMetrics();
        //
        onDistancePTPView();
        navigationDrawer();
        onRoute();
        onDensityView();
        onScreecBtn_Direction_Search();
        //enableButtons();

    }

    //Todo: init and add onclick for button on screen
    boolean fixScreenToLocation = false;
    private void enableButtons() {
        //enable buttons' functions
        getCurLocationButton = findViewById(R.id.get_curlocation_btn);
        getCurLocationButton.setOnClickListener(view -> {
            StartLocationService();

            if (curLocation != null) {
                map.setLonLatMyLocation(
                        Float.parseFloat(Double.toString(curLocation.getLatitude())),
                        Float.parseFloat(Double.toString(curLocation.getLongitude())), true
                );

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
                if(REQUEST_SEARCH == 1){
                    if(CHOOSE_SEARCH_OR_DIRECTION == 0) {
                        //float
                        //Khong can vi tri vi da biet vi tri search trc do
                        map.myLocationToDirection(0, 0, 0);
                        String name_first_Point = first_Point.getText().toString();
                        second_Point.setText(name_first_Point);
                        CHOOSE_SEARCH_OR_DIRECTION = 1;
                        setSearch_Route_result_layout("Vị trí của tôi", name_first_Point);
                    }
                }
                else {
                        CHOOSE_SEARCH_OR_DIRECTION = 1;
                        Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                        startActivityForResult(searchIntent, REQUEST_INPUT);
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
        changeLocationuttonWhenRoute();
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
                //map.setLonLatSearchPlace(coor[1], coor[0]);

                Coordinate selected = new Coordinate(coor[0], coor[1]);
                curRoute.addNewDestination(selected);

                if (!isCalculating) {
                    isCalculating = true;
                    runEtaTimer();
                }
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

                curRoute.route.remove(i);

                arrayAdapter.notifyDataSetChanged();
                // thiet lap lai va ve
                distancePTPView.setListCoor(route);
                distancePTPView.invalidate();
                map.mapOutdated = true;
                runEtaTimer();
            }
        });


        // phong to thu nho route
        imageBtnUp_Of_Route = findViewById(R.id.button_up);
        imageBtnUp_Of_Route.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (up_down_reset_route) {
                    //0: phong to
                    case 0: {
                        route_layout.getLayoutParams().height = heightScrUse * 5 / 8;
                        route_layout.requestLayout();
                        up_down_reset_route = 1;
                        break;
                    }
                    //1: thu nho
                    case 1: {
                        route_layout.getLayoutParams().height = heightScrUse / 8;
                        route_layout.requestLayout();
                        up_down_reset_route = 2;
                        break;
                    }
                    //2:reset
                    case 2:{
                        route_layout.getLayoutParams().height = heightScrUse * 2 / 5;
                        route_layout.requestLayout();
                        up_down_reset_route = 0;
                        break;
                    }
                }
            }

        });
    }

    // Todo -------------- -----------------------//
    private void onDistancePTPView() {
        addDestinationButton = findViewById(R.id.icon_addDirect);
        addDestinationButton.setVisibility(View.INVISIBLE);

        addDestinationButton.setOnClickListener(v -> {
            switch (CHOOSE_DISTANE_OR_ROUTE) {
                case DISTANCE: {
                    break;
                }
                case ROUTE: {
                    PointF p = map.ConvScrPointToWGS(map.scrCtX, map.scrCtY);
                    Coordinate selected = new Coordinate(p.x, p.y);
                    this.curRoute.addNewDestination(selected);
                    if (!isCalculating) {
                        isCalculating = true;
                        runEtaTimer();
                    }
                    String[] selectedStr = Coordinate.decimalToDMS(selected.longitude, selected.latitude);

                    String name_place = selectedStr[0] + " / " + selectedStr[1];
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
        });
        addDestinationButton.setVisibility(View.INVISIBLE);
    }


    //Todo : Khoi tao density va hien view khi an layer
    public void onDensityView() {
        imageBtnLayer = findViewById(R.id.ic_btn_layers);
        imageBtnLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (CHOOSE_BTN_LAYERS) {
                    case 0: {
                        map.isShowInfo = true;
                        CHOOSE_BTN_LAYERS = 1;
                        break;
                    }
                    case 1: {
                        map.isShowInfo = false;
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

                        curRoute = new Route();

                        distancePTPView = new DistancePTPView(getApplicationContext(), map);

                        frameLayout_map.addView(distancePTPView);
                        route_layout.setVisibility(View.VISIBLE);
                        imageButtonOther.setBackgroundResource(R.drawable.icon_back);
                        addDestinationButton.setVisibility(View.VISIBLE);
                        break;
                    }
                    case R.id.nav_tinhkhoangcach: {
                        CHOOSE_DISTANE_OR_ROUTE = DISTANCE;
                        onViewMain = 1;
                        distancePTPView = new DistancePTPView(getApplicationContext(), map);
                        frameLayout_map.addView(distancePTPView);
                        imageButtonOther.setBackgroundResource(R.drawable.icon_back);
                        addDestinationButton.setVisibility(View.VISIBLE);


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
                        addDestinationButton.setVisibility(View.INVISIBLE);
                        navigationView.getCheckedItem().setChecked(false);
                        frameLayout_map.removeView(distancePTPView);
                        distancePTPView = null;
                        route_layout.setVisibility(View.INVISIBLE);
                        route_layout.getLayoutParams().height = heightScrUse * 2 / 5;
                        route_layout.requestLayout();

                        //dat lai gia tri ban dau
                        map.mapOutdated = true;
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

    private void runEtaTimer() {
        final Handler calculateHandler = new Handler();
        final Handler countdownHandler = new Handler();

        calculateHandler.post(new Runnable() {
            @Override
            public void run() {
                if (curLocation != null && !curRoute.route.isEmpty() ) {
                    if (curRoute.isArrived(curLocation)) {
                        arrived = true;
                        toNextDestinationDistanceText
                                .setText("Khoảng cách đến điểm tiếp theo:");
                        etaToNextDestinationText
                                .setText("Thời gian đến điểm tiếp theo:\tĐã đến nơi");
                    } else {
                        if (arrived) {
                            curRoute.arrivedToDestination();
                            arrived = false;
                        }
                        toNextDestinationDistanceText
                                .setText("Khoảng cách đến điểm tiếp theo:\t" +
                                        (int) curRoute.getNextDestinationDistance(curLocation) +
                                        " km");
                        etaToNextDestination = curRoute.getNextDestinationEta(curLocation);
                        if (etaToNextDestination == -1) {
                            etaToNextDestinationText
                                    .setText("Thời gian đến điểm tiếp theo:\tKhông di chuyển !");
                        } else if (etaToNextDestination == -2) {
                            etaToNextDestinationText
                                    .setText("Đã hoàn thành hải trình !");
                        }
                    }

                }
                else{
                    toNextDestinationDistanceText
                            .setText("Khoảng cách đến điểm tiếp theo:");
                    etaToNextDestinationText
                            .setText("Thời gian đến điểm tiếp theo:");
                    isCalculating = false;
                    calculateHandler.removeCallbacks(this);

                }

                calculateHandler.postDelayed(this, 5000);
            }
        });

        countdownHandler.post(new Runnable() {
            @Override
            public void run() {
                if (etaToNextDestination > 0) {
                    int hour = etaToNextDestination / 3600;
                    int minute = (etaToNextDestination % 3600) / 60;
                    int sec = etaToNextDestination % 60;

                    etaToNextDestinationText
                            .setText("Thời gian đến điểm tiếp theo:\t" +
                                    hour + " giờ " + minute + " phút " + sec + " giây");

                    etaToNextDestination--;
                }

                countdownHandler.postDelayed( this, 1000);
            }
        });
    }

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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED);
        } else {
            Snackbar.make(map, "Vui lòng cập nhật phiên bản mới của hệ điều hành", Snackbar.LENGTH_LONG).show();
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE);
    }

    private void showMessageBox(String message, DialogInterface.OnClickListener clickListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", clickListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (accepted) {
                    //Snackbar.make( map, "Cho phép sử dụng vị trí", Snackbar.LENGTH_LONG).show();
                    StartLocationService();
                    enableButtons();
                } else {
                    //Snackbar.make( map, "Không có quyền sử dụng vị trí", Snackbar.LENGTH_LONG).show();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showMessageBox("Bạn cần cho phép ứng dụng sử dụng vị trí",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                    REQUEST_CODE);
                                        }
                                    });
                            return;
                        } else {
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

                switch (CHOOSE_SEARCH_OR_DIRECTION) {
                    case 0: {
                        map.setLonLatSearchPlace(mlat, mlon);
                        enable_disable_Layout_Search_Route();
                        setSearch_Route_result_layout(textSearch.getName(), null);
                        break;
                    }
                    case 1: {
                        setSearch_Route_result_layout("Vị trí của tôi", textSearch.getName());
                        map.myLocationToDirection(1, mlat, mlon);
                        break;
                    }
                }

            } else {
                // DetailActivity không thành công, không có data trả về.
                if (temp_Search_lon != 0 && temp_Search_lat != 0) {
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

    //Ham lay kich thuoc man hinh
    private void getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightScr = displayMetrics.heightPixels;
        widthScr = displayMetrics.widthPixels;
        heightScrUse = heightScr;
    }



    public void changeLocationuttonWhenRoute(){
        route_layout.getLayoutParams().height = heightScrUse * 2 / 5;
        route_layout.requestLayout();
    }

    //Nhận sự kiện thay đổi góc nghiêng điện thoại
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            heightScrUse = widthScr;
            changeLocationuttonWhenRoute();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            heightScrUse = heightScr;
            changeLocationuttonWhenRoute();
        }
    }

    private void enable_disable_Layout_Search_Route(){
        if(CHOOSE_SEARCH_OR_DIRECTION == 1 || CHOOSE_SEARCH_OR_DIRECTION == 0){
            frameLayout_main.setVisibility(View.INVISIBLE);
            route_result_layout.setVisibility(View.VISIBLE);
        }
        else {
            frameLayout_main.setVisibility(View.VISIBLE);
            route_result_layout.setVisibility(View.INVISIBLE);
            first_Point.setText("");
            //reset Map
            map.disableSearch_Direction(0);
        }
    }

    private void setSearch_Route_result_layout(String name_first_Point, String name_second_Point){

        if (name_second_Point == null) {
            first_Point.setText(name_first_Point);
        }
        else {
            first_Point.setText(name_first_Point);
            second_Point.setText(name_second_Point);
            coordinatorLayout_second.setVisibility(View.VISIBLE);

        }
    }

    public void onBackSearch_Route(View view){
        if(CHOOSE_SEARCH_OR_DIRECTION == 1){
            String name_first_Point = second_Point.getText().toString();
            first_Point.setText(name_first_Point);
            second_Point.setText("");
            coordinatorLayout_second.setVisibility(View.INVISIBLE);
            CHOOSE_SEARCH_OR_DIRECTION = 0;
            REQUEST_SEARCH = 1;
            map.disableSearch_Direction(1);
        }
        else if(CHOOSE_SEARCH_OR_DIRECTION == 0){
            CHOOSE_SEARCH_OR_DIRECTION  = -1;
            enable_disable_Layout_Search_Route();
            map.disableSearch_Direction(0);
            REQUEST_SEARCH = 0;
            coordinatorLayout_second.setVisibility(View.INVISIBLE);
        }
    }
}
