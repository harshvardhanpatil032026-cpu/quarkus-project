package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class Demo2AcceptanceTest1 {
    @Test
    void testDatabaseConnectionEndpoint() {
        given()
          .when().get("/db")
          .then()
             .statusCode(200)
             .body(containsString("Connected to:"));
    }
}
