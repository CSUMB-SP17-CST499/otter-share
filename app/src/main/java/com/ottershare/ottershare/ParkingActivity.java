package com.ottershare.ottershare;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import com.ottershare.ottershare.FusedGpsService.LocalBinder;

public class ParkingActivity extends AppCompatActivity{

    private TextView parkingConformationPrompt;
    private SwipeButton swipeWhenParkedSlider;
    private SwipeButtonCustomItems swipeButtonSettings;

    private FragmentManager fragmentManagerFrame;
    private FragmentManager fragmentManagerMap;
    private CircleFrameWithFade circleFrameWithFade;
    private MapOSFragment frag;
    private RelativeLayout swipeButton;

    private boolean boundToFusedGpsService;
    private FusedGpsService myFusedGpsService;
    Intent it;

    private Animation slideLeftOffScreen;// <--
    private Animation slideLeftOnScreen;//  -->

    private Animation slideRightOffScreen;//    -->
    private Animation slideRightOnScreen;//     <--

    private RelativeLayout buttonsLayout;
    private Button cancelBtn;
    private Button acceptBtn;

    final String DEFAULT_API_KEY = "empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_parking);

        setUpAnimations();

        parkingConformationPrompt = (TextView)findViewById(R.id.swipe_when_parked_prompt_tv);
        setPromtText(getString(R.string.swipe_when_parked_prompt));

        swipeWhenParkedSlider = (SwipeButton) findViewById(R.id.swipe_when_parked_slider);
        swipeButtonSettings = new SwipeButtonCustomItems() {
            @Override
            public void onSwipeConfirm() {
                LatLng tempLatLng = myFusedGpsService.getLocationLatLng();

                //// TODO: 4/19/17 change "if" statement to be true if the tempLatLng location is within the csumb bounds.
                if(true) {
                    frag.changeCameraLocation(tempLatLng.latitude, tempLatLng.longitude, 14);
                    circleFrameWithFade.fadeOutAnimation();
                    swipeButton.startAnimation(slideRightOffScreen);

                    if(buttonsLayout != null) {
                        buttonsLayout.startAnimation(slideRightOnScreen);
                    }
                    else{
                        buttonsLayout = (RelativeLayout) findViewById(R.id.parking_btn_layout);
                        buttonsLayout.startAnimation(slideRightOnScreen);
                    }
                }

            }
        };


        // getcolor deprecated sdk 23
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

    private void setUpAnimations() {
        setSlideLeftOffScreen();
        setSlideLeftOnScreen();
        setSlideRightOffScreen();
        setSlideRightOnScreen();
    }

    private void setSlideLeftOffScreen() {
        slideLeftOffScreen = new TranslateAnimation(0f,-1250f,0f,0f);
        slideLeftOffScreen.setInterpolator(new AccelerateInterpolator());
        slideLeftOffScreen.setStartOffset(250);
        slideLeftOffScreen.setDuration(500);

        slideLeftOffScreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttonsLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void setSlideLeftOnScreen() {
        slideLeftOnScreen = new TranslateAnimation(1250f,0,0f,0f);
        slideLeftOnScreen.setInterpolator(new AccelerateInterpolator());
        slideLeftOnScreen.setStartOffset(250);
        slideLeftOnScreen.setDuration(500);

        slideLeftOnScreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                swipeButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void setSlideRightOffScreen() {
        slideRightOffScreen = new TranslateAnimation(0f,1250f,0f,0f);
        slideRightOffScreen.setInterpolator(new AccelerateInterpolator());
        slideRightOffScreen.setStartOffset(250);
        slideRightOffScreen.setDuration(500);

        slideRightOffScreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                swipeButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void setSlideRightOnScreen() {
        slideRightOnScreen = new TranslateAnimation(-1250,0f,0f,0f);
        slideRightOnScreen.setInterpolator(new AccelerateInterpolator());
        slideRightOnScreen.setStartOffset(250);
        slideRightOnScreen.setDuration(500);

        slideRightOnScreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttonsLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void setPromtText(String str){
        parkingConformationPrompt.setText(str);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        swipeButton = (RelativeLayout) findViewById(R.id.swipe_layout);

        buttonsLayout = (RelativeLayout) findViewById(R.id.parking_btn_layout);
        buttonsLayout.setVisibility(View.GONE);

        acceptBtn = (Button)findViewById(R.id.accept_btn);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runParkingTask();
            }
        });

        cancelBtn = (Button)findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleFrameWithFade.fadeInAnimation();
                buttonsLayout.startAnimation(slideLeftOffScreen);
                swipeButton.startAnimation(slideLeftOnScreen);
            }
        });

        if (swipeWhenParkedSlider != null) {
            swipeWhenParkedSlider.setSwipeButtonCustomItems(swipeButtonSettings);
        }

        fragmentManagerFrame = getSupportFragmentManager();
        fragmentManagerMap = getSupportFragmentManager();

        circleFrameWithFade = (CircleFrameWithFade) fragmentManagerFrame.findFragmentById(R.id.parking_activity_circle_frame);
        frag = (MapOSFragment) fragmentManagerMap.findFragmentById(R.id.parking_map);
        frag.changeCameraLocation(Double.parseDouble(getString(R.string.all_csumb_lat)),Double.parseDouble(getString(R.string.all_csumb_lon)),Float.parseFloat(getString(R.string.all_csumb_zoom)));

    }



    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundToFusedGpsService = true;
            LocalBinder myLocalBinder = (LocalBinder)service;
            myFusedGpsService = myLocalBinder.getservice();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundToFusedGpsService = false;
            myFusedGpsService = null;
        }
    };

    @Override
    protected void onStart() {
        Log.w("parking", "onStart");
        super.onStart();
        Intent myIntent = new Intent(this, FusedGpsService.class);
        bindService(myIntent, myConnection ,BIND_AUTO_CREATE);

         it = new Intent(this,FusedGpsService.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(it);
    }


    @Override
    protected void onResume() {
        super.onResume();
        startService(it);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(boundToFusedGpsService) {
            unbindService(myConnection);
            boundToFusedGpsService = false;
        }
    }

    //start parking task to register the pass
    private void runParkingTask() {
        Context context = ParkingActivity.this.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        String apikey = prefs.getString(context.getString(R.string.os_apikey), DEFAULT_API_KEY);


        ParkingTask parkingTask = new ParkingTask(this);
        //pass in some sample data but real key and email
        parkingTask.execute(apikey, "bchehraz@csumb.edu", "200", new LatLng(36.652129, -121.804482).toString(), "4", "This pass is the best one");

    }
}
