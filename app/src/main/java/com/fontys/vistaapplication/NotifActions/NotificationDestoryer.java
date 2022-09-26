package com.fontys.vistaapplication.NotifActions;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.fontys.vistaapplication.API.BackendAPI;
import com.fontys.vistaapplication.R;

public class NotificationDestoryer extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int notifID = (int)intent.getExtras().get("notif-id");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifID);


    }
}
