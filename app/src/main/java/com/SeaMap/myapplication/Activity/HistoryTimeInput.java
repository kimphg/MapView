package com.SeaMap.myapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.GlobalDataManager;

public class HistoryTimeInput extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_time_input);
    }

    public void OnAccept(View view) {
        RadioGroup radGroup = findViewById(R.id.radio_time_period);
        String time;
        switch (radGroup.getCheckedRadioButtonId())
        {
            case R.id.radioButton_time_period_1h:
                time = String.valueOf(System.currentTimeMillis()/1000-3600L);
                GlobalDataManager.SetConfig("history_start",time);
                break;
            case R.id.radioButton_time_period_2h:
                time = String.valueOf(System.currentTimeMillis()/1000-2*3600L);
                GlobalDataManager.SetConfig("history_start",time);
                break;
            case R.id.radioButton_time_period_5h:
                time = String.valueOf(System.currentTimeMillis()/1000-5*3600L);
                GlobalDataManager.SetConfig("history_start",time);
                break;
            case R.id.radioButton_time_period_1d:
                time = String.valueOf(System.currentTimeMillis()/1000-24*3600L);
                GlobalDataManager.SetConfig("history_start",time);
                break;
            case R.id.radioButton_time_period_2d:
                time = String.valueOf(System.currentTimeMillis()/1000-48*3600L);
                GlobalDataManager.SetConfig("history_start",time);
                break;
            case R.id.radioButton_time_period_7d:
                time = String.valueOf(System.currentTimeMillis()/1000-7*24*3600L);
                GlobalDataManager.SetConfig("history_start",time);
                break;
            case R.id.radioButton_time_period_14d:
                time = String.valueOf(System.currentTimeMillis()/1000-14*24*3600L);
                GlobalDataManager.SetConfig("history_start",time);
                break;
            case R.id.radioButton_time_period_1m:
                time = String.valueOf(System.currentTimeMillis()/1000-30*24*3600L);
                GlobalDataManager.SetConfig("history_start",time);
                break;
            case R.id.radioButton_time_period_2m:
                time = String.valueOf(System.currentTimeMillis()/1000-60*24*3600L);
                GlobalDataManager.SetConfig("history_start",time);
                break;
            default:
                time = "0";
                GlobalDataManager.SetConfig("history_start",time);
                break;
        }
        finish();
    }
}
