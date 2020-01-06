package com.example.myapplication.classes;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StableArrayAdapter extends ArrayAdapter<String> {

    final int INVALID_ID = -1;

    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    public StableArrayAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
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
    public boolean hasStableIds() {
        return true;
    }
}
