package com.my.proj.resource;

import java.net.URI;
import java.util.List;

import com.my.proj.entity.User;
import com.my.proj.service.UserService;

import jakarta.inject.Inject;
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
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    public User getUser(@PathParam("id") Long id) {
        return userService.getUserById(id);
    }

    @POST
    public Response createUser(User user) {
        User created = userService.createUser(user);
        return Response.created(URI.create("/users/" + created.id)).entity(created).build();
    }

    @DELETE
    @Path("/email/{email}")
    public Response deleteUserByEmail(@PathParam("email") String email) {
        userService.deleteUserByEmail(email);
        return Response.noContent().build();
    }
}
