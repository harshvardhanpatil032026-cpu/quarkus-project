package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** DEMO-111: from the generated spec tests/api/DEMO-90/test-3.txt (criterion 3). */
@QuarkusTest
class LoginFailureGenericErrorTest {

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

    private static final String GENERIC_MESSAGE = "Invalid credentials";

    @Test
    void wrongPasswordReturns401WithGenericMessage() {
        String email = registerUser();

        login(email, "WrongP@ss9").then()
            .statusCode(401)
            .body(containsString(GENERIC_MESSAGE));
    }

    @Test
    void unknownEmailReturns401WithGenericMessage() {
        login("nobody-" + UUID.randomUUID() + "@example.com", PASSWORD).then()
            .statusCode(401)
            .body(containsString(GENERIC_MESSAGE));
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSameResponse() {
        String email = registerUser();

        String wrongPassword = login(email, "WrongP@ss9").then().statusCode(401).extract().asString();
        String unknownEmail = login("nobody-" + UUID.randomUUID() + "@example.com", PASSWORD).then()
            .statusCode(401).extract().asString();

        // Nothing in the response may reveal which part of the credentials was wrong.
        assertEquals(wrongPassword, unknownEmail);
    }

    @Test
    void failedLoginDoesNotLeakUserDetails() {
        String email = registerUser();

        login(email, "WrongP@ss9").then()
            .statusCode(401)
            .body(not(containsString(email)))
            .body(not(containsString("$2a$")))
            .body(not(containsString("password_hash")));
    }
}
