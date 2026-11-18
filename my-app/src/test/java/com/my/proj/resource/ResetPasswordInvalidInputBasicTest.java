package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/** DEMO-104: from the generated spec tests/api/DEMO-99/test-11.txt. */
@QuarkusTest
class ResetPasswordInvalidInputBasicTest {

    @Test
    void invalidInputsReturn400() {
        Map<String, Object> resetRequest = new HashMap<>();
        resetRequest.put("email", "invalid-email");
        resetRequest.put("currentPassword", "OldP@ssw0rd1");
        resetRequest.put("newPassword", "NewP@ssw0rd2");

        given().contentType(ContentType.JSON)
            .body(resetRequest)
            .when().post("/users/reset-password")
            .then().statusCode(400);
    }
}
