package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.my.proj.resource.TestAuth.given;
import static org.hamcrest.Matchers.equalTo;

/** DEMO-104: from the generated spec tests/api/DEMO-99/test-13.txt. */
@QuarkusTest
class ResetPasswordBadCredentialsBasicTest {

    private String registerUser(String email, String password) {
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Tester", "email", email, "password", password))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    @Test
    void badCredentialsReturn401() {
        String email = registerUser("reset-" + UUID.randomUUID() + "@example.com", "OldP@ssw0rd1");
        Map<String, Object> resetRequest = new HashMap<>();
        resetRequest.put("email", email);
        resetRequest.put("currentPassword", "WrongP@ss");
        resetRequest.put("newPassword", "NewP@ssw0rd2");

        given().contentType(ContentType.JSON)
            .body(resetRequest)
            .when().post("/users/reset-password")
            .then().statusCode(401).and()
                  .body("message", equalTo("Invalid email or password"));
    }
}
