package com.SeaMap.myapplication.function;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.MapCoordinate;
import com.SeaMap.myapplication.classes.Places;
import com.SeaMap.myapplication.classes.Route;
import com.SeaMap.myapplication.classes.StableArrayAdapter;
import com.SeaMap.myapplication.object.Text;
import com.SeaMap.myapplication.view.DynamicListView;
import com.SeaMap.myapplication.view.MapView;

import java.util.ArrayList;
import java.util.List;

public class f_setEventViewRoute implements SearchView.OnQueryTextListener{

    //Lay layout bao quat de tim id view
    FrameLayout contentFrame;
    //
    MapView map;
    Places places;
    Button startRoute;
    TextView _distance;
    SearchView searchView;
    ImageView addPlace;
    ImageButton imageButtonUp, imageButtonCancelRoute;

    float dYs, dYe;
    RelativeLayout routeLayout;

    //khai bao cho route
    Route route;
    ArrayList<String> mCheeseList;
    //listview search trong phan info_route;
    ListView listPlaceSeacrh;
    DynamicListView routesListView;
    List<Text> list;
    StableArrayAdapter adapter;
    Context mContext;
    boolean start = false;

    @SuppressLint("ClickableViewAccessibility")
    public f_setEventViewRoute(Context context, MapView mMap, FrameLayout mFrameLayout) {

        mContext = context;
        map = mMap;
        contentFrame = mFrameLayout;
        //---------------------..........................................//
        //Khai bao cac view
        routeLayout = contentFrame.findViewById(R.id.route_layout);
        listPlaceSeacrh = contentFrame.findViewById(R.id.listplace);
//        addPlace = contentFrame.findViewById(R.id.addplace);
        routesListView = contentFrame.findViewById(R.id.route_listview);
//        // su kien bat dau lo trinh
//        startRoute = contentFrame.findViewById(R.id.bt_startroute);
        _distance = contentFrame.findViewById(R.id._distance);
        //
        imageButtonUp = contentFrame.findViewById(R.id.button_up);
        //-----------------------------------------------------------//

        list = map.listPlace();

        //.............tao adpter.....
        places = new Places(mContext, list);
        listPlaceSeacrh.setAdapter(places);

        searchView =  contentFrame.findViewById(R.id.searchview_place);
        searchView.setOnQueryTextListener(this);

        //##### su kien an vao item cua listview: 1.route  2.search //
        route = new Route();
        mCheeseList = new ArrayList<String>();
        adapter = new StableArrayAdapter(mContext, R.layout.places_view,R.id.tx_namePlace, mCheeseList);
        routesListView.setAdapter(adapter);
        //1.search
        listPlaceSeacrh.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Text place = places.getItem(i);
                route.add(place);
                mCheeseList.add(place.getName());
                adapter.setAdapter(mCheeseList);
                adapter.notifyDataSetChanged();
            }
        });
        //2.route
        routesListView.setCheeseList(mCheeseList);
        routesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                route.remove(i);
                mCheeseList.remove(i);
                adapter.notifyDataSetChanged();
            }
        });
        //Su kien them mot dia diem
        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.choosePlacetoRoute();
                mCheeseList.clear();
                route = map.coordinateRoute();
                for (int i = 0; i < route.size(); i++) {
                    mCheeseList.add(route.get(i).getName());
                }
                adapter.setAdapter(mCheeseList);
                adapter.notifyDataSetChanged();
            }
        });
        //su kien cancel route quay ve ban dau
        startRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!start) {
                    double distanceKm = 0;
                    map.drawRoute(route);
                    MapCoordinate mC = new MapCoordinate();

                    double distanceNM = mC.convertKmToNm(distanceKm);

                    _distance.append((int) distanceNM + " NM");
                    startRoute.setText("Dung lo trinh");
                    start = true;
                } else {
                    startRoute.setText("Bat dau");
                    start = false;
                    map.canceldrawRoute();
                }

            }
        });


        //.....................................
        /// su kien di chuyen layout lo trinh //
        routeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    dYs = motionEvent.getY();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int height = map.getHeight();
                    dYe = motionEvent.getY();
                    routeLayout.getLayoutParams().height += (int) dYs - (int) dYe;
                    routeLayout.requestLayout();

                }
                return true;
            }
        });


    }


    public void cancelROute(){
        route.clear();
        adapter.clear();
        adapter.notifyDataSetChanged();
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
}
