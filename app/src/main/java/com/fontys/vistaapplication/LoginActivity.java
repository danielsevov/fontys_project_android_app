package com.fontys.vistaapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fontys.vistaapplication.API.BackendAPI;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText textEmail;
    private EditText textPassword;
    private TextView buttonLog;
    private TextView noAccount;
    private static final String TAG = "LOGIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonLog = findViewById(R.id.buttonLog);
        textEmail = findViewById(R.id.textEmail);
        textPassword = findViewById(R.id.textPassword);
        noAccount = findViewById(R.id.noAccount);

        noAccount.setOnClickListener(this);
        buttonLog.setOnClickListener(this);

        if(BackendAPI.isAuthenticated()) {
            Intent intent = new Intent(this, HubActivity.class);
            startActivity(intent);
        }

    }

    /**
     * Method for processing the login. Is called from login button
     */
    private void processLogin() {
        Handler handler = new Handler();
        Thread thread = new Thread(() -> {
            if (!BackendAPI.Authenticate(textEmail.getText().toString(), textPassword.getText().toString(), BackendAPI.getIP(LoginActivity.this))) {
                Looper.prepare();

                Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_LONG).show();

                handler.post(() -> textPassword.setText(""));

                return;
            }

            FirebaseMessaging.getInstance().subscribeToTopic("spots")
                    .addOnCompleteListener(task -> {
                        String msg = "Success!";
                        if (!task.isSuccessful()) {
                            msg = "Fail!";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    });

            Helper.wipeFile();
            Helper.writeToFile(textEmail.getText().toString(), getApplicationContext());
            Helper.writeToFile(textPassword.getText().toString(), getApplicationContext());

            Intent intent = new Intent(LoginActivity.this, HubActivity.class);
            startActivity(intent);
        });
        thread.start();
    }

    int backButtonCount = 0;
    @Override
    public void onBackPressed() {
        if(backButtonCount >= 1)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLog:
                processLogin();
                break;
            case R.id.noAccount:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
        }
    }
}