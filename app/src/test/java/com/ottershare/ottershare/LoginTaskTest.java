package com.ottershare.ottershare;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.widget.EditText;

import junit.framework.TestCase;

/**
 * Local test for LoginTask functionality (with Mockito)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginTaskTest {
    /*
        TODO: Test login functionality that makes sure sharedpreferences contains api_key and other info upon logging in
     */

    LoginTask loginTask;

    @Mock
    Context mMockContext;

    @Mock
    SharedPreferences mockPrefs;

/*    @Before
    public void init() {
        mMockContext = mock(Context.class);
        mockPrefs = mock(SharedPreferences.class);
        when(mMockContext.getSharedPreferences(loginTask.OS_PREF_USER_INFO, Context.MODE_PRIVATE)).thenReturn(mockPrefs);
        loginTask = new LoginTask(mMockContext);
    }

    @Test
    public void testSharedPrefs() {
        loginTask.api_key = "somerandomkey";

        loginTask.storeUserKey();

        assertThat("somerandomkey",loginTask.prefs.contains(mMockContext.getString(R.string.os_apikey)));

    }*/
}
