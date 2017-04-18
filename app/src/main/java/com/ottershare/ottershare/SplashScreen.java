package com.ottershare.ottershare;

/*
    Created by: bchehraz
    Name: Splash Screen Activity
    Function: Used to displaying a splash screen
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;
    private final String LOG_TAG = SplashScreen.class.getSimpleName();
    final String DEFAULT_API_KEY = "empty";

    /**
     * TODO: Configure the code on this class to achieve 100% code coverage
     */

    /**
     * TODO: [CURRENT] Find out how to test a function that starts a new activity
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Context context = SplashScreen.this.getApplicationContext();
                SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);

                String apikey = prefs.getString(context.getString(R.string.os_apikey), DEFAULT_API_KEY);

                Class target = isValidApi(apikey) ? MainActivity.class : LoginActivity.class;
                //Class target = LoginActivity.class; //for testing, automatically go to LoginActivity
                Intent intent = new Intent(SplashScreen.this, target);

                startActivity(intent);
                finish();

                // new
                /*if (prefsContainsApiKey()) {

                }*/
            }
        }, SPLASH_TIME_OUT);
    }

    protected boolean isValidApi(String api) {
        return api.equals(DEFAULT_API_KEY) ? false : true;
    }

    protected boolean prefsContainsApiKey(SharedPreferences prefs) {
        return false;
    }

}
