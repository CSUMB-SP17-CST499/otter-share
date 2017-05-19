package com.ottershare.ottershare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class RateInteraction extends AppCompatActivity {

    Button submitBtn;
    SharedPreferences prefs;
    String passId;
    RatingBar ratingBar;
    TextView userEmail;
    Float rating;
    String email;
    String apikey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_interaction);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.os_pref_user_info), Context.MODE_PRIVATE);
        email = prefs.getString(getString(R.string.os_pass_buying_email), "empty");
        apikey = prefs.getString(getString(R.string.os_apikey), "empty");

        submitBtn = (Button) findViewById(R.id.submit_btn);
        userEmail = (TextView) findViewById(R.id.userEmail);
        userEmail.setText(email);

        submitBtn.setOnClickListener(listener);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        rating = ratingBar.getRating();
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
                    runRatingTask();
                    break;
                default:
                    break;
            }

        }
    };

    public void runRatingTask() {
        RateTask rateTask = new RateTask(this);
        rateTask.execute(apikey, email, rating+"");
    }
}
