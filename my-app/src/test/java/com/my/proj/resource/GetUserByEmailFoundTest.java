package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/** DEMO-112, criterion 1 (tests/api/DEMO-112/test-1.txt): a registered email returns that user as JSON. */
@QuarkusTest
class GetUserByEmailFoundTest {

    @Test
    void returnsTheUserRegisteredWithThatEmail() {
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "John Doe", "email", "john.doe@example.com", "password", "S3cret-Passw0rd!"))
            .when().post("/users")
            .then().statusCode(201);

        given()
            .when().get("/users/email/john.doe@example.com")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("name", equalTo("John Doe"))
            .body("email", equalTo("john.doe@example.com"))
            .body("$", not(hasKey("password")))
            .body("$", not(hasKey("passwordHash")));
    }
}
