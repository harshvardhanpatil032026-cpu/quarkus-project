package com.my.proj.resource;

import static com.my.proj.resource.TestAuth.given;
import static org.hamcrest.Matchers.containsString;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/** DEMO-99, product owner's answer: a new password must be at least 10 characters long. */
@QuarkusTest
class ResetPasswordNewPasswordMinLengthTest {

    private static final String CURRENT = "OldP@ssw0rd1";

    private String registerUser() {
        String email = "reset-" + UUID.randomUUID() + "@example.com";
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Reset Tester", "email", email, "password", CURRENT))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    private io.restassured.response.ValidatableResponse reset(String email, String next) {
        return given().contentType(ContentType.JSON)
            .body(Map.of("email", email, "currentPassword", CURRENT, "newPassword", next))
            .when().post("/users/reset-password")
            .then();
    }

    @Test
    void newPasswordOfNineCharactersIsRejectedWith400() {
        String email = registerUser();

        reset(email, "Nine#Pas9")
            .statusCode(400)
            .body("message", containsString("newPassword must be at least 10 characters"));

        // Nothing changed: the current password still works for a valid reset.
        reset(email, "Valid#Pass10").statusCode(204);
    }

    @Test
    void newPasswordOverBcryptsSeventyTwoByteLimitIsRejectedWith400() {
        String email = registerUser();

        // 73 ASCII characters is 73 bytes, one more than bcrypt reads.
        reset(email, "A".repeat(73))
            .statusCode(400)
            .body("message", containsString("newPassword must be at most 72 bytes"));

        // Exactly 72 bytes is accepted.
        reset(email, "B".repeat(72)).statusCode(204);
    }

    @Test
    void newPasswordOfExactlyTenCharactersIsAccepted() {
        reset(registerUser(), "Ten#Pass10").statusCode(204);
    }
}
