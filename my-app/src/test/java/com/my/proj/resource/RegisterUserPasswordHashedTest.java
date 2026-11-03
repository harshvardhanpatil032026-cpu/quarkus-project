package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RegisterUserPasswordHashedTest {
    @Inject
    EntityManager em;

    @Test
    void storedPasswordIsAHashNotThePlainText() {
        String email = "u" + System.nanoTime() + "@example.com";
        String plain = "S3cret-Passw0rd!";
        given().contentType("application/json")
            .body("{\"name\":\"Bob\",\"email\":\"" + email + "\",\"password\":\"" + plain + "\"}")
            .when().post("/users").then().statusCode(anyOf(is(200), is(201)));

        // Column name password_hash is specified by the task plan for this story.
        String stored = (String) em.createNativeQuery("select password_hash from users where email = :e")
            .setParameter("e", email).getSingleResult();
        assertNotNull(stored);
        assertNotEquals(plain, stored);
        assertFalse(stored.contains(plain));
        assertTrue(stored.length() >= 32);
    }
}
