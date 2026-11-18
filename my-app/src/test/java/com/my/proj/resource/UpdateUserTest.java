package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/** DEMO-126: PATCH /users/email/{email} updates the name (and email) with the registration rules. */
@QuarkusTest
class UpdateUserTest {

    private static final String PASSWORD = "Old#Passw0rd";

    @Inject
    EntityManager em;

    private String register(String name) {
        String email = "update-" + UUID.randomUUID() + "@example.com";
        given().contentType(ContentType.JSON)
            .body(Map.of("name", name, "email", email, "password", PASSWORD))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    private ValidatableResponse update(String email, Map<String, Object> fields) {
        return given().contentType(ContentType.JSON).body(fields)
            .when().patch("/users/email/{email}", email)
            .then();
    }

    private Object column(String column, String email) {
        List<?> rows = em.createNativeQuery("select " + column + " from users where email = :e")
            .setParameter("e", email).getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Test
    void nameIsUpdatedTrimmedAndStoredWithoutTouchingThePassword() {
        String email = register("Before Name");
        Object hashBefore = column("password_hash", email);

        update(email, Map.of("name", "  After Name  "))
            .statusCode(200)
            .body("name", equalTo("After Name"))
            .body("email", equalTo(email))
            .body("$", not(hasKey("passwordHash")));

        assertEquals("After Name", column("name", email));
        assertEquals(hashBefore, column("password_hash", email));
    }

    @Test
    void namesInAnyLanguageWithAllowedMarksAreAccepted() {
        String email = register("Original Name");
        for (String name : List.of("José O'Neil-Smith Jr.", "李小龙", "Ольга", "Jo", "a".repeat(100))) {
            update(email, Map.of("name", name)).statusCode(200).body("name", equalTo(name));
        }
    }

    @Test
    void invalidNamesAreRefusedAndNothingChanges() {
        String email = register("Original Name");
        for (String name : List.of("A", "  A  ", "", "a".repeat(101), "John3", "Ann_Lee", "<b>Ann</b>")) {
            update(email, Map.of("name", name)).statusCode(400);
        }
        assertEquals("Original Name", column("name", email));
    }

    @Test
    void everyBrokenNameRuleIsListed() {
        String email = register("Original Name");
        String message = update(email, Map.of("name", "7")).statusCode(400).extract().path("message");
        assertTrue(message.contains("characters") && message.contains("letters"), message);
    }

    @Test
    void emptyBodyIsRefused() {
        update(register("Original Name"), Map.of()).statusCode(400);
    }

    @Test
    void unknownUserGives404() {
        String nobody = "nobody-" + UUID.randomUUID() + "@example.com";
        update(nobody, Map.of("name", "Any Name"))
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("message", equalTo("User not found with email: " + nobody));
    }

    @Test
    void emailOfAnotherUserIsRefusedInAnyCase() {
        String mine = register("First User");
        String theirs = register("Second User");

        update(mine, Map.of("email", " " + theirs.toUpperCase() + " "))
            .statusCode(409)
            .body("message", containsStringIgnoringCase("already exists"));
        assertEquals("First User", column("name", mine));
    }

    @Test
    void freeEmailCanBeTaken() {
        String mine = register("First User");
        String free = "free-" + UUID.randomUUID() + "@example.com";

        update(mine, Map.of("email", free)).statusCode(200).body("email", equalTo(free));
        assertNull(column("name", mine));
        assertEquals("First User", column("name", free));
    }
}
