package com.fontys.vistaapplication.API;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Consumer;

public class BackendAPI {

    public static final String URL = "http://213.34.128.195:3000";
    private static String TOKEN = null;
    public static Optional<JSONObject> last_result;

    /**
     * Convert bitmap to data type & convert data type back to bitmap
     */
    public static String ConvertImageToData(Bitmap image, String extension) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.valueOf(extension), 100, baos);

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
    public static Bitmap ConvertDataToImage(String data) {
        byte[] decodedByteArray = Base64.getDecoder().decode(data);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    /**
     * Resets the JWT
     */
    public static void ResetJWT() {
        TOKEN = null;
    }

    /**
     * Check if JWT is set
     */
    public static boolean IsJWTPresent() {
        return TOKEN != null;
    }

    /**
     * Create HTTP request to URL and add required information to this request
     *
     * @param properties - Properties you want to send with your request,
     * @param endpoint   - The specific endpoint of the API you want to request from
     * @param method     - API method (POST/GET/PATCH/DELETE)
     * @return JSONObject of backend api response
     */
    private static Optional<JSONObject> _send(PropertyConstructor properties, String endpoint, String method) {

        //Initialize variables into null
        java.net.URL url = null;
        HttpURLConnection con = null;

        //Response variables
        StringBuilder response = new StringBuilder();
        byte[] data = null;

        //Convert added properties to JSON then to BYTE[]
        if (properties != null) //Meaning you got no parameters
            data = properties.construct().getBytes(StandardCharsets.UTF_8);

        try {
            //Initialize data and setup connection
            url = new URL(URL + "/" + endpoint);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);

            //Set timeouts
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            //Add json info body & headers
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "*/*");

            //If authorized add Authorization header
            if (TOKEN != null) con.setRequestProperty("Authorization", "Bearer " + TOKEN);

            //If there are added properties write them to connections output stream
            if (data != null) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(data, 0, data.length);
                }
            }

            //Get the status of the connection
            int status = con.getResponseCode();
            if(status != 200) {
                System.out.println("Received a request with status: " + status);
            }
            //System.out.println("STATUS: " + status);

            //Read the Inputstream from the connection into the response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {

                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                //System.out.println(response.toString());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //If connection was created disconnect it
        if (con != null) con.disconnect();

        //Return result from connection after json conversion
        return fromJson(response.toString().trim());
    }

    /**
     * Send a request to backend API and get result back in JSONObject
     *
     * @param properties - Properties you want to send with your request,
     * @param endpoint   - The specific endpoint of the API you want to request from
     */
    public static Optional<JSONObject> Send(PropertyConstructor properties, String endpoint) {
        return _send(properties, endpoint, "GET");
    }
    public static void Send(PropertyConstructor properties, String endpoint, Consumer<JSONObject> callback) {
        Send(properties, endpoint, "GET", callback);
    }

    /**
     * Send a request to backend API and get result back in JSONObject
     *
     * @param properties - Properties you want to send with your request,
     * @param endpoint   - The specific endpoint of the API you want to request from
     * @param method     - API method (POST/GET/PATCH/DELETE)
     */
    public static Optional<JSONObject> Send(PropertyConstructor properties, String endpoint, String method) {
        return _send(properties, endpoint, method);
    }
    public static void Send(PropertyConstructor properties, String endpoint, String method, Consumer<JSONObject> callback) {
        Handler handler = new Handler();
        Thread thread = new Thread(() -> {
            if (Looper.myLooper() == null) Looper.prepare();
            Optional<JSONObject> response = _send(properties, endpoint, method);
            if(!response.isPresent()) return; //No response ERROR on URL

            handler.post(() -> {
                try {
                    callback.accept(response.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        thread.start();
    }

    /**
     * Convert a json string into JSONObject
     *
     * @param json json formatted string
     * @return JSONObject
     */
    public static Optional<JSONObject> fromJson(String json) {
        Optional<JSONObject> result = Optional.empty();
        JSONObject jsonObject = new JSONObject();

        try {
            if(!json.startsWith("[")) {
                jsonObject = new JSONObject(json);
            } else {
                JSONArray array = new JSONArray(json);
                jsonObject.put("data", array);
            }
            result = Optional.of(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        last_result = result;
        return result;
    }

    /**
     * Authenticate client for backend API
     *
     * @param email    - Email from account to auth
     * @param password - Password from account to auth
     * @return If authentication is successful.
     */
    public static boolean Authenticate(String email, String password, String ip) {

        Optional<JSONObject> response = Send(new PropertyConstructor()
                .addProperty("email", email)
                .addProperty("password", password)
                .addProperty("ip", ip), "signin", "POST");
        if (!response.isPresent()) return false;

        try {
            //Initialize token from sign in
            String t = response.get().getString("jwt_token");
            TOKEN = t;
        } catch (JSONException e) {
            //e.printStackTrace();
            TOKEN = null;
        }

        return TOKEN != null;
    }

    /**
     * Check if a users is signed in!
     * @return If JWT token is set
     */
    public static boolean isAuthenticated() {
        return TOKEN != null;
    }

    /**
     * Resets session token to null. By doing this you will need to run authenticate again to be signed in!
     */
    public boolean logout() {
        TOKEN = null;

        return true;
    }

    /**
     * Get WifiManager from system and get IPv4 from user
     *
     * @param context Activity that is active
     * @return Returns IPv4 for user
     */
    public static String getIP(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

}
