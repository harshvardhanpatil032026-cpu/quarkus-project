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
 * Acceptance criterion: A user can successfully update their password, which is stored as a bcrypt hash in the database.
 *
 * Assumes PUT /users/email/{email}/password with {"currentPassword", "newPassword"} -> 204; 400 ErrorResponse with every validation message (joined by "; "); 401 "Invalid email or password".
 */
@QuarkusTest
public class Criterion2_UpdatePasswordStoredAsBcryptTest {

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
    void newPasswordIsStoredAsABcryptHash() {
        String email = register();
        String next = "New#Passw0rd1";
        Object before = column("password_hash", email);

        changePassword(email, PASSWORD, next).statusCode(204).body(emptyOrNullString());

        String stored = (String) column("password_hash", email);
        assertNotNull(stored);
        assertNotEquals(before, stored);
        assertFalse(stored.contains(next));
        assertTrue(stored.matches("^\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}$"), "a bcrypt hash: " + stored);
    }

    @Test
    void theNewPasswordIsTheCurrentOneAfterwards() {
        String email = register();
        changePassword(email, PASSWORD, "New#Passw0rd1").statusCode(204);

        // The old password no longer verifies; the new one does.
        changePassword(email, PASSWORD, "Third#Passw0rd2").statusCode(401);
        changePassword(email, "New#Passw0rd1", "Third#Passw0rd2").statusCode(204);
    }
}
