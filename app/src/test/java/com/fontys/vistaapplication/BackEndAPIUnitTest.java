package com.fontys.vistaapplication;

import org.junit.Test;

import static org.junit.Assert.*;

import com.fontys.vistaapplication.API.BackendAPI;
import com.fontys.vistaapplication.API.PropertyConstructor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Unit test for BackEndAPI controller.
 */
public class BackEndAPIUnitTest {
    private final String passRight = "string";
    private final String passWrong = "wrong";

    private final String emailRight = "email@email.com";
    private final String emailWrong = "email.com";

    @Test
    public void ping_test() {
        Optional<JSONObject> obj = BackendAPI.Send(null, "ping", "GET");
        try {
            assertEquals("Hello from Loopback", obj.get().getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
            assertTrue(obj.isPresent());
        }
    }

    @Test
    public void signin_test_success() {
        assertTrue(BackendAPI.Authenticate(emailRight, passRight, ""));
    }

    @Test
    public void signin_test_fail() {
        assertFalse(BackendAPI.Authenticate(emailWrong, passWrong, ""));
    }

    @Test
    public void signin_token_test() {
        Optional<JSONObject> obj = BackendAPI.Send(new PropertyConstructor().addProperty("email", emailRight).addProperty("password", passRight).addProperty("ip", "string"), "signin", "POST");
        try {
            assertFalse(obj.get().getString("jwt_token").isEmpty());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void signup_test_fail_wrong_email() {
        Optional<JSONObject> obj = BackendAPI.Send(new PropertyConstructor().addProperty("email", emailWrong).addProperty("password", passWrong), "signup", "POST");
        assertFalse(obj.isPresent());
    }

    @Test
    public void signup_test_fail_already_registered() {
        Optional<JSONObject> obj = BackendAPI.Send(new PropertyConstructor().addProperty("email", emailRight).addProperty("password", passRight), "signup", "POST");
        assertFalse(obj.isPresent());
    }


}