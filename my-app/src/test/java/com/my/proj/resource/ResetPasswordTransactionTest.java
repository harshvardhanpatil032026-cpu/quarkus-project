package com.my.proj.resource;

import com.my.proj.service.UserService;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Criterion 8: the password update runs in a single transaction. */
/** DEMO-104: from the generated spec tests/api/DEMO-99/test-8.txt. */
@QuarkusTest
public class ResetPasswordTransactionTest {

    private static final String OLD_PASSWORD = "OldP@ssw0rd1";
    private static final String NEW_PASSWORD = "NewP@ssw0rd2";

    @Inject
    EntityManager em;

    private String registerUser() {
        String email = "reset-" + UUID.randomUUID() + "@example.com";
        given().contentType(ContentType.JSON)
            .body(Map.of("name", "Reset Tester", "email", email, "password", OLD_PASSWORD))
            .when().post("/users")
            .then().statusCode(201);
        return email;
    }

    private io.restassured.response.ValidatableResponse reset(Object email, Object current, Object next) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("currentPassword", current);
        body.put("newPassword", next);
        return given().contentType(ContentType.JSON).body(body)
            .when().post("/users/reset-password")
            .then();
    }

    private String storedHash(String email) {
        return (String) em.createNativeQuery("select password_hash from users where email = :e")
            .setParameter("e", email).getSingleResult();
    }

    @Test
    public void resetPasswordIsTransactional() {
        List<Method> methods = Arrays.stream(UserService.class.getDeclaredMethods())
            .filter(m -> m.getName().equals("resetPassword"))
            .toList();
        assertFalse(methods.isEmpty(), "UserService must declare resetPassword(email, currentPassword, newPassword)");

        for (Method method : methods) {
            Transactional tx = method.getAnnotation(Transactional.class);
            if (tx == null) {
                tx = UserService.class.getAnnotation(Transactional.class);
            }
            assertNotNull(tx, "resetPassword must run in a transaction (@Transactional on the method or class)");
            assertTrue(
                tx.value() == Transactional.TxType.REQUIRED || tx.value() == Transactional.TxType.REQUIRES_NEW,
                "resetPassword must start a transaction when none is active, but was " + tx.value());
        }
    }

    @Test
    public void rejectedResetsLeaveTheStoredHashUntouched() {
        String email = registerUser();
        String before = storedHash(email);

        reset(email, "Wrong#Pass9", NEW_PASSWORD).statusCode(401);  // wrong current password
        reset(email, OLD_PASSWORD, OLD_PASSWORD).statusCode(400);   // new same as current
        reset(email, OLD_PASSWORD, "").statusCode(400);             // blank new password

        assertEquals(before, storedHash(email), "a rejected reset must not change the stored hash");
        assertTrue(BcryptUtil.matches(OLD_PASSWORD, storedHash(email)), "the old password must still verify");
    }

    @Test
    public void successfulResetCommitsTheNewHash() {
        String email = registerUser();
        String before = storedHash(email);

        reset(email, OLD_PASSWORD, NEW_PASSWORD).statusCode(204);

        // Read back in a fresh query, after the request's transaction has ended.
        String after = storedHash(email);
        assertFalse(before.equals(after), "the stored hash must change after a successful reset");
        assertTrue(BcryptUtil.matches(NEW_PASSWORD, after), "the committed hash must verify the new password");
    }
}
