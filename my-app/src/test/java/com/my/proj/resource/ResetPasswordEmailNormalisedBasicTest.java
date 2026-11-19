package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.my.proj.resource.TestAuth.given;

/** DEMO-104: from the generated spec tests/api/DEMO-99/test-10.txt. */
@QuarkusTest
class ResetPasswordEmailNormalisedBasicTest {

    private String registerUser(String email, String password) {
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Tester", "email", email, "password", password))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    @Test
    void emailIsTrimmedAndLowercased() {
        String local = "reset-" + UUID.randomUUID();
        // Registration does not trim before @Email, so register mixed case only; the reset adds the spaces.
        registerUser("Reset-" + local.substring(6).toUpperCase() + "@Example.com", "OldP@ssw0rd1");
        Map<String, Object> resetRequest = new HashMap<>();
        resetRequest.put("email", " " + local + "@example.com ");
        resetRequest.put("currentPassword", "OldP@ssw0rd1");
        resetRequest.put("newPassword", "NewP@ssw0rd2");

        given().contentType(ContentType.JSON)
            .body(resetRequest)
            .when().post("/users/reset-password")
            .then().statusCode(204);
    }
}
