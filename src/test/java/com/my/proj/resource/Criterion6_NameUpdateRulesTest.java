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
 * Acceptance criterion: Updating a name trims spaces at the start and end, and only allows letters (any language), spaces, hyphen, apostrophe and period; 2-100 characters long.
 *
 * Assumes PATCH /users/email/{email} with {"name": ...} or {"email": ...} -> 200 and the user (id, name, email). An invalid name -> 400 ErrorResponse.
 */
@QuarkusTest
public class Criterion6_NameUpdateRulesTest {

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
    void surroundingSpacesAreTrimmed() {
        String email = register();
        update(email, Map.of("name", "   Ann Lee   ")).statusCode(200).body("name", equalTo("Ann Lee"));
        assertEquals("Ann Lee", column("name", email));
    }

    @Test
    void lettersOfAnyLanguageAndTheAllowedMarksAreAccepted() {
        String email = register();
        for (String name : List.of("José O'Neil-Smith Jr.", "Zoë Ångström", "李小龙", "Ольга Петрова", "Jo")) {
            update(email, Map.of("name", name)).statusCode(200).body("name", equalTo(name));
        }
    }

    @Test
    void otherCharactersAreRefused() {
        String email = register("Original Name");
        for (String name : List.of("John3", "Ann_Lee", "Ann@Lee", "<b>Ann</b>", "Ann!")) {
            update(email, Map.of("name", name)).statusCode(400);
        }
        assertEquals("Original Name", column("name", email));
    }

    @Test
    void lengthIsTwoToOneHundredAfterTrimming() {
        String email = register("Original Name");
        update(email, Map.of("name", "A")).statusCode(400);
        update(email, Map.of("name", "  A  ")).statusCode(400);
        update(email, Map.of("name", "")).statusCode(400);
        update(email, Map.of("name", "a".repeat(101))).statusCode(400);
        update(email, Map.of("name", "a".repeat(100))).statusCode(200);
        update(email, Map.of("name", "  " + "b".repeat(100) + "  ")).statusCode(200).body("name", equalTo("b".repeat(100)));
    }
}
