package com.fontys.vistaapplication.NotifActions;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fontys.vistaapplication.API.BackendAPI;
import com.fontys.vistaapplication.Helper;
import com.fontys.vistaapplication.R;

public class NotificationReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Cmon guys :c *cryin in a corner*");

        if(!BackendAPI.isAuthenticated()) {
            Toast.makeText(context, R.string.spot_visited_no_auth, Toast.LENGTH_LONG).show();
            return; //No account!!
        }

        String id = (String)intent.getExtras().get("spot-id");
        int notifID = (int)intent.getExtras().get("notif-id");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifID);

        System.out.println("visit_spot/" + id);
        BackendAPI.Send(null, "visit_spot/" + id, "POST", (data) -> {
            //Make it run backend wise
            Toast.makeText(context, R.string.spot_visited, Toast.LENGTH_LONG).show();
        });
    }
}
