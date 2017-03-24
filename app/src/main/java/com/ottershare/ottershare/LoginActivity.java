package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput;
    EditText passwordInput;
    Button loginSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = (EditText) findViewById(R.id.login_email_input);
        passwordInput = (EditText) findViewById(R.id.login_password_input);
        loginSubmit = (Button) findViewById(R.id.login_btn_submit);

        /*
            Upon clicking the "check mark" on the keyboard after filling out the login form
         */
        passwordInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginSubmit.performClick();
                    return true;
                }
                return false;
            }
        });

        /*
            Login submit button click listener
         */
        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();*/
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                if (isValidLoginInput(email, password)) {
                    if (isUser(email, password)) {
                        makeToast("Welcome to OtterShare!", Toast.LENGTH_LONG);
                    } else {
                        makeToast("Invalid Login Credentials");
                    }
                } else {
                    makeToast("Invalid Login Credentials");
                }


            }
        });
    }

    private void makeToast(String message) {
        makeToast(message, Toast.LENGTH_SHORT);
    }

    private void makeToast(String message, int speed) {
        Toast.makeText(getApplicationContext(), message, speed).show();
    }

    boolean isValidLoginInput(String email, String pass) {
        if (email.isEmpty() || pass.isEmpty()) {
            return false;
        }
        return true;
    }

    boolean isUser(String email, String pass) {
        LoginTask loginTask = new LoginTask();
        loginTask.execute(email, pass);
        return false;
    }

    // asynchronous call to log in and retreive key
    public class LoginTask extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = LoginTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String response = null;

            String emailInput = params[0];
            String passInput = params[1];
            InputStream in = null;
            try {
                final String LOGIN_BASE_URL = "https://young-plains-98404.herokuapp.com/";
                final String LOGIN_PARAM = "login";
                final String EMAIL_PARAM = "email";
                final String PASSWORD_PARAM = "password";

                URL url = new URL(LOGIN_BASE_URL + LOGIN_PARAM);

                httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                HashMap<String, String> postDataParams = new HashMap<>();

                postDataParams.put(EMAIL_PARAM, "bchehraz@csumb.edu");
                postDataParams.put(PASSWORD_PARAM, "password");

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
                    BufferedReader br=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response += line;
                    }
                }
                else {
                    response="";
                }
                // now parse the response since it's in JSON format
                // figure out how to read the output
                //JSONObject jsonObject = new JSONObject().getJ;

                Log.d(LOG_TAG, "RESULT 1 --> " + response);
                Reader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));


                JSONTokener jsonTokener = new JSONTokener(response);
                JSONStringer jsonStringer = new JSONStringer();
                jsonStringer.

                Log.d(LOG_TAG, "RESULT 2 --> " + jsonTokener.toString());
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if(httpURLConnection != null) // Make sure the connection is not null.
                    httpURLConnection.disconnect();
            }

            return "hello";
        }

        protected void onPostExecute(String result) {
            //Log.d(LOG_TAG, "RESULT --> "+result);
        }

        protected void onProgressUpdate(Void... progress) {

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
    }
}
