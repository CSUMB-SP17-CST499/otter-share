package com.ottershare.ottershare;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import static com.ottershare.ottershare.R.color.colorPrimary;

public class ParkingActivity extends AppCompatActivity{

    private TextView swipeWhenParkedPrompt;
    private TextView parkingConformationPrompt;
    private SwipeButton swipeWhenParkedSlider;
    private SwipeButtonCustomItems swipeButtonSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_parking);
        swipeWhenParkedPrompt = (TextView)findViewById(R.id.swipe_when_parked_prompt_tv);
        swipeWhenParkedPrompt.setText(formatTextSwipeWhenParkedPrompt());
       // parkingConformationPrompt = (TextView)findViewById(R.id.parking_conformation_prompt_tv);

        swipeWhenParkedSlider = (SwipeButton) findViewById(R.id.swipe_when_parked_slider);

        swipeButtonSettings = new SwipeButtonCustomItems() {
            @Override
            public void onSwipeConfirm() {
                Log.d("NEW_STUFF", "New swipe confirm callback");
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            swipeButtonSettings
                    .setButtonPressText("SLIDE")
                    .setGradientColor1(this.getColor(R.color.colorPrimary))
                    .setGradientColor2(this.getColor(R.color.colorPrimaryDark))
                    .setGradientColor2Width(100)
                    .setGradientColor3(this.getColor(R.color.colorsecondDarker))
                    .setPostConfirmationColor(this.getColor(R.color.colorsecondDarker))
                    .setActionConfirmDistanceFraction(0.8)
                    .setActionConfirmText("Action Confirmed");
        }else{
            swipeButtonSettings
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

    /*public CharSequence formatTextParked(){

    }*/

    public CharSequence formatTextSwipeWhenParkedPrompt(){
        SpannableString spanable1 = new SpannableString(getString(R.string.swipe_when_parked_prompt_1));
        spanable1.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.swipe_when_parked_prompt_size_small)), 0,
                getString(R.string.swipe_when_parked_prompt_1).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString spanable2 = new SpannableString(getString(R.string.swipe_when_parked_prompt_2));
        spanable2.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.swipe_when_parked_prompt_size_large)), 0,
                getString(R.string.swipe_when_parked_prompt_2).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return TextUtils.concat(spanable1,",\n",spanable2);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (swipeWhenParkedSlider != null) {
            swipeWhenParkedSlider.setSwipeButtonCustomItems(swipeButtonSettings);
        }
    }

}
