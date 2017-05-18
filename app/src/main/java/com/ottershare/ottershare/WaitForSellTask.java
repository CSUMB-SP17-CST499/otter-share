package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;

/**
 * Created by ryan on 5/14/17.
 */

public class WaitForSellTask extends AsyncTask<String,String,Integer>{

    /* Context being passed in from main thread*/
    Context context;
    Activity prevActivity;
    private String LOG_TAG =  WaitForSell.class.getSimpleName();


    public WaitForSellTask(Activity activity){
        prevActivity = activity;
        this.context = activity.getApplicationContext();
        SharedPreferences sharedPrefrences;

    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        String response = "";

        String emailInput = params[0];
        String passInput = params[1];

        try {



        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    /*available responses
    0 : Buyer not found
    1 : Buyer Found
    2 : unverified api
    anything else : error
 */
    @Override
    protected void onPostExecute(Integer result){

    }
}
