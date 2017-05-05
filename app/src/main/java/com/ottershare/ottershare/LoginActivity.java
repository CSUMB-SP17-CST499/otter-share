package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.prefs.Preferences;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput;
    EditText passwordInput;

    Button loginSubmit;
    Button regBtn; //register button

    SharedPreferences prefs;

    final static int BTN_CODE_SUBMIT = 1;
    final static int BTN_CODE_REGISTER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = (EditText) findViewById(R.id.login_email_input);
        passwordInput = (EditText) findViewById(R.id.login_password_input);
        loginSubmit = (Button) findViewById(R.id.login_btn_submit);
        regBtn = (Button) findViewById(R.id.login_btn_create);

        //initialize preferences to os_pref_user_info
        prefs = this.getSharedPreferences(this.getString(R.string.os_pref_user_info),Context.MODE_PRIVATE);

        //clear shared preferences
        clearLoginData();
        prefs = this.getSharedPreferences(this.getString(R.string.os_pref_user_info),Context.MODE_PRIVATE);
        Log.d("LoginActivity.class", "" + prefs.getString(this.getString(R.string.os_apikey), "default"));
        /*
            Upon clicking the "check mark" on the keyboard after filling out the login form
         */
        // TODO: (*) Create a UI test/integration test for this
        passwordInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginSubmit.performClick();
                    hideSoftKeyboard();
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
                int result = performClick(BTN_CODE_SUBMIT);
                if (result == 0) {
                    makeToast(R.string.login_toast_fatal_error);
                } else if (result == 2) {
                    makeToast(R.string.login_toast_fatal_error);
                }
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = performClick(BTN_CODE_REGISTER);
            }
        });
    }

    //returns 0 if click fails, 1 if success, 2 if value error
    protected int performClick(int value) {
        boolean success;
        switch(value) {
            case 1: //main login submit button
                String email = getEmailFromField();
                String password = getPasswordFromField();

                if (isValidLoginInput(email, password)) {
                    runLoginTask(email, password);
                } else {
                    makeToast(R.string.login_toast_invalid);
                }
                hideSoftKeyboard();

                success = true;
                break;
            case 2: //register button is pressed
                // TODO: Implement REGISTER activity


                //TODO: Start register activity

                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);

                success = true;
                break;
            default:
                //do nothing by default
                //return code 2 if the value is something that doesn't exist
                return 2;
        }
        return success ? 1 : 0;
    }


    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void makeToast(int message) {
        makeToast(message, Toast.LENGTH_SHORT);
    }

    private void makeToast(int message, int speed) {
        Toast.makeText(getApplicationContext(), message, speed).show();
    }

    boolean isValidLoginInput(String email, String pass) {
        return !(email.isEmpty() || pass.isEmpty());
    }

    String getEmailFromField() {
        return emailInput.getText().toString();
    }

    String getPasswordFromField() {
        return passwordInput.getText().toString();
    }

    int runLoginTask(String email, String password) {
        LoginTask loginTask = new LoginTask(this);
        loginTask.execute(email, password);
        return 1;
    }

    /*
    *  TODO: (*) Sanity Check: clearLoginData()
    *  Should I clear anything other than just api key, upon entering the login screen?
    */
    //clears shared preferences of any login data since it's on the login screen
    protected void clearLoginData() {
        prefs = this.getSharedPreferences(this.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(this.getString(R.string.os_apikey));
        editor.commit();
    }
}
