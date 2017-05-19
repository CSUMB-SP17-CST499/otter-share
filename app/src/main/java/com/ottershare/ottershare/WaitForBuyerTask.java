package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Context;
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

public class WaitForBuyerTask extends AsyncTask<String,String,Integer>{

    /* Context being passed in from main thread*/
    Context context;
    Activity prevActivity;
    private String LOG_TAG =  WaitForBuyerTask.class.getSimpleName();
    private int status;
    WaitForBuyerListener callback;

    public WaitForBuyerTask(WaitForBuyerListener cb, Activity activity){
        prevActivity = activity;
        this.context = activity.getApplicationContext();
        status = -1;
        this.callback = cb;
    }

    public WaitForBuyerTask(Activity activity){
        prevActivity = activity;
        this.context = activity.getApplicationContext();
        status = -1;
    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        String response = "";

        String apiKey = params[0];
        String passId = params[1];
        String requestCount = params[2];
        Log.d(LOG_TAG, apiKey);
        Log.d(LOG_TAG, passId);
        Log.d(LOG_TAG, requestCount);

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
            if (jsonResponse.has("pending")) {
                try {
                    Thread.sleep(5000);                 //1000 milliseconds is one second.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                status = 0;
            } else if (jsonResponse.has("accepted")) {
                status = 1;
            } else if (jsonResponse.has("rejected")) {
                status = 2;
            } else if (jsonResponse.has("error")) {
                status = -1;
            }
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

        return status;
    }

    /*available responses
    0 : Buyer not found
    1 : Buyer accepted
    2 : Buyer rejected
    -1 : api fail or anything
 */
    @Override
    protected void onPostExecute(Integer result){
        switch (result) {
            case 0:
                callback.onEventFailed();
                break;
            case 1:
                if (callback != null) {
                    callback.setStatus("accepted");
                    callback.onEventCompleted();
                }
                break;
            case 2:
                if (callback != null) {
                    callback.setStatus("rejected");
                    callback.onEventCompleted();
                }
                break;
            default:
                Toast.makeText(context, "An expected error occured.", Toast.LENGTH_SHORT);
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
}
