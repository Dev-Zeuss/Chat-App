package com.zeus.chatme.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zeus.chatme.chatSection.ChatDetailActivity;

import org.jetbrains.annotations.NotNull;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived( @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences sharedPref = getSharedPreferences("USER_PREF", MODE_PRIVATE);
        String currentUser = sharedPref.getString("currentuser", "none");

        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {
            if (!currentUser.equals(user)) { // don't send notification when currently chatting with the recipient
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage);
                } else {
                    sendNotification(remoteMessage);
                }
            }
        }

    }

    private void sendOreoNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
//        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userId", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//        int i = 0;
//        if (j > 0) {
//            i = j;
//        }

        int id = (int) System.currentTimeMillis();

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon , type);
        oreoNotification.getManager().notify(id, builder.build());

    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
//        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userId", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//        int i = 0;
//        if (j > 0) {
//            i = j;
//        }

        int id = (int) System.currentTimeMillis();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        switch (type) {
            case "text":
                        builder.setSmallIcon(Integer.parseInt(icon))
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSound)
                                .setTimeoutAfter(300000)
                        .setContentIntent(pendingIntent);
                break;
            case "image":
                builder.setSmallIcon(Integer.parseInt(icon))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentTitle(title)
                        .setContentText("Image")
                        .setAutoCancel(true)
                        .setSound(defaultSound)
                        .setTimeoutAfter(300000)
                        .setContentIntent(pendingIntent);
                break;
            case "video":
                builder.setSmallIcon(Integer.parseInt(icon))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentTitle(title)
                        .setContentText("Video")
                        .setAutoCancel(true)
                        .setSound(defaultSound)
                        .setTimeoutAfter(300000)
                        .setContentIntent(pendingIntent);
                break;
        }
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(id , builder.build());
    }
}
