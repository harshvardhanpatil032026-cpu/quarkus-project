package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/** DEMO-112: a blank email is rejected with 400 and an ErrorResponse body (guardrail 6). */
@QuarkusTest
class GetUserByEmailInvalidInputTest {

    @Test
    void blankEmailReturns400WithMessage() {
        given()
            .urlEncodingEnabled(false)
            .when().get("/users/email/%20%20")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Email cannot be null or empty"));
    }
}
