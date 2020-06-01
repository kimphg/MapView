package com.SeaMap.myapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.SeaMap.myapplication.R;


public class TextInput extends AppCompatActivity {
    String mapPointname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_input);
        mapPointname = (getIntent().getExtras().getString("MapPoint"));
    }
    public void OnAccept(View view)
    {
        mapPointname="";
        this.finish();
    }
    public void OnCancel(View view)
    {
        return;
    }
}
