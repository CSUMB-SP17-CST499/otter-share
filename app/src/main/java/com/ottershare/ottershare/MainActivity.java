package com.ottershare.ottershare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;


public class MainActivity extends FragmentActivity{

    FragmentManager fragManager;
    MapOSFragment frag;
    ImageView sellPassBtn;
    View.OnClickListener listener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.sell_pass_btn:
                    Intent intent = new Intent(MainActivity.this, ParkingActivity.class);
                    MainActivity.this.startActivity(intent);
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

        sellPassBtn = (ImageView) findViewById(R.id.sell_pass_btn);
        sellPassBtn.setOnClickListener(listener);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        fragManager = getSupportFragmentManager();
        frag = (MapOSFragment)fragManager.findFragmentById(R.id.map);

        frag.changeCameraLocation(Double.parseDouble(getString(R.string.all_csumb_lat)),Double.parseDouble(getString(R.string.all_csumb_lon)),Float.parseFloat(getString(R.string.all_csumb_zoom)));
    }


}