package com.ottershare.ottershare;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Local test for LoginActivity functionality
 *
 */
public class LoginActivityUnitTest {

    LoginActivity loginActivity;
    String email;
    String pass;

    @Before
    public void init() {
        loginActivity = new LoginActivity();
    }

    @Test
    public void testLoginValidatorWithBlankEmailAndPass() {
        email = "";
        pass = "";

        assertFalse(loginActivity.isValidLoginInput(email, pass));
    }

    @Test
    public void testLoginValidatorWithBlankEmail() {
        email = "";
        pass = "testpass";

        assertFalse(loginActivity.isValidLoginInput(email, pass));
    }

    @Test
    public void testLoginValidatorWithBlankPass() {
        email = "example@email.com";
        pass = "";

        assertFalse(loginActivity.isValidLoginInput(email, pass));
    }

    @Test
    public void testLoginValidatorWithValidInput() {
        email = "test@email.com";
        pass = "password";

        assertTrue(loginActivity.isValidLoginInput(email, pass));
    }
}