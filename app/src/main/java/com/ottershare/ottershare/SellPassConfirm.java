package com.ottershare.ottershare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

public class SellPassConfirm extends AppCompatActivity {

    Button confirmBtn;
    Button cancelBtn;
    CircleFrameWithFade circleFrameWithFade;
    FragmentManager fragmentManagerFrame;
    FragmentManager fragmentManagerMap;
    MapOSFragment frag;
    SharedPreferences prefs;
    LatLng passLocation;
    String LOG_TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_pass_confirm);

        confirmBtn = (Button) findViewById(R.id.wait_for_sell_accept_btn);
        confirmBtn.setOnClickListener(listener);

        cancelBtn = (Button) findViewById(R.id.wait_for_sell_cancel_btn);
        cancelBtn.setOnClickListener(listener);
        prefs = SellPassConfirm.this.getApplicationContext().getSharedPreferences(SellPassConfirm.this.getApplicationContext().getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
    }

    View.OnClickListener listener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent i;
            switch (v.getId())
            {
                case R.id.wait_for_sell_accept_btn:
                    storeSellingStatus(true);
                    i = new Intent(SellPassConfirm.this, WaitForSell.class);
                    startActivity(i);
                    finish();
                    break;
                case R.id.wait_for_sell_cancel_btn:
                    storeSellingStatus(false);
                    /*i = new Intent(SellPassConfirm.this, MainActivity.class);
                    startActivity(i);
                    finish();*/
                    SellPassConfirm.super.onBackPressed();
                    finish();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        fragmentManagerFrame = getSupportFragmentManager();
        fragmentManagerMap = getSupportFragmentManager();

        circleFrameWithFade = (CircleFrameWithFade) fragmentManagerFrame.findFragmentById(R.id.sell_pass_circle_frame);
        frag = (MapOSFragment) fragmentManagerMap.findFragmentById(R.id.sell_pass_confirm_parking_map);


        //getting the location of current pass.
        try{
            String[] tempPassLocation = prefs.getString(this.getString(R.string.os_pass_geolocation), "No location").split(",");
            passLocation = new LatLng(Double.parseDouble(tempPassLocation[0]),Double.parseDouble(tempPassLocation[1]));
            frag.changeCameraLocation(passLocation.latitude, passLocation.longitude ,Float.parseFloat(getString(R.string.all_csumb_zoom)));
            frag.makeMarker(passLocation.latitude, passLocation.longitude);
            circleFrameWithFade.fadeOutAnimation();

        }catch (NumberFormatException e){
            Log.i(LOG_TAG, "lat/lng improper format");
        }
    }

    private void storeSellingStatus(boolean value) {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.os_pass_selling_status), value);
        editor.apply();
    }
}
