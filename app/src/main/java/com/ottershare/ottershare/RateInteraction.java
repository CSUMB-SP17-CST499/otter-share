package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class RateInteraction extends AppCompatActivity {

    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_interaction);

        //submitBtn = (Button) findViewById(R.id.)
        SharedPreferences prefs = getSharedPreferences(getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        String email = prefs.getString(getString(R.string.os_pass_buying_email), "empty");
    }
}
