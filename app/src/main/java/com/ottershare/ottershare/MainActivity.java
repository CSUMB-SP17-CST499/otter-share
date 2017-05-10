package com.ottershare.ottershare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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

    final String DEFAULT_API_KEY = "empty";

    View.OnClickListener listener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.register_pass_btn:
                    Intent intent = new Intent(MainActivity.this, ParkingActivity.class);
                    MainActivity.this.startActivity(intent);
                    break;
                case R.id.sell_pass_btn:

                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regPassBtn = (ImageView) findViewById(R.id.register_pass_btn);
        regPassBtn.setOnClickListener(listener);

        sellPassBtn = (ImageView) findViewById(R.id.sell_pass_btn);
        regPassBtn.setOnClickListener(listener);

        topLotList = (ListView) findViewById(R.id.top_pannel_list_view);
       // bottomPassList = (ListView) findViewById(R.id.bottom_pannel_list_view);

        //runParkingTask();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        fragManager = getSupportFragmentManager();
        frag = (MapOSFragment)fragManager.findFragmentById(R.id.map);
        frag.changeCameraLocation(Double.parseDouble(getString(R.string.all_csumb_lat)),Double.parseDouble(getString(R.string.all_csumb_lon)),Float.parseFloat(getString(R.string.all_csumb_zoom)));
        ArrayList<LatLng> locations = new ArrayList<LatLng>();

        runMainTask();

        /*
        todo: there should eventualy be a function to get all of the points from the backend and store it into a array list and then pass it to the below function
        frag.addHeatMap(locations);
        */
    }

    private void runMainTask() {
        Context context = MainActivity.this.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        String apikey = prefs.getString(context.getString(R.string.os_apikey), DEFAULT_API_KEY);
        String keyword = "all";


        MainTask mainTask = new MainTask(this , frag);
        mainTask.execute(apikey, keyword);
        sellPassBtn.setVisibility(View.GONE);
    }

    // delete this after testing.
}
