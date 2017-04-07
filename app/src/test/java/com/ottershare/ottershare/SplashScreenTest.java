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
 * Local test for LoginActivity functionality (with Mockito)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SplashScreenTest {

    SplashScreen splash;

    @Mock
    SharedPreferences mPrefs;

    Context mContext;

    @Before
    public void init() {
        splash = new SplashScreen();

        mPrefs = mock(SharedPreferences.class);
        mContext = mock(Context.class);

        /*
        * TODO: Write tests for checking sharedpreferences in the splashscreen before writing the actual functionality
        * */
        /*loginActivity = spy(new LoginActivity());
        mockLoginActivity = mock(LoginActivity.class);

        emailInput = mock(EditText.class);
        passwordInput = mock(EditText.class);

        email = "";
        pass = "";
        btnResponse = 3;*/
        when(mContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mPrefs);
        /*mContext.getSharedPreferences
        isLoggedIn();*/
    }


}