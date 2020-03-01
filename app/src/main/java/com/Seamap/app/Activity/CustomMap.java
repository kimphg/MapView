package com.Seamap.app.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.Seamap.app.R;
import com.Seamap.app.view.SeaMap;

public class CustomMap extends AppCompatActivity {

    private SeaMap mMap;
    @Override
    protected void onResume(){
        super.onResume();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_map);
        mMap = findViewById(R.id.map);
    }
}
