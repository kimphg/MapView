package com.SeaMap.myapplication.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.fragment.app.FragmentActivity;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.Places;
import com.SeaMap.myapplication.classes.GlobalDataManager;
import com.SeaMap.myapplication.object.Text;

import java.io.Serializable;
import java.util.List;

public class SearchActivity extends FragmentActivity implements SearchView.OnQueryTextListener{

    public static final String EXTRA_DATA = "EXTRA_DATA";
    private ImageButton imgbtback;
    private SearchView search;
    private ListView listView;
    private Places adapter;
    private List list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        imgbtback = findViewById(R.id.back);
        search = findViewById(R.id.sv_place);
        listView = findViewById(R.id.listView);

        list = GlobalDataManager.getListPlaceOnText();

        adapter = new Places(this);
        listView.setAdapter(adapter);

        imgbtback.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                onBackPressed();
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
