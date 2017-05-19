package com.ottershare.ottershare;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class WaitForBuyer extends AppCompatActivity {

    String apikey;
    String passId;

    final String DEFAULT_RESPONSE = "empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_buyer);

        final Context context = this.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        apikey = prefs.getString(context.getString(R.string.os_apikey), DEFAULT_RESPONSE);
        passId = prefs.getString(context.getString(R.string.os_pass_buying_id), DEFAULT_RESPONSE);
        //start the looping with the eventlistener. and show certain UI when node holds "decision"
        WaitForBuyerListener waitForBuyerListener = new WaitForBuyerListener(this, apikey, passId);
        waitForBuyerListener.startEvent();
    }
}
