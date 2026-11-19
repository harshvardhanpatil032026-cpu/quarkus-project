package com.my.proj.resource;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import com.my.proj.config.JwtConfig;
import com.my.proj.entity.User;

import io.quarkus.arc.Arc;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import io.smallrye.jwt.build.Jwt;
import jakarta.persistence.EntityManager;

/**
 * DEMO-129: the user endpoints now need a token. {@link #given()} is RestAssured's {@code given()}
 * with the bearer token of a platform admin, so tests that exercise /users behaviour (not access
 * control) keep doing so. Tests of access control itself use plain {@code given()}.
 */
final class TestAuth {

    private static volatile Long adminId;

    private TestAuth() {
    }

    static RequestSpecification given() {
        JwtConfig config = Arc.container().instance(JwtConfig.class).get();
        String token = Jwt.issuer(config.issuer()).subject(String.valueOf(adminId()))
                .groups(Set.of("user")).expiresIn(Duration.ofMinutes(5)).sign();
        return RestAssured.given().header("Authorization", "Bearer " + token);
    }

    private static synchronized long adminId() {
        if (adminId == null || !exists(adminId)) {
            adminId = QuarkusTransaction.requiringNew().call(() -> {
                EntityManager em = Arc.container().instance(EntityManager.class).get();
                User user = new User();
                user.setName("Test Platform Admin");
                user.setEmail("test-admin-" + UUID.randomUUID() + "@example.com");
                user.setPasswordHash("not-a-real-hash");
                em.persist(user);
                em.flush();
                em.createNativeQuery("insert into user_roles (user_id, role_id, assigned_at) "
                        + "select :u, r.id, now() from roles r where r.code = 'PLATFORM_ADMIN'")
                        .setParameter("u", user.id).executeUpdate();
                return user.id;
            });
        }
        return adminId;
    }

    private static boolean exists(long id) {
        return QuarkusTransaction.requiringNew().call(() -> {
            EntityManager em = Arc.container().instance(EntityManager.class).get();
            return ((Number) em.createNativeQuery("select count(*) from users where id = :id")
                    .setParameter("id", id).getSingleResult()).longValue() > 0;
        });
    }
}
