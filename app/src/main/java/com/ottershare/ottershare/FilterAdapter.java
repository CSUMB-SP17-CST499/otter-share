package com.ottershare.ottershare;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ryan on 5/7/17.
 */

public class FilterAdapter extends ArrayAdapter<String>{

    ArrayList<String> list;
    public FilterAdapter(Context context, ArrayList<String> filterList) {
        super(context, 0, filterList);
        list = filterList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String filter = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.filter_item_textview, parent,false);
        }

        TextView filterText = (TextView) convertView.findViewById(R.id.top_pannel_filter_item);

        filterText.setText(filter);

        return convertView;

    }

}
