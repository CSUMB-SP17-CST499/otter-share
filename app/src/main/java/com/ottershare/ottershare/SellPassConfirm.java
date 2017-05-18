package com.ottershare.ottershare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SellPassConfirm extends AppCompatActivity {

    Button confirmBtn;
    Button cancelBtn;
    CircleFrameWithFade circleFrameWithFade;
    FragmentManager fragmentManagerFrame;
    FragmentManager fragmentManagerMap;
    MapOSFragment frag;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_pass_confirm);

        confirmBtn = (Button) findViewById(R.id.wait_for_sell_accept_btn);
        confirmBtn.setOnClickListener(listener);

        cancelBtn = (Button) findViewById(R.id.wait_for_sell_cancel_btn);
        cancelBtn.setOnClickListener(listener);
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
                    i = new Intent(SellPassConfirm.this, WaitForSell.class);
                    startActivity(i);
                    break;
                case R.id.wait_for_sell_cancel_btn:
                    i = new Intent(SellPassConfirm.this, MainActivity.class);
                    startActivity(i);
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
        frag.changeCameraLocation(Double.parseDouble(getString(R.string.all_csumb_lat)),Double.parseDouble(getString(R.string.all_csumb_lon)),Float.parseFloat(getString(R.string.all_csumb_zoom)));
        circleFrameWithFade.fadeOutAnimation();
    }
}
