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
public class Criterion3_HibernateValidatesMigratedSchemaTest {

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String url;

    @ConfigProperty(name = "quarkus.datasource.username", defaultValue = "")
    String user;

    @ConfigProperty(name = "quarkus.datasource.password", defaultValue = "")
    String password;

    @Inject
    EntityManagerFactory emf;

    /** A throw-away schema in the test database, so migrations can be run from empty without touching the app's own. */
    String newSchema() throws SQLException {
        String name = "mig_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        try (Connection c = open(); Statement s = c.createStatement()) {
            s.execute("create schema " + name);
        }
        return name;
    }

    void dropSchema(String schema) throws SQLException {
        try (Connection c = open(); Statement s = c.createStatement()) {
            s.execute("drop schema if exists " + schema + " cascade");
        }
    }

    Connection open() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    Connection open(String schema) throws SQLException {
        Connection c = open();
        c.setSchema(schema);
        return c;
    }

    /** Flyway pointed at the schema, reading the application's own db/migration folder from the classpath. */
    FluentConfiguration flywayFor(String schema) {
        return Flyway.configure().dataSource(url, user, password)
            .schemas(schema).defaultSchema(schema).locations("classpath:db/migration");
    }

    /** Every row of the query as "a|b|c"; "?" (if present) is bound to the schema name. */
    List<String> rows(String schema, String sql) throws SQLException {
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (sql.contains("?")) {
                ps.setString(1, schema);
            }
            List<String> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= n; i++) {
                        row.append(i > 1 ? "|" : "").append(rs.getString(i));
                    }
                    out.add(row.toString());
                }
            }
            return out;
        }
    }

    long count(String schema, String table) throws SQLException {
        try (Connection c = open(schema); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("select count(*) from " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    String resource(String name) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
            assertNotNull(in, name + " is on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** The superseded hand-applied script, when the repository still has it (it may be removed or marked superseded). */
    Optional<Path> oldAccessControlScript() {
        return List.of(Path.of("db/DEMO-129-access-control.sql"), Path.of("../db/DEMO-129-access-control.sql"))
            .stream().filter(Files::exists).findFirst();
    }

    /**
     * What Hibernate would validate: every table and column the entities map, minus whatever the schema lacks.
     * An empty list means validation would pass; anything else is the disagreement it would fail on.
     */
    List<String> mismatches(String schema) throws SQLException {
        Map<String, Set<String>> expected = new TreeMap<>();
        SessionFactoryImplementor sf = emf.unwrap(SessionFactoryImplementor.class);
        sf.getMappingMetamodel().streamEntityDescriptors().forEach(entity -> {
            java.util.function.BiConsumer<Integer, org.hibernate.metamodel.mapping.SelectableMapping> add = (i, sel) -> {
                if (sel.isFormula()) {
                    return;
                }
                String table = sel.getContainingTableExpression().replace("\"", "");
                table = table.substring(table.lastIndexOf('.') + 1).toLowerCase();
                expected.computeIfAbsent(table, k -> new TreeSet<>())
                    .add(sel.getSelectionExpression().replace("\"", "").toLowerCase());
            };
            entity.getIdentifierMapping().forEachSelectable(add::accept);
            entity.forEachAttributeMapping(attr -> attr.forEachSelectable(add::accept));
        });
        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, Set<String>> table : expected.entrySet()) {
            Set<String> actual = new TreeSet<>(rows(schema,
                "select lower(column_name) from information_schema.columns where table_schema = ? and table_name = '"
                    + table.getKey() + "'"));
            if (actual.isEmpty()) {
                problems.add("missing table " + table.getKey());
                continue;
            }
            for (String column : table.getValue()) {
                if (!actual.contains(column)) {
                    problems.add("missing column " + table.getKey() + "." + column);
                }
            }
        }
        return problems;
    }

    /** The shape of a schema, with its own name taken out so two schemas can be compared. */
    List<String> shapeOf(String schema) throws SQLException {
        List<String> shape = new ArrayList<>();
        for (String r : rows(schema,
            "select table_name, column_name, data_type, is_nullable, coalesce(character_maximum_length::text, '') "
                + "from information_schema.columns where table_schema = ? and table_name <> 'flyway_schema_history' "
                + "order by table_name, column_name")) {
            shape.add("column " + r);
        }
        for (String r : rows(schema,
            "select c.relname, con.conname, pg_get_constraintdef(con.oid) from pg_constraint con "
                + "join pg_class c on c.oid = con.conrelid join pg_namespace n on n.oid = c.relnamespace "
                + "where n.nspname = ? and c.relname <> 'flyway_schema_history' order by 1, 2, 3")) {
            shape.add("constraint " + r.replace(schema + ".", ""));
        }
        return shape;
    }

    @Test
    void everyEntityTableAndColumnExistsAfterMigration() throws Exception {
        String schema = newSchema();
        try {
            flywayFor(schema).load().migrate();
            assertEquals(List.of(), mismatches(schema), "Hibernate validation finds no schema errors");
        } finally {
            dropSchema(schema);
        }
    }

    @Test
    void theApplicationsOwnMigratedSchemaValidatesToo() throws Exception {
        assertEquals(List.of(), mismatches("public"));
    }
}
