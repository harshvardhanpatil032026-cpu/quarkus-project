package com.my.proj.resource;

import static com.my.proj.resource.TestAuth.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import com.my.proj.entity.User;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

/** DEMO-131: deleting a user removes their role grants and keeps grants they made to others. */
@QuarkusTest
class DeleteGrantingUserTest {

    @Inject
    EntityManager em;

    private long user(String email) {
        return QuarkusTransaction.requiringNew().call(() -> {
            User u = new User();
            u.setName("Granter Test");
            u.setEmail(email);
            u.setPasswordHash("not-a-real-hash");
            u.persist();
            return u.id;
        });
    }

    @Test
    void deletingTheGranterKeepsTheGrantWithoutAnAssigner() {
        String granterEmail = "granter-" + UUID.randomUUID() + "@example.com";
        long granter = user(granterEmail);
        long grantee = user("grantee-" + UUID.randomUUID() + "@example.com");
        QuarkusTransaction.requiringNew().run(() -> em.createNativeQuery(
                "insert into user_roles (user_id, role_id, assigned_by, assigned_at) "
                        + "select :g, r.id, :by, now() from roles r where r.code = 'PLATFORM_ADMIN'")
                .setParameter("g", grantee).setParameter("by", granter).executeUpdate());

        given().when().delete("/users/email/{email}", granterEmail).then().statusCode(204);

        Object[] row = (Object[]) em.createNativeQuery(
                "select count(*), count(assigned_by) from user_roles where user_id = :g")
                .setParameter("g", grantee).getSingleResult();
        assertEquals(1L, ((Number) row[0]).longValue());
        assertEquals(0L, ((Number) row[1]).longValue());
    }
}
