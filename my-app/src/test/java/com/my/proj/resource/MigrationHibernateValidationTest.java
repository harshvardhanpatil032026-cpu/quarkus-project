package com.my.proj.resource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.SchemaManagementException;
import org.junit.jupiter.api.Test;

import com.my.proj.entity.Firm;
import com.my.proj.entity.Module;
import com.my.proj.entity.Permission;
import com.my.proj.entity.Role;
import com.my.proj.entity.User;
import com.my.proj.entity.UserRole;

import io.quarkus.test.junit.QuarkusTest;

/**
 * DEMO-141 migration test: runs the real migrations on an empty schema and lets Hibernate's own schema
 * validator (the one prod runs with {@code validate}) compare every entity with the migrated schema. Unlike a
 * table and column name comparison it also checks column types and id sequences, and it is proven to fail when the
 * schema and the entities disagree.
 */
@QuarkusTest
public class MigrationHibernateValidationTest {

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String url;

    @ConfigProperty(name = "quarkus.datasource.username", defaultValue = "")
    String user;

    @ConfigProperty(name = "quarkus.datasource.password", defaultValue = "")
    String password;

    private Connection open() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private String newSchema() throws SQLException {
        String schema = "val_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        try (Connection c = open(); Statement s = c.createStatement()) {
            s.execute("create schema " + schema);
        }
        return schema;
    }

    private void dropSchema(String schema) throws SQLException {
        try (Connection c = open(); Statement s = c.createStatement()) {
            s.execute("drop schema if exists " + schema + " cascade");
        }
    }

    private void execute(String schema, String sql) throws SQLException {
        try (Connection c = open(); Statement s = c.createStatement()) {
            s.execute("set search_path to " + schema);
            s.execute(sql);
        }
    }

    private void migrate(String schema) {
        Flyway.configure().dataSource(url, user, password).schemas(schema).defaultSchema(schema)
                .locations("classpath:db/migration").load().migrate();
    }

    /** Hibernate's schema validation of all entities against the given schema; throws when they disagree. */
    private void validate(String schema) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", url)
                .applySetting("hibernate.connection.username", user)
                .applySetting("hibernate.connection.password", password)
                .applySetting("hibernate.default_schema", schema)
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                .build();
        try (SessionFactory sf = new MetadataSources(registry)
                .addAnnotatedClass(User.class).addAnnotatedClass(Firm.class).addAnnotatedClass(Module.class)
                .addAnnotatedClass(Permission.class).addAnnotatedClass(Role.class).addAnnotatedClass(UserRole.class)
                .buildMetadata().buildSessionFactory()) {
            assertEquals(true, sf.isOpen());
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void migrationsOnAnEmptySchemaPassHibernateValidation() throws Exception {
        String schema = newSchema();
        try {
            migrate(schema);
            assertDoesNotThrow(() -> validate(schema));
        } finally {
            dropSchema(schema);
        }
    }

    @Test
    void validationFailsWhenAMigrationIsMissing() throws Exception {
        String schema = newSchema();
        try {
            Flyway.configure().dataSource(url, user, password).schemas(schema).defaultSchema(schema)
                    .locations("classpath:db/migration").target("1").load().migrate();
            assertThrows(SchemaManagementException.class, () -> validate(schema),
                    "with only V1 applied the access-control tables are missing");
        } finally {
            dropSchema(schema);
        }
    }

    /**
     * Panache entities draw ids from {@code users_seq} (allocation size 50). The standalone validator above does not
     * apply Quarkus's sequence naming, so the baseline's sequence is asserted directly.
     */
    @Test
    void baselineCreatesTheUsersIdSequenceWithPanacheAllocationSize() throws Exception {
        String schema = newSchema();
        try {
            migrate(schema);
            try (Connection c = open(); Statement s = c.createStatement();
                    java.sql.ResultSet rs = s.executeQuery("select increment_by from pg_sequences where schemaname = '"
                            + schema + "' and sequencename = 'users_seq'")) {
                assertEquals(true, rs.next(), "users_seq exists after the migrations");
                assertEquals(50L, rs.getLong(1), "users_seq increments by 50 like Panache's id generator");
            }
        } finally {
            dropSchema(schema);
        }
    }

    @Test
    void validationFailsWhenAColumnTypeDrifts() throws Exception {
        String schema = newSchema();
        try {
            migrate(schema);
            execute(schema, "alter table firms alter column created_at type date");
            assertThrows(SchemaManagementException.class, () -> validate(schema),
                    "a column type that no longer matches the entity is a validation error");
        } finally {
            dropSchema(schema);
        }
    }
}
