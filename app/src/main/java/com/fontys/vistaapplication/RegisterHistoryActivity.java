package com.fontys.vistaapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.fontys.vistaapplication.API.BackendAPI;

import java.util.function.Consumer;

public class RegisterHistoryActivity extends AppCompatActivity {

    static Consumer<Boolean> clickAble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);

        Thread thread = new Thread(() -> {
            if(clickAble != null) clickAble.accept(BackendAPI.isAuthenticated());
            clickAble = null;

            if(!BackendAPI.isAuthenticated()) {
                Intent loginIntent = new Intent(RegisterHistoryActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                return;
            }

            Intent intent = new Intent(RegisterHistoryActivity.this, HubActivity.class);
            startActivity(intent);
        });
        thread.start();

    }

    public static void setAction(Consumer<Boolean> action) {
        clickAble = action;
    }

}