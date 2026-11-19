package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.my.proj.resource.TestAuth.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** DEMO-111: from the generated spec tests/api/DEMO-90/test-2.txt (criterion 2). */
@QuarkusTest
class LoginSuccessReturnsJwtTest {

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

    @Test
    void successfulLoginReturnsAJwtInTheBody() {
        String email = registerUser();

        String token = loginToken(email);

        assertEquals(3, token.split("\\.").length, "token is a three-part JWT");
    }

    @Test
    void jwtCarriesTheAuthenticatedUsersIdentity() {
        String email = registerUser();

        String claims = claimsOf(loginToken(email));

        assertFalse(!claims.toLowerCase().contains(email.toLowerCase())
                && !claims.contains("sub"), "claims identify the user");
    }

    @Test
    void responseNeverIncludesThePasswordOrItsHash() {
        String email = registerUser();

        login(email, PASSWORD).statusCode(200)
            .body(not(containsString(PASSWORD)))
            .body(not(containsString("$2a$")))
            .body(not(containsString("password_hash")))
            .body(not(containsString("passwordHash")));
    }
}
