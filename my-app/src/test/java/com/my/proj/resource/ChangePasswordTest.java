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

import static com.my.proj.resource.TestAuth.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/** DEMO-126: PUT /users/email/{email}/password changes the password with the registration rules. */
@QuarkusTest
class ChangePasswordTest {

    private static final String PASSWORD = "Old#Passw0rd";
    private static final String NEW_PASSWORD = "New#Passw0rd1";

    @Inject
    EntityManager em;

    private String register(String name) {
        String email = "pw-" + UUID.randomUUID() + "@example.com";
        given().contentType(ContentType.JSON)
            .body(Map.of("name", name, "email", email, "password", PASSWORD))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    private ValidatableResponse change(String email, String current, String next) {
        Map<String, Object> body = new HashMap<>();
        body.put("currentPassword", current);
        body.put("newPassword", next);
        return given().contentType(ContentType.JSON).body(body)
            .when().put("/users/email/{email}/password", email)
            .then();
    }

    private String hash(String email) {
        return (String) em.createNativeQuery("select password_hash from users where email = :e")
            .setParameter("e", email).getSingleResult();
    }

    @Test
    void newPasswordIsStoredAsBcryptAndReplacesTheOldOne() {
        String email = register("Account Tester");
        String before = hash(email);

        change(email, PASSWORD, NEW_PASSWORD).statusCode(204).body(emptyOrNullString());

        String after = hash(email);
        assertNotEquals(before, after);
        assertTrue(after.matches("^\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}$"), after);
        change(email, PASSWORD, "Third#Passw0rd2").statusCode(401);
        change(email, NEW_PASSWORD, "Third#Passw0rd2").statusCode(204);
    }

    @Test
    void lengthLimitsAreEightToSixtyFour() {
        String email = register("Account Tester");
        change(email, PASSWORD, "Aa1!aaaa").statusCode(204);
        change(email, "Aa1!aaaa", "Aa1!aaa").statusCode(400);
        change(email, "Aa1!aaaa", "Bb2@" + "y".repeat(61)).statusCode(400);
        change(email, "Aa1!aaaa", "Bb2@" + "y".repeat(60)).statusCode(204);
    }

    @Test
    void weakPasswordsAreRefusedAndNothingChanges() {
        String email = register("Account Tester");
        String before = hash(email);
        for (String weak : List.of("aa1!aaaa", "AA1!AAAA", "Aa!aaaaa", "Aa1aaaaa")) {
            change(email, PASSWORD, weak).statusCode(400);
        }
        assertEquals(before, hash(email));
    }

    @Test
    void everyBrokenRuleIsListed() {
        String message = change(register("Account Tester"), PASSWORD, "abc")
            .statusCode(400).contentType(ContentType.JSON).extract().path("message");
        assertTrue(message.split(";\\s*").length >= 4, message);
    }

    @Test
    void formatAndPersonalRulesAreListedTogether() {
        String email = register("Marigold");

        String message = change(email, PASSWORD, "marigold")
            .statusCode(400).contentType(ContentType.JSON).extract().path("message");

        assertTrue(message.contains("uppercase letter"), message);
        assertTrue(message.contains("digit"), message);
        assertTrue(message.contains("special character"), message);
        assertTrue(message.contains("must not contain your name"), message);
    }

    @Test
    void passwordContainingEmailOrNameOrEqualToCurrentIsRefused() {
        String email = register("Marigold");
        String before = hash(email);

        change(email, PASSWORD, "X1!" + email).statusCode(400);
        change(email, PASSWORD, "MARIGOLD#2026a").statusCode(400);
        change(email, PASSWORD, PASSWORD).statusCode(400);
        assertEquals(before, hash(email));
    }

    @Test
    void currentPasswordIsRequired() {
        String email = register("Account Tester");
        change(email, null, NEW_PASSWORD).statusCode(400);
        change(email, "   ", NEW_PASSWORD).statusCode(400);
    }

    @Test
    void wrongCurrentPasswordOrUnknownUserGives401WithGenericMessage() {
        String email = register("Account Tester");
        String before = hash(email);

        change(email, "Wrong#Passw0rd", NEW_PASSWORD)
            .statusCode(401).body("message", equalTo("Invalid email or password"));
        change("nobody-" + UUID.randomUUID() + "@example.com", PASSWORD, NEW_PASSWORD)
            .statusCode(401).body("message", equalTo("Invalid email or password"));
        assertEquals(before, hash(email));
    }
}
