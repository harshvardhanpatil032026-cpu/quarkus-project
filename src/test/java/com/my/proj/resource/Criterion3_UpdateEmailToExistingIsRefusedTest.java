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
 * Acceptance criterion: A user cannot update their email to one that already exists in the system.
 *
 * Assumes PATCH /users/email/{email} with {"name": ...} or {"email": ...} -> 200 and the user (id, name, email). A taken email -> 409 ErrorResponse (EmailAlreadyExistsException).
 */
@QuarkusTest
public class Criterion3_UpdateEmailToExistingIsRefusedTest {

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
    void anotherUsersEmailIsRefusedWith409AndNothingChanges() {
        String mine = register("First User");
        String theirs = register("Second User");

        update(mine, Map.of("email", theirs))
            .statusCode(409)
            .body("message", containsStringIgnoringCase("already exists"));

        assertEquals(1, usersWithEmail(mine));
        assertEquals(1, usersWithEmail(theirs));
    }

    @Test
    void theSameEmailInOtherLetterCaseIsAlsoTaken() {
        String mine = register("First User");
        String theirs = register("Second User");

        update(mine, Map.of("email", "  " + theirs.toUpperCase() + "  ")).statusCode(409);

        assertEquals(1, usersWithEmail(mine));
    }

    @Test
    void aFreeEmailCanBeTaken() {
        String mine = register("First User");
        String free = ("free-" + UUID.randomUUID() + "@example.com").toLowerCase();

        update(mine, Map.of("email", free)).statusCode(200).body("email", equalTo(free));

        assertEquals(0, usersWithEmail(mine));
        assertEquals(1, usersWithEmail(free));
    }
}
