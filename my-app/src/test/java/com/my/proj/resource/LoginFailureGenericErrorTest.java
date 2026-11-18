package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** DEMO-111: from the generated spec tests/api/DEMO-90/test-3.txt (criterion 3). */
@QuarkusTest
public class LoginFailureGenericErrorTest {

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

    private ValidatableResponse login(Object email, Object password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        return given().contentType(ContentType.JSON).body(body)
            .when().post(LOGIN_PATH)
            .then();
    }

    private String loginToken(String email) {
        return login(email, PASSWORD).statusCode(200).extract().path("token");
    }

    /** Decodes the claims of a JWT without verifying the signature. */
    private String claimsOf(String jwt) {
        String[] parts = jwt.split("\\.");
        assertEquals(3, parts.length, "a JWT has three dot-separated parts");
        return new String(Base64.getUrlDecoder().decode(parts[1]));
    }

    private long numericClaim(String claims, String name) {
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\"" + name + "\"\\s*:\\s*(\\d+)").matcher(claims);
        assertFalse(!m.find(), "claim '" + name + "' is present");
        return Long.parseLong(m.group(1));
    }

    private static final String GENERIC_MESSAGE = "Invalid credentials";

    @Test
    public void wrongPasswordReturns401WithGenericMessage() {
        String email = registerUser();

        login(email, "WrongP@ss9")
            .statusCode(401)
            .body(containsString(GENERIC_MESSAGE));
    }

    @Test
    public void unknownEmailReturns401WithGenericMessage() {
        login("nobody-" + UUID.randomUUID() + "@example.com", PASSWORD)
            .statusCode(401)
            .body(containsString(GENERIC_MESSAGE));
    }

    @Test
    public void wrongPasswordAndUnknownEmailGiveTheSameResponse() {
        String email = registerUser();

        String wrongPassword = login(email, "WrongP@ss9").statusCode(401).extract().asString();
        String unknownEmail = login("nobody-" + UUID.randomUUID() + "@example.com", PASSWORD)
            .statusCode(401).extract().asString();

        // Nothing in the response may reveal which part of the credentials was wrong.
        assertEquals(wrongPassword, unknownEmail);
    }

    @Test
    public void failedLoginDoesNotLeakUserDetails() {
        String email = registerUser();

        login(email, "WrongP@ss9")
            .statusCode(401)
            .body(not(containsString(email)))
            .body(not(containsString("$2a$")))
            .body(not(containsString("password_hash")));
    }
}
