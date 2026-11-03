package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RegisterUserDuplicateEmailTest {

    private static String body(String email) {
        return "{\"name\":\"Gina\",\"email\":\"" + email + "\",\"password\":\"S3cret-Passw0rd!\"}";
    }

    @Test
    void duplicateEmailIsRejectedIncludingDifferentCase() {
        String email = "dup" + System.nanoTime() + "@example.com";
        given().contentType("application/json").body(body(email))
            .when().post("/users").then().statusCode(201);

        given().contentType("application/json").body(body(email.toUpperCase()))
            .when().post("/users").then().statusCode(409)
            .body("message", notNullValue());
    }

    @Test
    void validationFailureReturnsErrorResponseWithMessage() {
        given().contentType("application/json")
            .body("{\"name\":\"\",\"email\":\"x@example.com\",\"password\":\"p\"}")
            .when().post("/users").then().statusCode(400)
            .body("message", containsString("name is required"));
    }
}
