package com.SeaMap.myapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.GlobalDataManager;
import com.SeaMap.myapplication.classes.MapPointUser;
import com.google.android.material.textfield.TextInputEditText;


public class MapPointEditor extends AppCompatActivity {
    String mapPointname;
    MapPointUser newPoint;
    RadioGroup type;
    int[] radioId = new int[4];
    TextInputEditText textInputName,textInputLat,textInputLon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_input);
        mapPointname = (getIntent().getExtras().getString("MapPoint"));
        newPoint= GlobalDataManager.getMapPoint(mapPointname);
        type = findViewById(R.id.radio_type);
//        TextView OldName = findViewById(R.id.textView1);
        textInputName = findViewById(R.id.text_input_name_map_point);
        textInputLat = findViewById(R.id.text_input_lat_map_point);
        textInputLon = findViewById(R.id.text_input_lon_map_point);
        textInputName.setText(mapPointname);
        textInputLat.setText(String.valueOf( ( (int)(newPoint.mlat*10000) )/10000.0f ) );
        textInputLon.setText(String.valueOf( ( (int)(newPoint.mlon*10000) )/10000.0f ) );
        for (int i=0;i<4;i++)
        {
            radioId[i]=type.getChildAt(i).getId();
        }
        if(newPoint.mType>=0&&newPoint.mType<4)
            type.check(radioId[newPoint.mType]);
    }
    public void OnAccept(View view)
    {
        newPoint.mlon = Float.parseFloat(textInputLon.getText().toString());
        newPoint.mlat = Float.parseFloat(textInputLat.getText().toString());
        newPoint.mName = textInputName.getText().toString();
        if(GlobalDataManager.checkMapPointNameExist(newPoint.mName )>1)
        newPoint.mName = GlobalDataManager.GetUniqueMapPointName(newPoint.mName);
        for (int i=0;i<4;i++)
        {
            if(radioId[i]==type.getCheckedRadioButtonId()) {
                newPoint.mType = i;
                break;
            }
        }

        this.finish();
    }
    public void OnCancel(View view)
    {
        this.finish();
        return;
    }
    public void OnDelete(View view)
    {
        GlobalDataManager.removemapPoint(newPoint.mName);
        this.finish();
        return;
    }
}
