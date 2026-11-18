package com.my.proj.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.my.proj.entity.User;
import com.my.proj.repository.UserRepository;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/** DEMO-90 business rule: users created before password hashing are rejected gracefully. */
@QuarkusTest
class LoginLegacyUserWithoutHashTest {

    @Inject
    UserRepository userRepository;

    @Transactional
    String createLegacyUser() {
        User user = new User();
        user.setName("Legacy");
        user.setEmail("legacy-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash(null);
        userRepository.persist(user);
        return user.getEmail();
    }

    @Test
    void userWithoutPasswordHashGetsTheGenericFailure() {
        String email = createLegacyUser();

        given().contentType(ContentType.JSON)
            .body(Map.of("email", email, "password", "anything"))
            .when().post("/users/login")
            .then().statusCode(401)
            .body("message", equalTo("Invalid credentials"));
    }
}
