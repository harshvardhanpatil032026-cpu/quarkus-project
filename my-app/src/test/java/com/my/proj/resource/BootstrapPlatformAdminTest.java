package com.my.proj.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;

import com.my.proj.entity.User;
import com.my.proj.service.AccessControlSeeder;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

/** DEMO-133: the configured email receives PLATFORM_ADMIN at startup, once, and nobody else does. */
@QuarkusTest
@TestProfile(BootstrapPlatformAdminTest.Profile.class)
class BootstrapPlatformAdminTest {

    static final String ADMIN_EMAIL = "bootstrap-admin@example.com";

    public static class Profile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("app.bootstrap.platform-admin-email", ADMIN_EMAIL);
        }
    }

    @Inject
    EntityManager em;

    @Inject
    AccessControlSeeder seeder;

    private long createUser(String email) {
        return QuarkusTransaction.requiringNew().call(() -> {
            User user = new User();
            user.setName("Bootstrap Test");
            user.setEmail(email);
            user.setPasswordHash("not-a-real-hash");
            user.persist();
            return user.id;
        });
    }

    private long admins(long userId) {
        return ((Number) em.createNativeQuery("select count(*) from user_roles ur join roles r on r.id = ur.role_id "
                + "where ur.user_id = :u and r.code = 'PLATFORM_ADMIN'").setParameter("u", userId).getSingleResult())
                .longValue();
    }

    @Test
    void configuredEmailGetsPlatformAdminExactlyOnceAndOthersDoNot() {
        long admin = createUser(ADMIN_EMAIL);
        long other = createUser("other-" + UUID.randomUUID() + "@example.com");

        seeder.seed();
        assertEquals(1, admins(admin));
        assertEquals(0, admins(other));

        seeder.seed();
        assertEquals(1, admins(admin), "a restart adds no duplicate grant");
    }
}
