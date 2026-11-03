package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RegisterUserSuccessTest {
    @Test
    void validRegistrationReturnsSuccessWithIdNameAndEmail() {
        String email = "u" + System.nanoTime() + "@example.com";
        given().contentType("application/json")
            .body("{\"name\":\"Alice\",\"email\":\"" + email + "\",\"password\":\"S3cret-Passw0rd!\"}")
            .when().post("/users")
            .then().statusCode(201)
            .body("id", notNullValue())
            .body("name", is("Alice"))
            .body("email", is(email));
    }
}
