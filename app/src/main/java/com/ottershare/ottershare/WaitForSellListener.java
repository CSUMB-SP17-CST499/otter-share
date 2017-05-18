package com.ottershare.ottershare;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class WaitForSellListener implements OSEventListener {

    Activity activity;
    String apikey;
    String passId;

    public WaitForSellListener(Activity newActivity, String apikey, String passId) {
        this.activity = newActivity;
        this.apikey = apikey;
        this.passId = passId;
    }

    public void startEvent() {
        new WaitForSellTask(this, activity).execute(apikey, passId, "empty");
    }

    @Override
    public void onEventCompleted() {
        RelativeLayout layout = (RelativeLayout) activity.findViewById(R.id.wait_for_sell_button_layout);
        layout.setVisibility(View.VISIBLE);

        TextView heading = (TextView) activity.findViewById(R.id.wait_for_sell_text_view);
        heading.setText("Found Buyer");

        ProgressBar bar = (ProgressBar) activity.findViewById(R.id.wait_for_sell_progress_bar);
        bar.setVisibility(View.GONE);
    }

    @Override
    public void onEventFailed() {
        startEvent();
    }
}
