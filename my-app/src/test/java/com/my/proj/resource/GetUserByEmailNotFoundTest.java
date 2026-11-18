package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/** DEMO-112, criterion 2 (tests/api/DEMO-112/test-2.txt): an unknown email returns 404 with a message. */
@QuarkusTest
class GetUserByEmailNotFoundTest {

    @Test
    void unknownEmailReturns404WithMessage() {
        given()
            .when().get("/users/email/nonexistent@example.com")
            .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("message", equalTo("User not found with email: nonexistent@example.com"));
    }
}
