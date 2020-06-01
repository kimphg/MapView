package com.SeaMap.myapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.GlobalDataManager;
import com.SeaMap.myapplication.classes.MapPoint;
import com.google.android.material.textfield.TextInputEditText;


public class MapPointEditor extends AppCompatActivity {
    String mapPointname;
    MapPoint newPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_input);
        mapPointname = (getIntent().getExtras().getString("MapPoint"));
        newPoint= GlobalDataManager.getMapPoint(mapPointname);
        RadioGroup type = findViewById(R.id.radio_type);
//        TextView OldName = findViewById(R.id.textView1);
        TextInputEditText textInput = findViewById(R.id.text_input_name_map_point);
        textInput.setText(mapPointname);
//        OldName.setText(mapPointname);
        if(newPoint.mType<4)
            type.check(newPoint.mType);
    }
    public void OnAccept(View view)
    {
//        newPoint = new
//        mapPointname="";
        this.finish();
    }
    public void OnCancel(View view)
    {
        this.finish();
        return;
    }
}
