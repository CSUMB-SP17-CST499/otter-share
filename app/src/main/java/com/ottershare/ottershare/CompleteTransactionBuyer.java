package com.ottershare.ottershare;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CompleteTransactionBuyer extends AppCompatActivity {

    private SwipeButton swipeButton;
    private SwipeButtonCustomItems swipeButtonCustomItems;
    private String sellerEmail;
    TextView userEmailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_transaction_buyer);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        sellerEmail = prefs.getString(getString(R.string.os_pass_buying_email), "empty");

        userEmailText = (TextView) findViewById(R.id.user_text);
        userEmailText.setText(sellerEmail);

        swipeButton = (SwipeButton) findViewById(R.id.complete_slider);
        swipeButtonCustomItems = new SwipeButtonCustomItems() {
            @Override
            public void onSwipeConfirm() {
                // TODO: 5/14/17 make call to say that they have completed the transaction.
                // TODO: 5/14/17 may need to make a repeating ping to listen for the other user to complete transaction.
                // TODO: 5/14/17  start activity RateInteraction passing the appropriate information to identify the user that they will be rating.
            }

        };

        // getcolor deprecated sdk 23
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            swipeButtonCustomItems
                    .setButtonPressText("SLIDE")
                    .setGradientColor1(this.getColor(R.color.colorPrimary))
                    .setGradientColor2(this.getColor(R.color.colorPrimaryDark))
                    .setGradientColor2Width(100)
                    .setGradientColor3(this.getColor(R.color.colorsecondDarker))
                    .setPostConfirmationColor(this.getColor(R.color.colorsecondDarker))
                    .setActionConfirmDistanceFraction(0.8)
                    .setActionConfirmText("Action Confirmed");
        }else{
            swipeButtonCustomItems
                    .setButtonPressText("SLIDE")
                    .setGradientColor1(getResources().getColor(R.color.colorPrimary))
                    .setGradientColor2(getResources().getColor(R.color.colorPrimaryDark))
                    .setGradientColor2Width(100)
                    .setGradientColor3(getResources().getColor(R.color.colorsecondDarker))
                    .setPostConfirmationColor(getResources().getColor(R.color.colorsecondDarker))
                    .setActionConfirmDistanceFraction(0.8)
                    .setActionConfirmText("Action Confirmed");
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){

        if(swipeButton != null){
            swipeButton.setSwipeButtonCustomItems(swipeButtonCustomItems);
        }

    }
}