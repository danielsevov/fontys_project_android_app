package com.fontys.vistaapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fontys.vistaapplication.API.BackendAPI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BackendAPI.Send(null, "spots", "GET", (data) -> {

            try {
                JSONArray spots = data.getJSONArray("data");
                JSONObject spot0 = spots.getJSONObject(0);

                Toast.makeText(getApplicationContext(), "Found spot: " + spot0.getString("title"), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }



            //{
            //    "id": 0,
            //    "title": "string",
            //    "coordinates": "string",
            //    "category": "string",
            //    "range": 0,
            //    "date_created": "string",
            //    "date_modified": "string",
            //    "creator": "string",
            //    "additionalProp1": {}
            //  }

        });

    }
}
