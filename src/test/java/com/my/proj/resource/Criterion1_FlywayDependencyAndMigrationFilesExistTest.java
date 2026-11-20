package com.my.proj.resource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Acceptance criterion: quarkus-flyway is declared in pom.xml and the two migration files are on the classpath. */
public class Criterion1_FlywayDependencyAndMigrationFilesExistTest {

    @Test
    void pomDeclaresQuarkusFlyway() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"));
        assertTrue(pom.contains("<artifactId>quarkus-flyway</artifactId>"), "pom.xml declares quarkus-flyway");
    }

    @Test
    void migrationFilesLiveUnderDbMigration() {
        for (String file : new String[] {"V1__baseline.sql", "V2__access_control.sql"}) {
            assertTrue(Files.exists(Path.of("src/main/resources/db/migration", file)),
                file + " is in src/main/resources/db/migration");
            assertNotNull(Thread.currentThread().getContextClassLoader().getResource("db/migration/" + file),
                file + " is packaged on the classpath");
        }
    }
}
