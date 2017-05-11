package com.ottershare.ottershare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class PassView extends AppCompatActivity {

    private RelativeLayout passNotAvailableBtn;
    private RelativeLayout passAvailableBtn;
    private FragmentManager mManager;
    private MapOSFragment mapOSFragment;
    private CircleFrameWithFade circleFrame;
    private RatingBar userRating;
    private TextView user;

    private Button visitUser;
    private Button backButtonAvailable;
    private Button backButtonNotAvailable;
    private Button acceptBtnAvailable;

    private TextView id;
    private TextView price;
    private TextView parkingLot;
    private TextView parkingLat;
    private TextView parkingLon;
    ParkingPassInfo pass;
    String passOwnerEmail;

    final String DEFAULT_API_KEY = "empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_view);
        Bundle b = getIntent().getExtras();
        passOwnerEmail = "";
        if(b != null){
            pass = b.getParcelable("pass");
            passOwnerEmail = pass.getEmail();
        }

    }
    @Override
    public void onResume(){
        super.onResume();

        passNotAvailableBtn = (RelativeLayout) findViewById(R.id.pass_not_available);
        passAvailableBtn = (RelativeLayout) findViewById(R.id.pass_available_layout);

        visitUser = (Button) findViewById(R.id.visit_user_btn);
        backButtonAvailable = (Button) findViewById(R.id.back_btn_available);
        backButtonNotAvailable = (Button) findViewById(R.id.back_btn_not_available);
        acceptBtnAvailable = (Button) findViewById(R.id.accept_btn_available);

        visitUser.setOnClickListener(listener);
        backButtonAvailable.setOnClickListener(listener);
        backButtonNotAvailable.setOnClickListener(listener);
        acceptBtnAvailable.findViewById(R.id.accept_btn_available);

        userRating = (RatingBar) findViewById(R.id.rating_bar);
        user = (TextView) findViewById(R.id.user_tv);
        price = (TextView) findViewById(R.id.price_tv);
        parkingLot = (TextView) findViewById(R.id.lot_tv);
        parkingLat = (TextView) findViewById(R.id.lat_tv);
        parkingLon = (TextView) findViewById(R.id.lon_tv);

        //setting attributes

        user.setText(pass.getEmail());
        price.setText(Double.toString(pass.getPrice()));
        parkingLat.setText(Double.toString(pass.getGpsLoction().latitude));
        parkingLon.setText(Double.toString(pass.getGpsLoction().longitude));
        parkingLot.setText(Integer.toString(pass.getLotLocation()));

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mManager = getSupportFragmentManager();
        mapOSFragment = (MapOSFragment) mManager.findFragmentById(R.id.map);
        circleFrame = (CircleFrameWithFade) mManager.findFragmentById(R.id.circle_frame);

        mapOSFragment.changeCameraLocation(pass.getGpsLoction().latitude,pass.getGpsLoction().longitude,18);
        mapOSFragment.makeMarker(pass.getGpsLoction().latitude,pass.getGpsLoction().longitude);

        circleFrame.fadeOutAnimation();

    }

    View.OnClickListener listener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.back_btn_available:
                case R.id.back_btn_not_available:{
                    //todo go back to mainActivity.
                    Log.i("backBtn","look");
                 break;
                }
                case R.id.accept_btn:
                    //todo go to purchase pass view.
                    Log.i("acceptBtn","look");
                    break;
                case R.id.visit_user_btn:
                    runFetchProfileTask();
                default:
                    break;
            }

        }
    };
    //run the FetchProfileTask for public and private profiles
    private void runFetchProfileTask() {
        Context context = PassView.this.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        String apikey = prefs.getString(context.getString(R.string.os_apikey), DEFAULT_API_KEY);


        FetchProfileTask fetchMovieTask = new FetchProfileTask(this);
        //pass in some sample data but real key and email
        //fetchMovieTask.execute(passOwnerEmail, apikey);
        fetchMovieTask.execute("bchehraz@csumb.edu", apikey);

    }

}
