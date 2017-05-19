package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class TransactionListener implements OSEventListener {

    Activity activity;
    String apikey;
    String passId;
    String status;

    public TransactionListener(Activity newActivity, String apikey, String passId, String status) {
        this.activity = newActivity;
        this.apikey = apikey;
        this.passId = passId;
        this.status = status;
    }
    //os_pass_buying_id = get pass id for person buying it
    //os_pass_id = get pass id for person selling it
    public void startEvent() {
        new TransactionTask(this, activity).execute(apikey, passId, this.status);
    }

    @Override
    public void onEventCompleted() {
        /*RelativeLayout layout = (RelativeLayout) activity.findViewById(R.id.wait_for_sell_button_layout);
        layout.setVisibility(View.VISIBLE);

        TextView heading = (TextView) activity.findViewById(R.id.wait_for_sell_text_view);
        heading.setText("Found Buyer");

        ProgressBar bar = (ProgressBar) activity.findViewById(R.id.wait_for_sell_progress_bar);
        bar.setVisibility(View.GONE);*/
        Intent i = new Intent(activity, RateInteraction.class);
        activity.startActivity(i);
    }

    @Override
    public void onEventFailed() {
        startEvent();
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
