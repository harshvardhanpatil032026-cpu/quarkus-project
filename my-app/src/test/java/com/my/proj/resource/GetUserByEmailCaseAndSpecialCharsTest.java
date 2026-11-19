package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.my.proj.resource.TestAuth.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * DEMO-112, criterion 3 (tests/api/DEMO-112/test-3.txt): lookup ignores case and handles special
 * characters such as '+', and trims surrounding whitespace. Emails are stored lower-cased at registration, so that is what comes back.
 */
@QuarkusTest
class GetUserByEmailCaseAndSpecialCharsTest {

    @Test
    void lookupIgnoresCaseAndKeepsSpecialCharacters() {
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Jane Doe", "email", "jane.doe+test@EXAMPLE.com", "password", "S3cret-Passw0rd!"))
            .when().post("/users")
            .then().statusCode(201);

        given()
            .when().get("/users/email/JANE.DOE+TEST@example.com")
            .then()
            .statusCode(200)
            .body("name", equalTo("Jane Doe"))
            .body("email", equalTo("jane.doe+test@example.com"));
    }

    @Test
    void surroundingWhitespaceIsTrimmed() {
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Tom Trim", "email", "tom.trim@example.com", "password", "S3cret-Passw0rd!"))
            .when().post("/users")
            .then().statusCode(201);

        given()
            .urlEncodingEnabled(false)
            .when().get("/users/email/%20%20Tom.Trim@Example.com%20")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("name", equalTo("Tom Trim"))
            .body("email", equalTo("tom.trim@example.com"));
    }

    @Test
    void percentEncodedSpecialCharactersAreDecoded() {
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Sam O'Neil", "email", "sam.o'neil_99+x@example.com", "password", "S3cret-Passw0rd!"))
            .when().post("/users")
            .then().statusCode(201);

        given()
            .urlEncodingEnabled(false)
            .when().get("/users/email/Sam.O%27Neil_99%2BX%40Example.com")
            .then()
            .statusCode(200)
            .body("name", equalTo("Sam O'Neil"))
            .body("email", equalTo("sam.o'neil_99+x@example.com"));
    }
}
