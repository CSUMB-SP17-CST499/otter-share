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

public class TransactionTask extends AsyncTask<String,String,Integer>{

    /* Context being passed in from main thread*/
    Context context;
    Activity prevActivity;
    private String LOG_TAG =  TransactionTask.class.getSimpleName();
    private int status;
    TransactionListener callback;

    public TransactionTask(TransactionListener cb, Activity activity){
        prevActivity = activity;
        this.context = activity.getApplicationContext();
        status = -1;
        this.callback = cb;
    }

    public TransactionTask(Activity activity){
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
        String customerType = params[2];

        Log.d(LOG_TAG, apiKey);
        Log.d(LOG_TAG, passId);
        Log.d(LOG_TAG, customerType);

        try {
            final String BASE_URL = "https://young-plains-98404.herokuapp.com/";
            final String CALL = "completionListener";
            final String API_KEY_PARAM = "api_key";
            final String PASS_ID_PARAM = "passId";
            final String CUSTOMER_TYPE_PARAM = "customerType";

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
            postDataParams.put(CUSTOMER_TYPE_PARAM, customerType);

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
            if (customerType.equals("buyer") || customerType.equals("seller")) {
                if (jsonResponse.has("exchanged")) {
                    status = 0;
                    try {
                        Thread.sleep(1000);                 //1000 milliseconds is one second.
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    status = -1;
                }
            } else if (customerType.equals("pending")) {
                if (jsonResponse.has("pending")) {
                    try {
                        Thread.sleep(5000);                 //1000 milliseconds is one second.
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    status = 1;
                } else if (jsonResponse.has("complete")) {
                    status = 2;
                }
            } else {
                status = -1;
            }
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

        return status;
    }

    /*available responses
    0 : Successfully swiped, go to pending
    1 : Currently pending, keep calling
    2 : Other person successfully swiped too, end
    -1 : api fail or anything
 */
    @Override
    protected void onPostExecute(Integer result){
        switch (result) {
            case 0:
                if (callback != null) {
                    callback.setStatus("pending");
                    callback.onEventFailed();
                }
                break;
            case 1:
                if (callback != null) {
                    callback.onEventFailed();
                }
                break;
            case 2:
                if (callback != null) {
                    callback.onEventCompleted();
                }
                break;
            default:
                Toast.makeText(context, "An expected error occured.", Toast.LENGTH_SHORT).show();
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
