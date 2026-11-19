package com.my.proj.resource;

import com.my.proj.config.JwtConfig;
import com.my.proj.entity.User;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance criterion: DELETE /firms/{id} by a user with firm:delete returns 204, and the firm is still returned by GET /firms/{id} with status INACTIVE.
 *
 * Assumed contract (DEMO-129 subtask plan; adjust to the code as built):
 * - Tables modules(code), permissions(code), roles(code, scope, system), role_permissions(role_id, permission_id),
 *   user_roles(user_id, role_id, assigned_at) and firms(code, status).
 * - A token's subject is the user id; permissions come from user_roles -> role_permissions, not the token's groups.
 * - JSON uses camelCase (contactEmail, createdAt, updatedAt); GET /me and GET /roles list codes under "roles"/"permissions".
 * - 401 and 403 bodies are ErrorResponse {"message"}.
 */
@QuarkusTest
public class Criterion10_DeleteFirmSetsInactiveTest {

    static final String ADMIN = "PLATFORM_ADMIN";
    static final List<String> ALL_PERMISSIONS = List.of(
        "firm:create", "firm:delete", "firm:read", "firm:update",
        "role:assign", "role:read",
        "user:create", "user:delete", "user:read", "user:update");

    @Inject
    EntityManager em;

    @Inject
    JwtConfig jwtConfig;

    @Inject
    Event<StartupEvent> startup;

    /** Creates a user directly (committed, so HTTP requests see it) holding the given role codes; returns its id. */
    long createUser(String name, String... roleCodes) {
        return QuarkusTransaction.requiringNew().call(() -> {
            User user = new User();
            user.setName(name);
            user.setEmail(("u-" + UUID.randomUUID() + "@example.com").toLowerCase());
            user.setPasswordHash("not-a-real-hash");
            user.persist();
            for (String role : roleCodes) {
                int linked = em.createNativeQuery(
                        "insert into user_roles (user_id, role_id, assigned_at) "
                            + "select :u, r.id, now() from roles r where r.code = :c")
                    .setParameter("u", user.id).setParameter("c", role).executeUpdate();
                assertEquals(1, linked, "role " + role + " exists and was assigned");
            }
            return user.id;
        });
    }

    long createAdmin() {
        return createUser("Platform Admin", ADMIN);
    }

    /** A user who is logged in but holds no role, and so no permission. */
    long createUserWithoutRoles() {
        return createUser("No Roles");
    }

    String emailOf(long userId) {
        return (String) em.createNativeQuery("select email from users where id = :id")
            .setParameter("id", userId).getSingleResult();
    }

    /** Signs a token the way TokenService does: issuer from JwtConfig, subject = user id, groups "user". */
    String tokenFor(long userId) {
        return Jwt.issuer(jwtConfig.issuer()).subject(String.valueOf(userId))
            .groups(Set.of("user")).expiresIn(Duration.ofMinutes(5)).sign();
    }

    String expiredTokenFor(long userId) {
        Instant now = Instant.now();
        return Jwt.issuer(jwtConfig.issuer()).subject(String.valueOf(userId)).groups(Set.of("user"))
            .issuedAt(now.minus(Duration.ofHours(2))).expiresAt(now.minus(Duration.ofHours(1))).sign();
    }

    RequestSpecification as(String token) {
        return given().header("Authorization", "Bearer " + token);
    }

    RequestSpecification asUser(long userId) {
        return as(tokenFor(userId));
    }

    long count(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    List<String> codes(String sql) {
        return (List<String>) em.createNativeQuery(sql).getResultList();
    }

    /** Re-runs every startup observer, as a restart would, so idempotence can be checked in one test run. */
    void simulateRestart() {
        startup.fire(new StartupEvent());
    }

    Map<String, Object> firmBody(String code) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Firm " + code);
        body.put("code", code);
        body.put("contactEmail", "contact@" + code.toLowerCase() + ".example.com");
        body.put("phone", "+1 555 0100");
        body.put("address", "1 Main Street");
        return body;
    }

    String uniqueCode() {
        return "F" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    /** POST /firms as the given user; returns the created firm's id. */
    long createFirm(long userId, String code) {
        Number id = asUser(userId).contentType(ContentType.JSON).body(firmBody(code))
            .when().post("/firms")
            .then().statusCode(201).extract().path("id");
        return id.longValue();
    }

    @Test
    void deleteReturns204AndLeavesTheFirmInactive() {
        long admin = createAdmin();
        String code = uniqueCode();
        long id = createFirm(admin, code);

        asUser(admin).when().delete("/firms/{id}", id).then().statusCode(204);

        asUser(admin).when().get("/firms/{id}", id).then().statusCode(200).body("status", equalTo("INACTIVE"));
        assertEquals(1, count("select count(*) from firms where code = '" + code + "'"), "the row is kept");
    }

    @Test
    void deletingAnAlreadyInactiveFirmIsStill204() {
        long admin = createAdmin();
        long id = createFirm(admin, uniqueCode());
        asUser(admin).when().delete("/firms/{id}", id).then().statusCode(204);

        asUser(admin).when().delete("/firms/{id}", id).then().statusCode(204);

        asUser(admin).when().get("/firms/{id}", id).then().statusCode(200).body("status", equalTo("INACTIVE"));
    }
}
