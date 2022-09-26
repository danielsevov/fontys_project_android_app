package com.fontys.vistaapplication.API;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.fontys.vistaapplication.Helper;
import com.fontys.vistaapplication.LoginActivity;
import com.fontys.vistaapplication.MainActivity;
import com.fontys.vistaapplication.PermissionUtil;
import com.fontys.vistaapplication.R;
import com.fontys.vistaapplication.RegisterHistoryActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;

import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    private final LocalBinder mBinder = new LocalBinder();
    protected Handler handler;
    protected Toast mToast;

    protected HashMap<String, Date> _spotsAsked = new HashMap<String, Date>();

    public class LocalBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
                new Notification.Builder(this, Helper.BACKGROUND_NOTIFICATION_CHANNEL)
                        .setContentTitle("Background tasks?!")
                        .setContentText("Hey! We are checking if you have visited any spots. No worries we can see none of your data!")
                        .setSmallIcon(R.drawable.ic_newnotiicon)
                        .setContentIntent(pendingIntent)
                        .build();

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Vista";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_NONE;
            NotificationChannel mChannel = new NotificationChannel(Helper.BACKGROUND_NOTIFICATION_CHANNEL, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        startForeground(Helper.reqCode++, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Background service destroyed!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Timer timr = new Timer("Notif-Spammer", true);
                timr.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(!PermissionUtil.isGPSactive(getApplicationContext())) {
                            return; //GPS is not enabled!
                        }

                        //Fragment can be null you would not want to get the request every interation
                        PermissionUtil.getCurrentLocation(getApplicationContext(), null, location -> {
                            if(location == null) return;

                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener((token) -> {
                                BackendAPI.Send(new PropertyConstructor()
                                                .addProperty("user_coordinates", location.getLatitude() + "," + location.getLongitude())
                                                .addProperty("user_token", token.getResult()),
                                        "is_user_nearby_spot", "POST", (data) -> {

                                        });
                            });
                        });
                    }
                }, 5 * 60 * 1000, 7 * 60 * 1000);
            }
        });
        return android.app.Service.START_STICKY;
    }

}
