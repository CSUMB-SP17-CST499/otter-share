package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ryan on 5/14/17.
 */

public class WaitForBuyerTask extends AsyncTask<String,String,Integer>{

    /* Context being passed in from main thread*/
    Context context;
    Activity prevActivity;
    private String LOG_TAG =  WaitForBuyerTask.class.getSimpleName();


    public WaitForBuyerTask(Activity activity){
        prevActivity = activity;
        this.context = activity.getApplicationContext();
        SharedPreferences sharedPrefrences;

    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        String response = "";

        String apiKey = params[0];
        String passId = params[1];
        String requestCount = params[2];

        try {
            final String BASE_URL = "https://young-plains-98404.herokuapp.com/";
            final String CALL = "buyerListener";
            final String API_KEY_PARAM = "api_key";
            final String PASS_ID_PARAM = "passId";
            final String REQUEST_COUNT_PARAM = "requestCount";

            URL url = new URL(BASE_URL + CALL);

            //set up http connection
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            HashMap<String, String> postDataParams = new HashMap<>();

            postDataParams.put(API_KEY_PARAM, apiKey);
            postDataParams.put(PASS_ID_PARAM, passId);
            postDataParams.put(REQUEST_COUNT_PARAM, requestCount);

            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            int responseCode = httpURLConnection.getResponseCode();

            Log.d(LOG_TAG, "Response code: " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                while ((line = br.readLine()) != null) {
                    response += line;
                    Log.d(LOG_TAG, response);
                }
            } else {
                return -1;
            }

            JSONObject jsonResponse = new JSONObject(response);
            //parse json

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

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
