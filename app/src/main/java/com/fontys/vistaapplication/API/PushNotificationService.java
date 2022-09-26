package com.fontys.vistaapplication.API;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.fontys.vistaapplication.Helper;
import com.fontys.vistaapplication.MainActivity;
import com.fontys.vistaapplication.NotifActions.NotificationDestoryer;
import com.fontys.vistaapplication.NotifActions.NotificationReciever;
import com.fontys.vistaapplication.R;
import com.fontys.vistaapplication.RegisterActivity;
import com.fontys.vistaapplication.RegisterHistoryActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = message.getNotification().getTitle();
        String body = message.getNotification().getBody();

        final String CHANNEL_ID = message.getData().get("channel");
        if(CHANNEL_ID.equals("HEADS_UP_NOTIFICATION")) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Heads Up Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.drawable.ic_notiicon)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Helper.reqCode++, notification.build());
        } else if(CHANNEL_ID.equals("SPOT_VISITED_CHANNEL")) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "At Location Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );

            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.drawable.ic_notiicon)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), Helper.reqCode++, intent, PendingIntent.FLAG_IMMUTABLE);
            //notification.setContentIntent(contentIntent);

            int notifId = Helper.reqCode++;
            Intent broadcastIntent = new Intent(getApplicationContext(), NotificationReciever.class);
            broadcastIntent.putExtra("spot-id", message.getData().get("id"));
            broadcastIntent.putExtra("notif-id", notifId);
            broadcastIntent.setAction("ADD_TO_HISTORY");
            PendingIntent actionIntent = PendingIntent.getBroadcast(getApplicationContext(), Helper.reqCode++, broadcastIntent, PendingIntent.FLAG_MUTABLE);
            notification.addAction(R.drawable.ic_notiicon, "I'm here!", actionIntent);

            Intent broadcastIntent2 = new Intent(getApplicationContext(), NotificationDestoryer.class);
            broadcastIntent2.putExtra("notif-id", notifId);
            broadcastIntent2.setAction("DESTORY_NOTIFICATION");
            PendingIntent actionIntent2 = PendingIntent.getBroadcast(getApplicationContext(), Helper.reqCode++, broadcastIntent2, PendingIntent.FLAG_MUTABLE);
            notification.addAction(R.drawable.ic_notiicon, "No I'm not!", actionIntent2);

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notifId, notification.build());
        }

        super.onMessageReceived(message);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        //now we will have the token
        Task<String> tok = FirebaseMessaging.getInstance().getToken();

        //for now we are displaying the token in the log
        Log.d("MyRefreshedToken", tok.getResult());
    }
}
