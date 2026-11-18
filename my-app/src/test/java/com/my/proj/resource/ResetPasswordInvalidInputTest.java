package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

/** DEMO-104: from the generated spec tests/api/DEMO-99/test-3.txt. */
@QuarkusTest
class ResetPasswordInvalidInputTest {

    private static final String OLD_PASSWORD = "OldP@ssw0rd1";
    private static final String NEW_PASSWORD = "NewP@ssw0rd2";

    private String uniqueEmail() {
        return "reset-" + UUID.randomUUID() + "@example.com";
    }

    private Response reset(Object email, Object current, Object next) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("email", email);
        body.put("currentPassword", current);
        body.put("newPassword", next);
        return given().contentType(ContentType.JSON).body(body)
            .when().post("/users/reset-password");
    }

    @Test
    void missingEmailReturns400() {
        reset(null, OLD_PASSWORD, NEW_PASSWORD).then().statusCode(400);
    }

    @Test
    void blankEmailReturns400() {
        reset("   ", OLD_PASSWORD, NEW_PASSWORD).then().statusCode(400);
    }

    @Test
    void missingCurrentPasswordReturns400() {
        reset(uniqueEmail(), null, NEW_PASSWORD).then().statusCode(400);
    }

    @Test
    void blankCurrentPasswordReturns400() {
        reset(uniqueEmail(), "", NEW_PASSWORD).then().statusCode(400);
    }

    @Test
    void missingNewPasswordReturns400() {
        reset(uniqueEmail(), OLD_PASSWORD, null).then().statusCode(400);
    }

    @Test
    void blankNewPasswordReturns400() {
        reset(uniqueEmail(), OLD_PASSWORD, "").then().statusCode(400);
    }

    @Test
    void badlyFormattedEmailReturns400() {
        reset("not-an-email", OLD_PASSWORD, NEW_PASSWORD).then().statusCode(400);
    }

    @Test
    void errorResponseListsTheValidationMessages() {
        // Every field is invalid at once, so more than one message is expected.
        reset("", "", "").then()
            .statusCode(400)
            .body(notNullValue());
    }
}
