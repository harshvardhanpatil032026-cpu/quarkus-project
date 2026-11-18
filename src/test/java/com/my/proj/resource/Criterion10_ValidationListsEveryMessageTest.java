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
 * Acceptance criterion: Validation failure returns a 400 status code with all messages listed.
 *
 * Assumes PUT /users/email/{email}/password with {"currentPassword", "newPassword"} -> 204; 400 ErrorResponse with every validation message (joined by "; "); 401 "Invalid email or password". Assumes PATCH /users/email/{email} with {"name": ...} or {"email": ...} -> 200 and the user (id, name, email).
 */
@QuarkusTest
public class Criterion10_ValidationListsEveryMessageTest {

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
    void everyBrokenPasswordRuleIsListed() {
        String email = register();

        // Too short, and no uppercase letter, digit or special character: four rules at once.
        String message = changePassword(email, PASSWORD, "abc")
            .statusCode(400)
            .contentType(ContentType.JSON)
            .extract().path("message");

        assertNotNull(message);
        assertTrue(message.split(";\\s*").length >= 4, "every message is listed, not just the first: " + message);
    }

    @Test
    void everyBrokenNameRuleIsListed() {
        String email = register();

        // One character long and a digit: two rules at once.
        String message = update(email, Map.of("name", "7"))
            .statusCode(400)
            .contentType(ContentType.JSON)
            .extract().path("message");

        assertNotNull(message);
        assertTrue(message.split(";\\s*").length >= 2, "every message is listed, not just the first: " + message);
    }
}
