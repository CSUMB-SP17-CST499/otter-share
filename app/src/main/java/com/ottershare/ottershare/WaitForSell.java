package com.ottershare.ottershare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class WaitForSell extends AppCompatActivity {

    Button btnRejectBuyer;
    Button btnAcceptBuyer;
    ImageButton exitBtn;
    final String DEFAULT_RESPONSE = "empty";
    private String LOG_TAG =  WaitForSell.class.getSimpleName();
    String apikey;
    String passId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_sell);

        btnRejectBuyer = (Button) findViewById(R.id.cancel_sell_btn);
        btnAcceptBuyer = (Button) findViewById(R.id.accept_sell_btn);

        final Context context = this.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        apikey = prefs.getString(context.getString(R.string.os_apikey), DEFAULT_RESPONSE);
        passId = prefs.getString(context.getString(R.string.os_pass_id), DEFAULT_RESPONSE);

        //start the looping with the eventlistener. and show certain UI when node holds "decision"
        WaitForSellListener waitForSellListener = new WaitForSellListener(this, apikey, passId);
        waitForSellListener.startEvent();

        btnAcceptBuyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runWaitForSellTask("accept");
            }
        });

        btnRejectBuyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runWaitForSellTask("reject");
            }
        });
        final Intent i = new Intent(this, MainActivity.class);
        exitBtn = (ImageButton) findViewById(R.id.cancel_sell_upper_btn);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSellingStatus();
                startActivity(i);
                finish();
            }
        });
    }

    private void runWaitForSellTask(String action) {
        WaitForSellTask waitForSellTask = new WaitForSellTask(this);
        waitForSellTask.execute(apikey, passId, action);
    }

    @Override
    public void onBackPressed() {
        //do nothing!!!
    }

    private void clearSellingStatus() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.os_pass_selling_status), false);
        editor.apply();
    }
}
