package com.my.proj.resource;

import java.net.URI;
import java.util.List;

import com.my.proj.dto.RegisterUserRequest;
import com.my.proj.dto.ResetPasswordRequest;
import com.my.proj.dto.UserResponse;
import com.my.proj.entity.User;
import com.my.proj.service.UserService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @GET
    public List<UserResponse> getUsers() {
        return userService.getAllUsers().stream().map(UserResponse::from).toList();
    }

    @GET
    @Path("/{id}")
    public UserResponse getUser(@PathParam("id") Long id) {
        return UserResponse.from(userService.getUserById(id));
    }

    @POST
    public Response createUser(@Valid RegisterUserRequest request) {
        User created = userService.createUser(request.name(), request.email(), request.password());
        return Response.created(URI.create("/users/" + created.id)).entity(UserResponse.from(created)).build();
    }

    /** 204 on success; 400 for invalid input or an unchanged password; 401 "Invalid email or password" otherwise. */
    @POST
    @Path("/reset-password")
    public Response resetPassword(@NotNull(message = "request body is required") @Valid ResetPasswordRequest request) {
        userService.resetPassword(request.email(), request.currentPassword(), request.newPassword());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/email/{email}")
    public Response deleteUserByEmail(@PathParam("email") String email) {
        userService.deleteUserByEmail(email);
        return Response.noContent().build();
    }
}
