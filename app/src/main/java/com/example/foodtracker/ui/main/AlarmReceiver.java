package com.example.foodtracker.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.example.foodtracker.SEND_NOTIFICATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent(ACTION));
    }
}
