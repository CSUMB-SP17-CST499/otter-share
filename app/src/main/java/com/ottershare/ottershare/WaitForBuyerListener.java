package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

public class WaitForBuyerListener implements OSEventListener {

    Activity activity;
    String apikey;
    String passId;
    int count;
    String status;

    public WaitForBuyerListener(Activity newActivity, String apikey, String passId) {
        this.activity = newActivity;
        this.apikey = apikey;
        this.passId = passId;
        this.count = 1;
        this.status = "";
    }

    public void startEvent() {
        new WaitForBuyerTask(this, activity).execute(apikey, passId, this.count + "");
    }

    @Override
    public void onEventCompleted() {
        Intent i;
        if (status.equals("accepted")) {
            i = new Intent(activity, CompleteTransactionBuyer.class);
            activity.startActivity(i);
            activity.finish();
        } else if (status.equals("rejected")) {
            i = new Intent(activity, MainActivity.class);
            Toast.makeText(activity.getApplicationContext(), "Seller rejected your buy!", Toast.LENGTH_SHORT).show();
            activity.startActivity(i);
            activity.finish();
        }
    }

    @Override
    public void onEventFailed() {
        this.count++;
        startEvent();
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
