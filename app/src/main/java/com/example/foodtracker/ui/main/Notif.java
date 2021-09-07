package com.example.foodtracker.ui.main;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Notif extends android.app.Application {
    public static final String NOTIFICATION_ID = "notif";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notif = new NotificationChannel(
                    NOTIFICATION_ID,
                    "notif",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notif.setDescription("This is for notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notif);
        }

    }
}
