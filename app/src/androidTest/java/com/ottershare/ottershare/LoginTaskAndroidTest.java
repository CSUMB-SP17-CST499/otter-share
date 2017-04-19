package com.ottershare.ottershare;

import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class LoginTaskAndroidTest {

    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);

    //for waiting before asserting after an AsyncTask execution
    final CountDownLatch signal = new CountDownLatch(1);
    final static int WAIT_TIME = 5; //in seconds

    Context mContext;
    LoginTask loginTask;

    @Before
    public void init() {
        mContext = InstrumentationRegistry.getContext();
        loginTask = new LoginTask(activityRule.getActivity());
    }

    /***
     * @throws Throwable
     * This tests a successful LoginTask  by checking to see if SharedPreferences stored an API key
     */

    @Test
    public void testLoginApiStoreWithValidLogin() throws Throwable {
        activityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginTask.execute("bchehraz@csumb.edu", "password");
            }
        });

        //loginTask.execute("bchehraz@csumb.edu", "password");

        signal.await(WAIT_TIME, TimeUnit.SECONDS);
        mContext = activityRule.getActivity().getApplicationContext();
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getString(R.string.os_pref_user_info),Context.MODE_PRIVATE);
        assertThat("Checking to see api key was saved upon a successful login", prefs.getString(mContext.getString(R.string.os_apikey), "default"), is(not("default")));
    }

    /***
     * @throws Throwable
     * This tests a unsuccessful LoginTask by checking to see if SharedPreferences did not store an API key
     */
    @Test
    public void testLoginApiStoreWithInvalidLogin() throws Throwable {

        activityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginTask.execute("somebody@csumb.edu", "password");
            }
        });

        signal.await(WAIT_TIME, TimeUnit.SECONDS);
        mContext = activityRule.getActivity();
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getString(R.string.os_pref_user_info),Context.MODE_PRIVATE);


        assertThat(prefs.getString(mContext.getString(R.string.os_apikey), "default"), is("default"));
    }

    /**
     * TODO: (*) Are there any other tests you can think of for LoginTask?
     */
}
