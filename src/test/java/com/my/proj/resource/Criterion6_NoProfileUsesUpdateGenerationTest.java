package com.my.proj.resource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Acceptance criterion: database.generation is not update in any profile; prod validates (or none); dev does not auto-alter. */
public class Criterion6_NoProfileUsesUpdateGenerationTest {

    static final Path RESOURCES = Path.of("src/main/resources");

    Properties application() throws IOException {
        Properties p = new Properties();
        try (Reader in = Files.newBufferedReader(RESOURCES.resolve("application.properties"))) {
            p.load(in);
        }
        return p;
    }

    boolean isGenerationKey(String key) {
        String bare = key.replaceFirst("^%[^.]+\\.", "");
        return bare.startsWith("quarkus.hibernate-orm") && bare.endsWith("database.generation");
    }

    @Test
    void noProfileSetsGenerationToUpdate() throws IOException {
        Properties p = application();
        for (String key : p.stringPropertyNames()) {
            if (isGenerationKey(key)) {
                assertNotEquals("update", p.getProperty(key).trim().toLowerCase(), key + " must not be update");
            }
        }
        try (Stream<Path> files = Files.list(RESOURCES)) {
            for (Path yaml : files.filter(f -> f.getFileName().toString().matches("application.*\\.ya?ml")).toList()) {
                assertFalse(Files.readString(yaml).matches("(?s).*generation:\\s*update.*"), yaml + " must not use update");
            }
        }
    }

    @Test
    void prodValidatesOrDoesNothing() throws IOException {
        Properties p = application();
        String prod = p.getProperty("%prod.quarkus.hibernate-orm.database.generation",
            p.getProperty("quarkus.hibernate-orm.database.generation", "none")).trim().toLowerCase();
        assertTrue(List.of("validate", "none").contains(prod), "prod generation is validate or none, was " + prod);
    }

    @Test
    void devDoesNotAutoAlterTheSchema() throws IOException {
        Properties p = application();
        String dev = p.getProperty("%dev.quarkus.hibernate-orm.database.generation",
            p.getProperty("quarkus.hibernate-orm.database.generation", "none")).trim().toLowerCase();
        assertFalse(List.of("update", "create", "drop-and-create", "create-drop").contains(dev),
            "dev must not alter the schema, was " + dev);
    }
}
