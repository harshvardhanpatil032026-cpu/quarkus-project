package com.my.proj.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;

/** Checks that the application can reach its database. */
@ApplicationScoped
public class DatabaseService {

    private final DataSource dataSource;

    public DatabaseService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Opens a connection and returns the name of the database it is connected to.
     *
     * @throws IllegalStateException if no connection can be made; ApiExceptionMapper turns it
     *         into a 500 with the generic "Unexpected error" body
     */
    public String catalogName() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getCatalog();
        } catch (SQLException e) {
            throw new IllegalStateException("Database connection check failed", e);
        }
    }
}
