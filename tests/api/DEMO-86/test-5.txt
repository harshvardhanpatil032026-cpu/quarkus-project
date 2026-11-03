package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RegisterUserMissingFieldsTest {
    @Test
    void missingNameEmailOrPasswordIsRejectedAndNoUserCreated() {
        String email = "u" + System.nanoTime() + "@example.com";
        String[] bodies = {
            "{\"email\":\"" + email + "\",\"password\":\"S3cret-Passw0rd!\"}",
            "{\"name\":\"Eve\",\"password\":\"S3cret-Passw0rd!\"}",
            "{\"name\":\"Eve\",\"email\":\"" + email + "\"}"
        };
        for (String b : bodies) {
            given().contentType("application/json").body(b)
                .when().post("/users")
                .then().statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)));
        }
        given().when().get("/users").then().statusCode(200).body("email", not(hasItem(email)));
    }
}
