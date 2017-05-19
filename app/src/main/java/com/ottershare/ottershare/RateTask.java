package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

public class RateTask extends AsyncTask<String,String,Integer>{

    /* Context being passed in from main thread*/
    Context context;
    Activity prevActivity;
    private String LOG_TAG =  RateTask.class.getSimpleName();
    private int status;


    public RateTask(Activity activity){
        prevActivity = activity;
        this.context = activity.getApplicationContext();
        status = -1;
    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        String response = "";

        String apiKey = params[0];
        String targetEmail = params[1];
        String rating = params[2];

        Log.d(LOG_TAG, apiKey);
        Log.d(LOG_TAG, targetEmail);
        Log.d(LOG_TAG, rating);

        try {
            final String BASE_URL = "https://young-plains-98404.herokuapp.com/";
            final String CALL = "rateUser";
            final String API_KEY_PARAM = "api_key";
            final String PASS_ID_PARAM = "targetEmail";
            final String CUSTOMER_TYPE_PARAM = "rating";

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
            postDataParams.put(PASS_ID_PARAM, targetEmail);
            postDataParams.put(CUSTOMER_TYPE_PARAM, rating);

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
                status = -1;
            }

            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("complete")) {
                status = 0;
            } else if (jsonResponse.has("error")) {
                status = 1;
            }
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

        return status;
    }

    /*available responses
    0 : Successful rating
    1 : Incorrect params to API
    -1 : unexpected error
 */
    @Override
    protected void onPostExecute(Integer result){
        switch (result) {
            case 0:
                clearPassDataAndReinitialize();
                Intent i = new Intent(prevActivity, MainActivity.class);
                prevActivity.startActivity(i);
                prevActivity.finish();
                break;
            case 1:
                Toast.makeText(context, "Rating failed.", Toast.LENGTH_SHORT).show();
                break;
            case 2:

                break;
            default:
                Toast.makeText(context, "An unexpected error occured.", Toast.LENGTH_SHORT).show();
        }
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

    protected void clearPassDataAndReinitialize() {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String oldPassId = prefs.getString(context.getString(R.string.os_pass_id), "empty");
        String oldPassStatus = prefs.getString(context.getString(R.string.os_pass_status), "empty");
        String oldPassGeoLocation = prefs.getString(context.getString(R.string.os_pass_geolocation), "empty");
        String oldApiKey = prefs.getString(context.getString(R.string.os_apikey), "empty");
        String oldEmail = prefs.getString(context.getString(R.string.os_email), "empty");
        editor.clear();
        editor.apply();

        prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        edit.putString(context.getString(R.string.os_pass_geolocation), oldPassGeoLocation);
        edit.putString(context.getString(R.string.os_pass_status), oldPassStatus);
        edit.putString(context.getString(R.string.os_apikey), oldApiKey);
        edit.putString(context.getString(R.string.os_email), oldEmail);
        edit.putString(context.getString(R.string.os_pass_id), oldPassId);
        edit.apply();
    }
}
