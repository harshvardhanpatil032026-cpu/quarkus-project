package com.my.proj.resource;

import java.util.List;

import com.my.proj.dto.RoleResponse;
import com.my.proj.service.AccessService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
public class RoleResource {

    private final AccessService accessService;

    public RoleResource(AccessService accessService) {
        this.accessService = accessService;
    }

    @GET
    @RolesAllowed("role:read")
    public List<RoleResponse> list() {
        return accessService.listRoles();
    }
}
