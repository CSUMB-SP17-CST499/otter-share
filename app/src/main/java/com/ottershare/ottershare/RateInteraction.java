package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

public class RateInteraction extends AppCompatActivity {

    Button submitBtn;
    SharedPreferences prefs;
    String passId;
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_interaction);

        submitBtn = (Button) findViewById(R.id.submit_btn);

        submitBtn.setOnClickListener(listener);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
    }

    View.OnClickListener listener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent i;
            switch (v.getId())
            {
                case R.id.submit_btn:
                    break;
                default:
                    break;
            }

        }
    };
}
