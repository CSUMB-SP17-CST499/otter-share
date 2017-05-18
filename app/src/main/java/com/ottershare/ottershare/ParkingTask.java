package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
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
 * Created by User on 3/25/2017.
 */
// asynchronous call to log in and retreive key
public class ParkingTask extends AsyncTask<String, String, Integer> {
    private final String LOG_TAG = ParkingTask.class.getSimpleName();

    /* Context being passed in from main thread*/
    Context mContext;
    Activity prevActivity;

    /* SharedPreferences to be modified */
    SharedPreferences prefs;

    /* Information needed from server response */
    int status;



    public ParkingTask(Activity activity) {
        status = 0;
        mContext = activity.getApplicationContext();
        prevActivity = activity;
    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        String response = "";

        String api_key = params[0];
        String email = params[1];
        String lotLocation = params[2];
        String gpsLocation = params[3];
        String price = params[4];
        String notes = params[5];

        try {
            final String BASE_URL = "https://young-plains-98404.herokuapp.com/";
            final String CALL = "registerPass";
            final String API_KEY_PARAM = "api_key";
            final String EMAIL_PARAM = "email";
            final String LOT_PARAM = "lotLocation";
            final String GPS_PARAM = "gpsLocation";
            final String PRICE_PARAM = "price";
            final String NOTES_PARAM = "notes";

            URL url = new URL(BASE_URL + CALL);

            //set up http connection
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            HashMap<String, String> postDataParams = new HashMap<>();

            postDataParams.put(EMAIL_PARAM, email);
            postDataParams.put(API_KEY_PARAM, api_key);
            postDataParams.put(LOT_PARAM, lotLocation);
            postDataParams.put(GPS_PARAM, gpsLocation);
            postDataParams.put(PRICE_PARAM, price);
            postDataParams.put(NOTES_PARAM, notes);

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

                int index = 0;
                while ((line = br.readLine()) != null) {
                    response += line;
                    Log.d(LOG_TAG, response);
                }
            } else {
                /**
                 * TODO: find out how to test this for when there really is a bad response code
                 * Perhaps test the next step where the result is returned to the Switch in onPostExecute...
                 */
                response = "";
                return -1;
            }

            //Put data into a json object format
            JSONObject jsonResponse = new JSONObject(response);

            Log.d(LOG_TAG, api_key);
            if (jsonResponse.has("success")) {
                status = 2;
            } else {
                status = 1;
            }

            //Log.d(LOG_TAG, "\"name\" --> " + name);
            //Log.d(LOG_TAG, "\"email\" --> " + email);
            //Log.d(LOG_TAG, "\"Retrieved user api_key\" --> " + api_key);


        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) // Make sure the connection is not null.
                httpURLConnection.disconnect();
        }

        return status;
    }

    //assuming response 200...
    // 0 ->
    // 1 ->
    // 2 -> success
    // -1 || any other value -> response code was not ok "200"
    protected void onPostExecute(Integer result) {
        switch (result) {
            case (0):

                break;
            case (1):
                break;
            case (2)://success, start main activity
                storePassStatus("registered");
                Intent i = new Intent(prevActivity,MainActivity.class);
                prevActivity.startActivity(i);
                break;
            default:
                Log.d(LOG_TAG, "case = default" + " actual: " + result);
                makeToast(R.string.login_toast_fatal_error, Toast.LENGTH_SHORT);
        }
    }

    protected void onProgressUpdate(Integer... progress) {
        //probably wont use, but maybe way later...

    }

    //put the data from hash map into POST format "this=this&this=that" since post data has be sent via a string
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


    private void makeToast(int message, int length) {
        Toast.makeText(mContext, message, length).show();
    }

    private void storePassStatus(String status) {
        prefs = mContext.getSharedPreferences(mContext.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(mContext.getString(R.string.os_pass_status), status);
        editor.commit();
    }
}