package com.my.proj.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.my.proj.entity.User;
import com.my.proj.repository.UserRepository;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * DEMO-99 criterion 5: a user with no stored password hash (created before password hashing)
 * gets the same 401 as a wrong password or an unknown email, and the reset changes nothing.
 */
@QuarkusTest
class ResetPasswordLegacyUserWithoutHashTest {

    @Inject
    UserRepository userRepository;

    @Transactional
    String createLegacyUser() {
        User user = new User();
        user.setName("Legacy");
        user.setEmail("legacy-reset-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash(null);
        userRepository.persist(user);
        return user.getEmail();
    }

    @Transactional
    String storedHash(String email) {
        return userRepository.find("email", email).firstResult().getPasswordHash();
    }

    @Test
    void userWithoutPasswordHashGetsTheGenericFailure() {
        String email = createLegacyUser();

        given().contentType(ContentType.JSON)
            .body(Map.of("email", email, "currentPassword", "AnyOldPass1", "newPassword", "BrandNewPass2"))
            .when().post("/users/reset-password")
            .then().statusCode(401)
            .body("message", equalTo("Invalid email or password"));

        assertNull(storedHash(email), "a rejected reset must not set a password hash");
    }
}
