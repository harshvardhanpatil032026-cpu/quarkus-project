package com.my.proj.resource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/db")
public class DatabaseResource {
    private final DataSource dataSource;

    public DatabaseResource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GET
    public String testConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return "Connected to: " + connection.getCatalog();
        }
    }
}
