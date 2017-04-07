package com.ottershare.ottershare;

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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class LoginTaskAndroidTest {

    @Rule
    public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);

    Context mContext;
    LoginActivity loginActivity;
    LoginTask loginTask;

    @Before
    public void init() {
        mContext = InstrumentationRegistry.getContext();
        loginTask = new LoginTask(mContext);
    }

    @Test
    public void testLoginApiStore() throws Throwable {
        activityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginTask.execute("bchehraz@csumb.edu", "password");
            }
        });
        mContext = activityRule.getActivity().getApplicationContext();
        SharedPreferences prefs = mContext.getSharedPreferences(loginTask.OS_PREF_USER_INFO,Context.MODE_PRIVATE);

        assertThat(true, is(prefs.contains(mContext.getString(R.string.os_apikey))));

    }
}
