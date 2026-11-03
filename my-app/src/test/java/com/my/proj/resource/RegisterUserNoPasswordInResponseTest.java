package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RegisterUserNoPasswordInResponseTest {
    @Test
    void registrationResponseHasNoPasswordOrHashField() {
        String email = "u" + System.nanoTime() + "@example.com";
        String body = given().contentType("application/json")
            .body("{\"name\":\"Carol\",\"email\":\"" + email + "\",\"password\":\"S3cret-Passw0rd!\"}")
            .when().post("/users")
            .then().statusCode(201)
            .body("$", not(hasKey("password")))
            .body("$", not(hasKey("passwordHash")))
            .body("$", not(hasKey("password_hash")))
            .extract().asString();
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("S3cret-Passw0rd!"));
    }
}
