package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class DatabaseResourceTest {

    @Test
    void reportsTheConnectedDatabase() {
        given()
            .when().get("/db")
            .then()
            .statusCode(200)
            .body(startsWith("Connected to: "));
    }
}
