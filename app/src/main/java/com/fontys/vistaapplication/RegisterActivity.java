package com.fontys.vistaapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fontys.vistaapplication.API.BackendAPI;
import com.fontys.vistaapplication.API.PropertyConstructor;

import org.json.JSONException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText textUsernameregister;
    private EditText textPasswordregister;
    private EditText textPasswordregister2;
    private EditText textEmailregister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button buttonReg = findViewById(R.id.buttonReg);
        textUsernameregister = findViewById(R.id.textUsernameregister);
        textPasswordregister = findViewById(R.id.textPasswordregister);
        textPasswordregister2 = findViewById(R.id.textPasswordregister2);
        textEmailregister = findViewById(R.id.textEmailregister);
        TextView alreadyReg = findViewById(R.id.alreadyReg);

        buttonReg.setOnClickListener(this);
        alreadyReg.setOnClickListener(this);
}

    /**
     * Method for processing the register. Is called from register button
     */
    private void processRegister(){
        String p1 = textPasswordregister.getText().toString();
        String p2 = textPasswordregister2.getText().toString();

        if (!p1.equals(p2)){
            Toast.makeText(getApplicationContext(), R.string.passwords_do_not_match, Toast.LENGTH_LONG).show();
            return;
        }

        BackendAPI.Send(new PropertyConstructor()
                .addProperty("username", textUsernameregister.getText().toString())
                .addProperty("password", p1)
                .addProperty("email", textEmailregister.getText().toString())
                .addProperty("ip_reg", BackendAPI.getIP(RegisterActivity.this)), "signup", "POST", (data) -> {
            if (data.has("ERROR")) {

                try {
                    String keyValue = data.getString("ERROR");
                    int resID = getResources().getIdentifier(keyValue, "string", getPackageName());
                    Toast.makeText(getApplicationContext(), getString(resID), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }

            Toast.makeText(getApplicationContext(), "Login to confirm registration", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonReg:
                processRegister();
                break;
            case R.id.alreadyReg:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }
}