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
public class LoginActivityUnitTest {



    String email;
    String pass;
    int btnResponse;
    LoginActivity loginActivity;

    @Mock
    Context mMockContext;

    @Mock
    EditText emailInput;

    @Mock
    EditText passwordInput;

    @Mock
    LoginActivity mockLoginActivity;

    @Before
    public void init() {
        loginActivity = spy(new LoginActivity());
        mockLoginActivity = mock(LoginActivity.class);

        emailInput = mock(EditText.class);
        passwordInput = mock(EditText.class);

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

    @Test
    public void testPerformClickForLogin1() {
        email = "random@email.com";
        pass = "password";
        when(mockLoginActivity.getEmailFromField()).thenReturn(email);
        when(mockLoginActivity.getPasswordFromField()).thenReturn(pass);
        when(mockLoginActivity.runLoginTask(email, pass)).thenReturn(1);
        when(mockLoginActivity.isValidLoginInput(email, pass)).thenReturn(true);
        when(mockLoginActivity.performClick(mockLoginActivity.BTN_CODE_SUBMIT)).thenReturn(1);

        //doReturn(1).when(mockLoginActivity.performClick(1));
        doCallRealMethod().when(mockLoginActivity.performClick(mockLoginActivity.BTN_CODE_SUBMIT));
        //



        /*final EditText emailInput = mock(EditText.class);
        final EditText passwordInput = mock(EditText.class);

        doCallRealMethod().when(emailInput).getText();
        doCallRealMethod().when(passwordInput).getText();

        when(emailInput.getText()).thenReturn(Editable.Factory.getInstance().newEditable(email));
        when(passwordInput.getText()).thenReturn(Editable.Factory.getInstance().newEditable(pass));
        when(emailInput.getText().toString()).thenReturn(email);
        when(passwordInput.getText().toString()).thenReturn(pass);



        /*doReturn(emailInput).when(loginActivity).findViewById(R.id.login_email_input);
        doReturn(passwordInput).when(loginActivity).findViewById(R.id.login_password_input);*/
        btnResponse = mockLoginActivity.performClick(mockLoginActivity.BTN_CODE_SUBMIT);
        assertThat(btnResponse, is(1));

        /*
        *  TODO: Write local unit test for performing login button click and other login tasks...
        *
        * */
    }

}