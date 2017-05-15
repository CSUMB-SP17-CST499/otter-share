package com.ottershare.ottershare;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ryan on 5/7/17.
 */

public class PassAdapter extends ArrayAdapter<ParkingPassInfo>{

    public PassAdapter(Context context, ArrayList<ParkingPassInfo> passes) {
        super(context, 0,passes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ParkingPassInfo pass = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_pass,parent,false);
        }

        TextView userTv = (TextView) convertView.findViewById(R.id.user_tv);
        TextView priceTv = (TextView) convertView.findViewById(R.id.price_tv);
        TextView ratingTv = (TextView) convertView.findViewById(R.id.user_rating_tv);
        TextView lotTv = (TextView) convertView.findViewById(R.id.lot_tv);

        userTv.setText(pass.getEmail());
        priceTv.setText(Float.toString(pass.getPrice()));
        ratingTv.setText(Float.toString(pass.getRating()));
        lotTv.setText(Integer.toString(pass.getLotLocation()));
        lotTv.setText(Integer.toString(pass.getLotLocation()));

        return convertView;

    }

}
