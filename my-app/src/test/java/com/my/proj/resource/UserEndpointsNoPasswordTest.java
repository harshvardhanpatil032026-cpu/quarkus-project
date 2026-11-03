package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UserEndpointsNoPasswordTest {
    @Test
    void getEndpointsNeverReturnPasswordOrHash() {
        String email = "u" + System.nanoTime() + "@example.com";
        int id = given().contentType("application/json")
            .body("{\"name\":\"Dave\",\"email\":\"" + email + "\",\"password\":\"S3cret-Passw0rd!\"}")
            .when().post("/users").then().statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        given().when().get("/users/" + id).then().statusCode(200)
            .body("$", not(hasKey("password")))
            .body("$", not(hasKey("passwordHash")))
            .body("$", not(hasKey("password_hash")));

        String list = given().when().get("/users").then().statusCode(200).extract().asString();
        org.junit.jupiter.api.Assertions.assertFalse(list.toLowerCase().contains("password"));
    }
}
