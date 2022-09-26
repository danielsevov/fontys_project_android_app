package com.fontys.vistaapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HubActivity extends AppCompatActivity {
    private final String TAG = "HUB_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub); //This line throws the error RIP

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);

        NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);

        NavigationUI.setupWithNavController(navView, navController);


//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.firstFragment, R.id.secondFragment, R.id.thirdFragment)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupActionBarWithNavController(navView, navController);
    }

    int backButtonCount = 0;
    @Override
    public void onBackPressed() {
        if(backButtonCount >= 1)
        {
            Intent intent = new Intent(HubActivity.this, HubActivity.class);
            startActivity(intent);
        }
        else
        {
            backButtonCount++;
        }
    }
}