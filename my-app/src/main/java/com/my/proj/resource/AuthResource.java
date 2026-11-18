package com.my.proj.resource;

import com.my.proj.dto.LoginRequest;
import com.my.proj.dto.LoginResponse;
import com.my.proj.service.AuthService;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/users/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    /** 200 with a JWT on success; 400 for a malformed request; 401 "Invalid credentials" otherwise. */
    @POST
    @PermitAll
    public LoginResponse login(@NotNull(message = "request body is required") @Valid LoginRequest request) {
        return authService.login(request.email(), request.password());
    }
}
