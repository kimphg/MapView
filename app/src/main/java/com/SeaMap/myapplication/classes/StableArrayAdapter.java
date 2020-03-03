package com.SeaMap.myapplication.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.SeaMap.myapplication.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StableArrayAdapter extends ArrayAdapter<String> {

    final int INVALID_ID = -1;
    private final Context context;

    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    public StableArrayAdapter(Context context, List<String> objects) {
        super(context,-1, objects);
        this.context = context;

        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
    }

    public void setAdapter(ArrayList<String> mList){
        mIdMap.clear();
        for (int i = 0; i < mList.size(); ++i) {
            mIdMap.put(mList.get(i), i);
        }
    }

    public void addPlaceToAdapter(String namePlace){
        int i = mIdMap.size();
        mIdMap.put(namePlace, i + 1);

    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }
        String item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.places_view, parent, false);
        TextView count_Plc_txtView = (TextView) rowView.findViewById(R.id.count_plc);
        TextView name_Plc_txtView = (TextView) rowView.findViewById(R.id.txt_namePlace);

        //dat ten cua place ung vs position
        String item = getItem(position);
        name_Plc_txtView.setText(item);
        count_Plc_txtView.setText(position + 1 + ".");
        return rowView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
