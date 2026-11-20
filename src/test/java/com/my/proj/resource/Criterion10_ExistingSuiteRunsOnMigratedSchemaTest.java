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

/**
 * Acceptance criterion: the full existing test suite passes after the change.
 *
 * A test cannot run the suite it belongs to; that is what `mvn test` being green proves. This one guards the
 * preconditions that make it meaningful: the suite runs on the Flyway-built schema with the seeder's data present,
 * and no test reaches around Flyway to rebuild the schema.
 */
@QuarkusTest
public class Criterion10_ExistingSuiteRunsOnMigratedSchemaTest {

    @Inject
    EntityManager em;

    long count(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }

    @Test
    void theSuiteRunsOnTheFlywayBuiltSchema() {
        assertEquals(2, count("select count(*) from flyway_schema_history where success and version in ('1', '2')"));
    }

    @Test
    void theSeederRanAfterMigration() {
        assertEquals(1, count("select count(*) from roles where code = 'PLATFORM_ADMIN'"), "PLATFORM_ADMIN is seeded");
        assertTrue(count("select count(*) from permissions") > 0, "permissions are seeded");
    }

    @Test
    void noExistingTestRebuildsTheSchemaItself() throws IOException {
        try (java.util.stream.Stream<Path> files = Files.walk(Path.of("src/test"))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java") || f.toString().endsWith(".properties")).toList()) {
                if (file.getFileName().toString().startsWith("Criterion10_")) {
                    continue;
                }
                String text = Files.readString(file).toLowerCase();
                assertFalse(text.contains("drop-and-create"), file + " must not select drop-and-create");
                assertFalse(text.contains("drop schema public"), file + " must not rebuild the schema by hand");
            }
        }
    }
}
