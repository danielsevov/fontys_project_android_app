package com.fontys.vistaapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.fontys.vistaapplication.API.BackendAPI;
import com.fontys.vistaapplication.API.BackgroundService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);


        Thread thread = new Thread(new Runnable() {
            public void run() {

                if(BackendAPI.isAuthenticated()) {
                    Intent intent = new Intent(MainActivity.this, HubActivity.class);
                    startActivity(intent);
                    return;
                }

                String data = Helper.readFromFile(getApplicationContext());
                if(data.split("\n").length >= 2 && BackendAPI.Authenticate(data.split("\n")[0], data.split("\n")[1], BackendAPI.getIP(getApplicationContext()))) {
                    Intent intent = new Intent(MainActivity.this, HubActivity.class);
                    startActivity(intent);
                    return;
                }

                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);

            }
        });
        thread.start();

        Context context = getApplicationContext();
        Intent intent = new Intent(this, BackgroundService.class);
        context.startForegroundService(intent);

    }

}