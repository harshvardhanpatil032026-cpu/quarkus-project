package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RegisterUserInvalidEmailTest {
    @Test
    void invalidEmailFormatIsRejectedAndNoUserCreated() {
        String bad = "not-an-email-" + System.nanoTime();
        given().contentType("application/json")
            .body("{\"name\":\"Frank\",\"email\":\"" + bad + "\",\"password\":\"S3cret-Passw0rd!\"}")
            .when().post("/users")
            .then().statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)));
        given().when().get("/users").then().statusCode(200).body("email", not(hasItem(bad)));
    }
}
