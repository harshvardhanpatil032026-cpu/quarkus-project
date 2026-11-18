package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/** DEMO-111: from the generated spec tests/api/DEMO-90/test-1.txt (criterion 1). */
@QuarkusTest
class LoginAcceptsEmailAndPasswordTest {

    // Assumed contract: POST /users/login with {"email","password"} returns {"token": "<jwt>"}.
    private static final String LOGIN_PATH = "/users/login";
    private static final String PASSWORD = "SecureP@ss1";

    /** Registers a user through POST /users and returns the email it was registered with. */
    private String registerUser() {
        String email = "login-" + UUID.randomUUID() + "@example.com";
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Login Tester", "email", email, "password", PASSWORD))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    private Response login(Object email, Object password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        return given().contentType(ContentType.JSON).body(body)
            .when().post(LOGIN_PATH);
    }

    @Test
    void postWithEmailAndPasswordIsAccepted() {
        String email = registerUser();

        login(email, PASSWORD).then().statusCode(200);
    }

    @Test
    void emailIsMatchedAfterTrimmingAndLowercasing() {
        String email = registerUser();

        login("  " + email.toUpperCase() + "  ", PASSWORD).then().statusCode(200);
    }

    @Test
    void missingEmailIsRejectedWith400() {
        login(null, PASSWORD).then().statusCode(400);
    }

    @Test
    void blankPasswordIsRejectedWith400() {
        login(registerUser(), "").then().statusCode(400);
    }

    @Test
    void badlyFormattedEmailIsRejectedWith400() {
        login("not-an-email", PASSWORD).then().statusCode(400);
    }
}
