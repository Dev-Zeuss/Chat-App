package com.zeus.chatme.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

public class OreoNotification extends ContextWrapper {

    private static final String CHANNEL_ID = "com.zeus.chatme";
    private static final String CHANNEL_NAME = "chat_me";

    private NotificationManager notificationManager;

    public OreoNotification(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME , NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOreoNotification(String title , String body, PendingIntent pendingIntent, Uri soundUri, String icon, String type) {
        Notification.Builder builder =  new Notification.Builder(getApplicationContext(), CHANNEL_ID);
        switch (type){
            case "text" :
                builder.setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSmallIcon(Integer.parseInt(icon))
                        .setSound(soundUri)
                        .setTimeoutAfter(300000)
                        .setAutoCancel(true);
                break;
            case "image":
                builder.setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setContentText("Image")
                        .setSmallIcon(Integer.parseInt(icon))
                        .setSound(soundUri)
                        .setTimeoutAfter(300000)
                        .setAutoCancel(true);
                break;
            case "video":
                builder.setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setContentText("Video")
                        .setSmallIcon(Integer.parseInt(icon))
                        .setSound(soundUri)
                        .setTimeoutAfter(300000)
                        .setAutoCancel(true);
                break;

        }
        return builder;
    }
}
