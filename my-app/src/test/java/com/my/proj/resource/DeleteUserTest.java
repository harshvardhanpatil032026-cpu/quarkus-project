package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static com.my.proj.resource.TestAuth.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/** DEMO-127: DELETE /users/email/{email} removes the account, or says clearly that there is none. */
@QuarkusTest
class DeleteUserTest {

    @Inject
    EntityManager em;

    private String register() {
        String email = "delete-" + UUID.randomUUID() + "@example.com";
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Delete Tester", "email", email, "password", "Old#Passw0rd"))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    private long usersWithEmail(String email) {
        return ((Number) em.createNativeQuery("select count(*) from users where email = :e")
            .setParameter("e", email).getSingleResult()).longValue();
    }

    @Test
    void deletedAccountIsRemovedAndOthersAreKept() {
        String gone = register();
        String kept = register();

        given().when().delete("/users/email/{email}", gone).then().statusCode(204);

        assertEquals(0, usersWithEmail(gone));
        assertEquals(1, usersWithEmail(kept));
        given().when().get("/users/email/{email}", gone).then().statusCode(404);
    }

    @Test
    void emailIsMatchedRegardlessOfCase() {
        String email = register();
        given().when().delete("/users/email/{email}", email.toUpperCase()).then().statusCode(204);
        assertEquals(0, usersWithEmail(email));
    }

    @Test
    void unknownUserGives404NamingTheEmail() {
        String nobody = "nobody-" + UUID.randomUUID() + "@example.com";
        given().when().delete("/users/email/{email}", nobody).then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("message", equalTo("User not found with email: " + nobody));
    }

    @Test
    void deletingTwiceGives404TheSecondTime() {
        String email = register();
        given().when().delete("/users/email/{email}", email).then().statusCode(204);
        given().when().delete("/users/email/{email}", email).then()
            .statusCode(404).body("message", containsStringIgnoringCase("not found"));
    }
}
