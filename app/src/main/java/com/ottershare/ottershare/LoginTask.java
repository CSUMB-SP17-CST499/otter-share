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
public class LoginTask extends AsyncTask<String, String, Integer> {
    private final String LOG_TAG = LoginTask.class.getSimpleName();

    /* Context being passed in from main thread*/
    Context mContext;
    Activity prevActivity;

    /* SharedPreferences to be modified */
    SharedPreferences prefs;

    /* Information needed from server response */
    /*TODO: (*) Once you find out what other info you need from the user, update this*/
    //String name;
    String email;
    String api_key;
    int status;



    public LoginTask(Activity activity) {
        //name = "";
        email = "";
        api_key = "";
        status = 0;
        mContext = activity.getApplicationContext();
        prevActivity = activity;
    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        String response = "";

        String emailInput = params[0];
        String passInput = params[1];

        try {
            final String LOGIN_BASE_URL = "https://young-plains-98404.herokuapp.com/";
            final String LOGIN_PARAM = "login";
            final String EMAIL_PARAM = "email";
            final String PASSWORD_PARAM = "password";

            URL url = new URL(LOGIN_BASE_URL + LOGIN_PARAM);

            //set up http connection
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            HashMap<String, String> postDataParams = new HashMap<>();

            postDataParams.put(EMAIL_PARAM, emailInput);
            postDataParams.put(PASSWORD_PARAM, passInput);

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
            JSONObject loginJsonResponse = new JSONObject(response);


            if (loginJsonResponse.has("error")) {
                status = getErrorStatus(loginJsonResponse.getString("error"));
            } else {
                getLoginDataFromJson(loginJsonResponse);
                status = 2;
            }

            //Log.d(LOG_TAG, "\"name\" --> " + name);
            //Log.d(LOG_TAG, "\"email\" --> " + email);
            Log.d(LOG_TAG, "\"Retrieved user api_key\" --> " + api_key);


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
    // 0 -> hasn't verified account
    // 1 -> incorrect email and password combination
    // 2 -> success
    // -1 || any other value -> response code was not ok "200"
    protected void onPostExecute(Integer result) {
        switch (result) {
            case (0):
                Log.d(LOG_TAG, "case = 0" + " actual: " + result);
                makeToast(R.string.login_toast_unverified, Toast.LENGTH_LONG);
                //Anything else to do for unverified users other than a toast? Launch gmail?
                break;
            case (1):
                Log.d(LOG_TAG, "case = 1" + " actual: " + result);
                makeToast(R.string.login_toast_invalid, Toast.LENGTH_LONG);
                break;
            case (2):

                /**
                 * TODO: (***) Find out what other data you need from the user upon logging in
                 */
                storeUserKeyAndEmail();

                boolean isNew = prefs.getBoolean(mContext.getString(R.string.os_new_status), true);
                displayWelcomeToast(isNew);

                //start next activity upon a successful login
                Intent i = new Intent(prevActivity, MainActivity.class);
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

    private void getLoginDataFromJson(JSONObject loginObject) throws JSONException {
        /**
         * TODO: (***) Find out what other data needs to be collected from the server to be stored
         */
        //this.name = loginObject.getString("name");
        this.email = loginObject.getString("email");
        this.api_key = loginObject.getString("api_key");
    }

    private void makeToast(int message, int length) {
        Toast.makeText(mContext, message, length).show();
    }

    private int getErrorStatus(String message) {
        return message.contains("Code 1") ? 1: 0;
    }

    private void storeUserKeyAndEmail() {
        prefs = mContext.getSharedPreferences(mContext.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(mContext.getString(R.string.os_apikey), api_key);
        editor.putString(mContext.getString(R.string.os_email), email);
        editor.commit();
    }

    /**
     * This could potentially be on the server side where you can just check, upon logging in successfully, what that value returned is... and if it's 1, display welcome message, and if 2, different message
     * The advantage
     */
    private void updateUserStatus() {
        prefs = mContext.getSharedPreferences(mContext.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(mContext.getString(R.string.os_new_status), false);
        editor.commit();
    }

    void displayWelcomeToast(boolean isNew) {
        if (isNew) {
            makeToast(R.string.login_toast_success_new, Toast.LENGTH_SHORT);
            updateUserStatus();
        } else {
            makeToast(R.string.login_toast_success, Toast.LENGTH_SHORT);
        }
    }
}