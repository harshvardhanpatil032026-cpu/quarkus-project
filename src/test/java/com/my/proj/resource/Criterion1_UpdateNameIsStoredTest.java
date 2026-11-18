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
 * Acceptance criterion: A user can successfully update their name and the change is reflected in the database.
 *
 * Assumes PATCH /users/email/{email} with {"name": ...} or {"email": ...} -> 200 and the user (id, name, email).
 */
@QuarkusTest
public class Criterion1_UpdateNameIsStoredTest {

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
    void updatedNameIsReturnedAndStored() {
        String email = register("Before Name");

        update(email, Map.of("name", "After Name"))
            .statusCode(200)
            .body("name", equalTo("After Name"))
            .body("email", equalTo(email))
            .body("$", not(hasKey("password")))
            .body("$", not(hasKey("passwordHash")));

        assertEquals("After Name", column("name", email));
        given().when().get("/users/email/{email}", email).then()
            .statusCode(200).body("name", equalTo("After Name"));
    }

    @Test
    void updatingTheNameKeepsThePassword() {
        String email = register("Keep Password");
        Object hashBefore = column("password_hash", email);

        update(email, Map.of("name", "Renamed User")).statusCode(200);

        assertEquals(hashBefore, column("password_hash", email));
    }
}
