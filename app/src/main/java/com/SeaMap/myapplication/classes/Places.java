package com.SeaMap.myapplication.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.object.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Places extends BaseAdapter{

    Context context;
    LayoutInflater inflter;
    List<Text> mData;
    ArrayList<Text> temp;
    public Places(Context context, List<Text> list){
        super();
        this.context = context;
        //mData = getNameFromText(list);
        //places = list;
        mData = list;
        inflter = LayoutInflater.from(context);
        temp = new ArrayList<Text>();
        temp.addAll(mData);
    }

    public List<String> getNameFromText(List<Text> list){
        List<String> listName = new ArrayList<>();
        for(Text t:list){
            listName.add(t.getName());
        }
        return listName;
    }

    public class ViewHolder {
        TextView name;
    }

    // tra ve so dong cua list
    @Override
    public int getCount() {
        return mData.size();
    }

    //tra ve doi tuong dua vao vi tri i
    @Override
    public Text getItem(int i) {
        return mData.get(i);
    }

    //tra ve vi tri id dua vaof vi tri i
    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflter.inflate(R.layout.search_view, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.text_search);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(mData.get(i).getName());
        return view;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mData.clear();
        if (charText.length() == 0) {
            mData.addAll(temp);
        } else {
            for (Text s : temp) {
                if (s.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    mData.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }
}
