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
import android.test.mock.MockContext;
import android.text.Editable;
import android.widget.EditText;

import junit.framework.TestCase;

/**
 * Local test for LoginActivity functionality (with Mockito)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginActivityUnitTest {

    String email;
    String pass;
    int btnResponse;
    LoginActivity loginActivity;
    SharedPreferences prefs;


    @Before
    public void init() {
        loginActivity = new LoginActivity();
        email = "";
        pass = "";
        btnResponse = 3;
    }

    @Test
    public void testLoginValidatorWithBlankEmailAndPass() {
        email = "";
        pass = "";

        assertThat(loginActivity.isValidLoginInput(email, pass), is(false));
    }

    @Test
    public void testLoginValidatorWithBlankEmail() {
        email = "";
        pass = "testpass";

        assertThat(loginActivity.isValidLoginInput(email, pass), is(false));
    }

    @Test
    public void testLoginValidatorWithBlankPass() {
        email = "example@email.com";
        pass = "";

        assertThat(loginActivity.isValidLoginInput(email, pass), is(false));
    }

    @Test
    public void testLoginValidatorWithValidInput() {
        email = "test@email.com";
        pass = "password";

        assertThat(loginActivity.isValidLoginInput(email, pass), is(true));
    }


    /**
     * TODO: (*) Find out what other LOCAL tests are needed for LoginActivity
     */

    /*@Test - This is attempting to test clearLoginData which clears SharedPreferences. Not working... yet.
    public void test() {
        //at least make sure api key is removed when method is called
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.os_apikey), "12345");
        editor.commit();
        loginActivity.clearLoginData();

    }*/

}