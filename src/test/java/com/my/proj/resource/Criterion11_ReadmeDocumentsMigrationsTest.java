package com.my.proj.resource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Acceptance criterion: my-app/README.md explains adding a migration, baselining existing databases, and not editing applied ones. */
public class Criterion11_ReadmeDocumentsMigrationsTest {

    String readme() throws IOException {
        return Files.readString(Path.of("README.md"));
    }

    @Test
    void explainsHowToAddANewMigration() throws IOException {
        String text = readme();
        assertTrue(text.toLowerCase().contains("flyway"), "mentions Flyway");
        assertTrue(text.contains("db/migration"), "names the migration folder");
        assertTrue(text.matches("(?s).*V\\d*[A-Za-z]*__\\w+\\.sql.*"), "shows the Vn__name.sql naming");
    }

    @Test
    void explainsHowExistingDatabasesAreBaselined() throws IOException {
        String text = readme().toLowerCase();
        assertTrue(text.contains("baseline"), "mentions baselining");
        assertTrue(text.matches("(?s).*existing database.*") || text.matches("(?s).*existing databases.*"),
            "says what happens to an existing database");
    }

    @Test
    void warnsAgainstEditingAppliedMigrations() throws IOException {
        assertTrue(readme().toLowerCase().matches("(?s).*(never|do not|don't|must not).{0,60}(edit|modify|change).*"),
            "warns against editing a migration that has been applied");
    }
}
