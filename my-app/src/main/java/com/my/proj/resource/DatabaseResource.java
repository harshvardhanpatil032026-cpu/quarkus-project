package com.my.proj.resource;

import com.my.proj.service.DatabaseService;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/db")
public class DatabaseResource {

    private final DatabaseService databaseService;

    public DatabaseResource(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GET
    @PermitAll
    public String testConnection() {
        return "Connected to: " + databaseService.catalogName();
    }
}
