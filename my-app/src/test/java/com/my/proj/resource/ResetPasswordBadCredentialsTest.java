package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

/** DEMO-104: from the generated spec tests/api/DEMO-99/test-5.txt. */
@QuarkusTest
class ResetPasswordBadCredentialsTest {

    private static final String OLD_PASSWORD = "OldP@ssw0rd1";
    private static final String NEW_PASSWORD = "NewP@ssw0rd2";

    /** Registers a user through POST /users and returns the email it was registered with. */
    private String registerUser(String email) {
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Reset Tester", "email", email, "password", OLD_PASSWORD))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    private String uniqueEmail() {
        return "reset-" + UUID.randomUUID() + "@example.com";
    }

    private Response reset(Object email, Object current, Object next) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("email", email);
        body.put("currentPassword", current);
        body.put("newPassword", next);
        return given().contentType(ContentType.JSON).body(body)
            .when().post("/users/reset-password");
    }

    private static final String GENERIC_MESSAGE = "Invalid email or password";

    @Test
    void wrongCurrentPasswordReturns401WithGenericMessage() {
        String email = registerUser(uniqueEmail());

        reset(email, "WrongP@ss9", NEW_PASSWORD).then()
            .statusCode(401)
            .body(containsString(GENERIC_MESSAGE));
    }

    @Test
    void unknownEmailReturns401WithGenericMessage() {
        reset(uniqueEmail(), OLD_PASSWORD, NEW_PASSWORD).then()
            .statusCode(401)
            .body(containsString(GENERIC_MESSAGE));
    }

    @Test
    void unknownEmailAndWrongPasswordGiveTheSameResponse() {
        String known = registerUser(uniqueEmail());

        String wrongPassword = reset(known, "WrongP@ss9", NEW_PASSWORD).then()
            .statusCode(401).extract().asString();
        String unknownEmail = reset(uniqueEmail(), OLD_PASSWORD, NEW_PASSWORD).then()
            .statusCode(401).extract().asString();

        // Nothing in the response may reveal which emails are registered.
        org.junit.jupiter.api.Assertions.assertEquals(wrongPassword, unknownEmail);
    }
}
