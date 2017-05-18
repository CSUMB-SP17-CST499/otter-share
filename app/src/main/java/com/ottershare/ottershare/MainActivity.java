package com.ottershare.ottershare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity{

    FragmentManager fragManager;
    MapOSFragment frag;
    ImageView regPassBtn;
    ImageView sellPassBtn;
    ArrayList <ParkingPassInfo> testdataarray = new ArrayList<>();
    ListView topLotList;
    ListView bottomPassList;

    final String DEFAULT_RESPONSE = "empty";

    View.OnClickListener listener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent i;
            switch (v.getId())
            {
                case R.id.register_pass_btn:
                    i = new Intent(MainActivity.this, ParkingActivity.class);
                    startActivity(i);
                    break;
                case R.id.sell_pass_btn:
                    i = new Intent(MainActivity.this, SellPassConfirm.class);
                    startActivity(i);
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = this.getSharedPreferences(this.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);

        sellPassBtn = (ImageView) findViewById(R.id.sell_pass_btn);
        regPassBtn = (ImageView) findViewById(R.id.register_pass_btn);

        String passStatus = prefs.getString(this.getString(R.string.os_pass_status), DEFAULT_RESPONSE);
        if (passStatus.equals("registered")) {
            sellPassBtn.setOnClickListener(listener);
            sellPassBtn.setVisibility(View.VISIBLE);
            regPassBtn.setVisibility(View.GONE);
        } else {
            sellPassBtn.setVisibility(View.GONE);
            regPassBtn.setVisibility(View.VISIBLE);
            regPassBtn.setOnClickListener(listener);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        fragManager = getSupportFragmentManager();
        frag = (MapOSFragment)fragManager.findFragmentById(R.id.map);
        frag.changeCameraLocation(Double.parseDouble(getString(R.string.all_csumb_lat)),Double.parseDouble(getString(R.string.all_csumb_lon)),Float.parseFloat(getString(R.string.all_csumb_zoom)));
        ArrayList<LatLng> locations = new ArrayList<LatLng>();

        runMainTask();
        runWaitForBuyerTask();

        /*
        todo: there should eventualy be a function to get all of the points from the backend and store it into a array list and then pass it to the below function
        frag.addHeatMap(locations);
        */
    }

    private void runMainTask() {
        Context context = MainActivity.this.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        String apikey = prefs.getString(context.getString(R.string.os_apikey), DEFAULT_RESPONSE);
        String keyword = "all";

        MainTask mainTask = new MainTask(this, frag);
        mainTask.execute(apikey, keyword);

    }

    private void runWaitForBuyerTask() {
        Context context = MainActivity.this.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        String apikey = prefs.getString(context.getString(R.string.os_apikey), DEFAULT_RESPONSE);
        String passId = "SyXJvhcg-";
        String requestCount = "1";


        WaitForBuyerTask waitForBuyerTask = new WaitForBuyerTask(this);
        waitForBuyerTask.execute(apikey, passId , requestCount);

    }

    @Override
    public void onBackPressed() {
        //do nothing!!!
    }
}
