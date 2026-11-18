package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance criterion: Updating a password requires it to be 8-64 characters long, with at least one uppercase letter, one lowercase letter, one digit, and one special character.
 *
 * Assumes PUT /users/email/{email}/password with {"currentPassword", "newPassword"} -> 204; 400 ErrorResponse with every validation message (joined by "; "); 401 "Invalid email or password".
 */
@QuarkusTest
public class Criterion7_PasswordStrengthRulesTest {

    static final String PASSWORD = "Old#Passw0rd";

    @Inject
    EntityManager em;

    /** Registers a user through POST /users and returns the stored (lower-cased) email. */
    String register(String name) {
        String email = ("u-" + UUID.randomUUID() + "@example.com").toLowerCase();
        given().contentType(ContentType.JSON)
            .body(Map.of("name", name, "email", email, "password", PASSWORD))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    String register() {
        return register("Account Tester");
    }

    /** PATCH /users/email/{email} with the given fields (the story's update endpoint). */
    ValidatableResponse update(String email, Map<String, Object> fields) {
        return given().contentType(ContentType.JSON).body(fields)
            .when().patch("/users/email/{email}", email)
            .then();
    }

    /** PUT /users/email/{email}/password (the story's password endpoint). A null value is sent as null. */
    ValidatableResponse changePassword(String email, String current, String next) {
        Map<String, Object> body = new HashMap<>();
        body.put("currentPassword", current);
        body.put("newPassword", next);
        return given().contentType(ContentType.JSON).body(body)
            .when().put("/users/email/{email}/password", email)
            .then();
    }

    Object column(String column, String email) {
        List<?> rows = em.createNativeQuery("select " + column + " from users where email = :e")
            .setParameter("e", email).getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    long usersWithEmail(String email) {
        return ((Number) em.createNativeQuery("select count(*) from users where email = :e")
            .setParameter("e", email).getSingleResult()).longValue();
    }

    @Test
    void aPasswordMeetingEveryRuleIsAccepted() {
        String email = register();
        changePassword(email, PASSWORD, "Aa1!aaaa").statusCode(204); // exactly 8
    }

    @Test
    void sixtyFourCharactersIsTheMaximum() {
        String email = register();
        String sixtyFour = "Aa1!" + "x".repeat(60);
        changePassword(email, PASSWORD, sixtyFour).statusCode(204);

        String sixtyFive = "Bb2@" + "y".repeat(61);
        changePassword(email, sixtyFour, sixtyFive).statusCode(400);
    }

    @Test
    void eachMissingRuleIsRefused() {
        String email = register();
        Object before = column("password_hash", email);
        for (String weak : List.of(
                "Aa1!aaa",          // 7 characters
                "aa1!aaaa",         // no uppercase
                "AA1!AAAA",         // no lowercase
                "Aa!aaaaa",         // no digit
                "Aa1aaaaa")) {      // no special character
            changePassword(email, PASSWORD, weak).statusCode(400);
        }
        assertEquals(before, column("password_hash", email), "a refused password changes nothing");
    }
}
