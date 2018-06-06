package com.hamdam.hamdam.service.update;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Updates from AWS.
 */

public class CustomAmazonReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, UpdateService.class);
        String action = intent.getAction();
        serviceIntent.setAction(action);
        startWakefulService(context, serviceIntent);
    }
}
