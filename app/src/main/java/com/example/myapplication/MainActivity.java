package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import java.lang.Object;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapView map ;

        setContentView(R.layout.activity_mapview);
        //onClick1();
        //addButton();
    }

    public void addButton (){
        LinearLayout parent = findViewById(R.id.parent);
        //MapView map = new MapView(this);
        //parent.addView(map,0);
        Button button = new Button(this);
        button.setText("click me");
        button.setHeight(46);
        button.setWidth(230);
        parent.addView(button);
        setContentView(parent);
    }
    public void getLocation(){
        final FloatingActionButton fab = findViewById(R.id.fab1);

        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

            }
        });
    }
}
