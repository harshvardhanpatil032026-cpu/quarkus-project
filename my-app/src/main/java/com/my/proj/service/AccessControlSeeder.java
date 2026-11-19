package com.my.proj.service;

import java.util.List;
import java.util.Locale;

import org.jboss.logging.Logger;

import com.my.proj.config.BootstrapConfig;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Idempotent startup seeding of modules, permissions, the PLATFORM_ADMIN role and its permission
 * links, plus the optional first-admin grant. Every insert is {@code ON CONFLICT DO NOTHING}
 * against a unique constraint, so running it repeatedly, or from two instances at once, never
 * creates a duplicate.
 */
@ApplicationScoped
public class AccessControlSeeder {

    private static final Logger LOG = Logger.getLogger(AccessControlSeeder.class);

    public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";

    private record ModuleSeed(String code, String name, String description) {
    }

    private record PermissionSeed(String module, String action, String description) {
        String code() {
            return module.toLowerCase(Locale.ROOT) + ":" + action;
        }
    }

    private static final List<ModuleSeed> MODULES = List.of(
            new ModuleSeed("FIRM", "Firms", "Firm management"),
            new ModuleSeed("USER", "Users", "User management"),
            new ModuleSeed("ROLE", "Roles", "Role management"));

    private static final List<PermissionSeed> PERMISSIONS = List.of(
            new PermissionSeed("FIRM", "create", "Create firms"),
            new PermissionSeed("FIRM", "read", "View firms"),
            new PermissionSeed("FIRM", "update", "Update firms"),
            new PermissionSeed("FIRM", "delete", "Deactivate firms"),
            new PermissionSeed("USER", "create", "Create users"),
            new PermissionSeed("USER", "read", "View users"),
            new PermissionSeed("USER", "update", "Update users"),
            new PermissionSeed("USER", "delete", "Delete users"),
            new PermissionSeed("ROLE", "read", "View roles"),
            new PermissionSeed("ROLE", "assign", "Assign roles to users"));

    private final EntityManager em;
    private final BootstrapConfig bootstrap;

    public AccessControlSeeder(EntityManager em, BootstrapConfig bootstrap) {
        this.em = em;
        this.bootstrap = bootstrap;
    }

    void onStart(@Observes StartupEvent event) {
        seed();
    }

    @Transactional
    public void seed() {
        for (ModuleSeed m : MODULES) {
            em.createNativeQuery("insert into modules (code, name, description, active) "
                    + "values (:code, :name, :description, true) on conflict (code) do nothing")
                    .setParameter("code", m.code()).setParameter("name", m.name())
                    .setParameter("description", m.description()).executeUpdate();
        }
        for (PermissionSeed p : PERMISSIONS) {
            em.createNativeQuery("insert into permissions (module_id, action, code, description) "
                    + "select m.id, :action, :code, :description from modules m where m.code = :module "
                    + "on conflict (code) do nothing")
                    .setParameter("module", p.module()).setParameter("action", p.action())
                    .setParameter("code", p.code()).setParameter("description", p.description())
                    .executeUpdate();
        }
        em.createNativeQuery("insert into roles (code, name, scope, system) "
                + "values (:code, 'Platform administrator', 'PLATFORM', true) on conflict (code) do nothing")
                .setParameter("code", PLATFORM_ADMIN).executeUpdate();
        em.createNativeQuery("insert into role_permissions (role_id, permission_id) "
                + "select r.id, p.id from roles r cross join permissions p "
                + "where r.code = :role and p.code in (:codes) on conflict do nothing")
                .setParameter("role", PLATFORM_ADMIN)
                .setParameter("codes", PERMISSIONS.stream().map(PermissionSeed::code).toList())
                .executeUpdate();
        grantFirstAdmin();
    }

    private void grantFirstAdmin() {
        String email = bootstrap.platformAdminEmail().map(e -> e.trim().toLowerCase(Locale.ROOT)).orElse("");
        if (email.isEmpty()) {
            return;
        }
        int granted = em.createNativeQuery("insert into user_roles (user_id, role_id, assigned_at) "
                + "select u.id, r.id, now() from users u cross join roles r "
                + "where u.email = :email and r.code = :role on conflict (user_id, role_id) do nothing")
                .setParameter("email", email).setParameter("role", PLATFORM_ADMIN).executeUpdate();
        if (granted > 0) {
            LOG.info("Granted PLATFORM_ADMIN to the configured first platform admin");
        }
    }
}
