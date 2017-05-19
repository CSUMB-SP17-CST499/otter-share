package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

public class WaitForSellTask extends AsyncTask<String,String,Integer>{

    /* Context being passed in from main thread*/
    Context context;
    Activity prevActivity;
    private String LOG_TAG =  WaitForSellTask.class.getSimpleName();
    private WaitForSellListener callback;
    int status;
    String buyerEmail;
    double avgRating;

    public WaitForSellTask(WaitForSellListener cb, Activity activity){
        callback = cb;
        prevActivity = activity;
        this.context = activity.getApplicationContext();
        status = -1;
        buyerEmail = "";
        avgRating = -1;
    }

    public WaitForSellTask(Activity activity) {
        prevActivity = activity;
        this.context = activity.getApplicationContext();
        status = -1;
        buyerEmail = "";
        avgRating = -1;
    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        String response = "";

        String apikey = params[0];
        String passId = params[1];
        String action = params[2];
        Log.d(LOG_TAG, passId);

        try {
            final String BASE_URL = "https://young-plains-98404.herokuapp.com/";
            final String CALL = "sellerListener";
            final String API_KEY_PARAM = "api_key";
            final String PASS_ID_PARAM = "passId";
            final String ACTION_PARAM = "action";

            URL url = new URL(BASE_URL + CALL);

            //set up http connection
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            HashMap<String, String> postDataParams = new HashMap<>();

            postDataParams.put(API_KEY_PARAM, apikey);
            postDataParams.put(PASS_ID_PARAM, passId);
            if (!action.equals("empty")) {
                postDataParams.put(ACTION_PARAM, action);
            }

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
            //put in a delay
            if (jsonResponse.has("pending")) {
                try {
                    Thread.sleep(5000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                status = 0;
            } else if (jsonResponse.has("decision")) {
                status = 1;
                JSONObject decisionObject = jsonResponse.getJSONObject("decision");
                avgRating = decisionObject.getDouble("avgRating");
                buyerEmail = decisionObject.getString("email");
            } else if (jsonResponse.has("accepted")) {
                status = 2;
            } else if (jsonResponse.has("rejected")) {
                status = 3;
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
    -1 : api error
    0 : Buyer not found; pending
    1 : Buyer Found
    2 : accepted
    3 : rejected
 */
    @Override
    protected void onPostExecute(Integer result){
        Intent intent;
        switch (result) {
            case 0:
                storeSellingStatus(true, false);
                callback.onEventFailed();
                break;
            case 1: //found buyer
                if (callback != null) {
                    storeSellingStatus(true, true);

                    TextView buyerEmailText = (TextView) prevActivity.findViewById(R.id.found_buyer_email);
                    storeBuyerEmail(buyerEmail);
                    buyerEmailText.setText(buyerEmail);
                    buyerEmailText.setVisibility(View.VISIBLE);

                    RelativeLayout rl = (RelativeLayout) prevActivity.findViewById(R.id.found_buyer_rating_layout);
                    rl.setVisibility(View.VISIBLE);

                    RatingBar rating = (RatingBar) prevActivity.findViewById(R.id.user_profile_rating_bar);
                    rating.setRating((float)avgRating);

                    callback.onEventCompleted();
                }
                break;
            case 2: //accepted
                storeSellingStatus(false, true);
                intent = new Intent(prevActivity, CompleteTransactonSeller.class);
                prevActivity.startActivity(intent);
                prevActivity.finish();
                break;
            case 3: //rejected
                storeSellingStatus(false, false);
                intent = new Intent(prevActivity, WaitForSell.class);
                prevActivity.startActivity(intent);
                prevActivity.finish();
            default:
                Toast.makeText(context, "An unexpected error occured", Toast.LENGTH_SHORT);
        }
    }

    private void storeBuyerEmail(String email) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.os_pass_buying_email), email);
        editor.apply();
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

    private void storeSellingStatus(boolean isSelling, boolean foundBuyer) {
        SharedPreferences prefs = prevActivity.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(context.getString(R.string.os_pass_selling_status), isSelling);
        editor.putBoolean(context.getString(R.string.os_pass_selling_status_foundBuyer), foundBuyer);
        editor.apply();
    }
}
