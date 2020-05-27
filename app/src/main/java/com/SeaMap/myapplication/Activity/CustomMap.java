package com.SeaMap.myapplication.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.view.HistoryMapView;

public class CustomMap extends AppCompatActivity {

    private HistoryMapView mMap;
    @Override
    protected void onResume(){
        super.onResume();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_map);
    }
}
