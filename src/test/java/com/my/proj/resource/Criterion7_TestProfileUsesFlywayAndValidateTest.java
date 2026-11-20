package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.output.MigrateResult;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class Criterion7_TestProfileUsesFlywayAndValidateTest {

    @Inject
    EntityManager em;

    String config(String key) {
        return org.eclipse.microprofile.config.ConfigProvider.getConfig().getOptionalValue(key, String.class).orElse("");
    }

    @Test
    void testProfileValidatesAndNeverDropsAndCreates() {
        assertEquals("validate", config("quarkus.hibernate-orm.database.generation").toLowerCase(),
            "Hibernate validation is enabled in the test profile");
    }

    @Test
    void testProfileMigratesWithFlywayAtStart() {
        assertEquals("true", config("quarkus.flyway.migrate-at-start").toLowerCase());
    }

    @Test
    void theTestSchemaWasBuiltByFlywayMigrations() {
        Number applied = (Number) em.createNativeQuery(
            "select count(*) from flyway_schema_history where success and version in ('1', '2')").getSingleResult();
        assertEquals(2, applied.intValue(), "V1 and V2 are in flyway_schema_history of the test database");
    }

    @Test
    void noTestResourceSelectsDropAndCreate() throws IOException {
        Path testProps = Path.of("src/test/resources/application.properties");
        if (Files.exists(testProps)) {
            assertFalse(Files.readString(testProps).toLowerCase().contains("drop-and-create"));
        }
    }
}
