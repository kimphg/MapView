package com.Seamap.app.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import com.Seamap.app.R;
import com.Seamap.app.classes.Places;
import com.Seamap.app.classes.ReadFile;
import com.Seamap.app.object.Text;

import java.io.Serializable;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    public static final String EXTRA_DATA = "EXTRA_DATA";
    ImageButton imgbtback;
    SearchView search;
    ListView listView;
    Places adapter;
    List list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        imgbtback = findViewById(R.id.back);
        search = findViewById(R.id.sv_place);
        listView = findViewById(R.id.listView);

        Intent intent = getIntent();
        list = ReadFile.ListPlace;

        adapter = new Places(this,list);
        listView.setAdapter(adapter);

        imgbtback.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        search.setOnQueryTextListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Text place = adapter.getItem(position);
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_DATA, (Serializable) place);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return false;
    }

    @Override
    public void onBackPressed() {

        // đặt resultCode là Activity.RESULT_CANCELED thể hiện
        // đã thất bại khi người dùng click vào nút Back.
        // Khi này sẽ không trả về data.
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

}