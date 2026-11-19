package com.my.proj.resource;

import java.net.URI;
import java.util.List;

import com.my.proj.dto.FirmRequest;
import com.my.proj.dto.FirmResponse;
import com.my.proj.entity.Firm;
import com.my.proj.service.FirmService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/firms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FirmResource {

    private final FirmService firmService;

    public FirmResource(FirmService firmService) {
        this.firmService = firmService;
    }

    @GET
    @RolesAllowed("firm:read")
    public List<FirmResponse> list() {
        return firmService.list().stream().map(FirmResponse::from).toList();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("firm:read")
    public FirmResponse get(@PathParam("id") Long id) {
        return FirmResponse.from(firmService.get(id));
    }

    /** 201 with Location; 400 invalid body; 409 code already used. */
    @POST
    @RolesAllowed("firm:create")
    public Response create(@NotNull(message = "request body is required") @Valid FirmRequest request) {
        Firm created = firmService.create(request);
        return Response.created(URI.create("/firms/" + created.getId())).entity(FirmResponse.from(created)).build();
    }

    /** 200 with the updated firm; 404 unknown; 409 code used by another firm. */
    @PUT
    @Path("/{id}")
    @RolesAllowed("firm:update")
    public FirmResponse update(@PathParam("id") Long id,
            @NotNull(message = "request body is required") @Valid FirmRequest request) {
        return FirmResponse.from(firmService.update(id, request));
    }

    /** 204; the firm is set INACTIVE, never hard-deleted. */
    @DELETE
    @Path("/{id}")
    @RolesAllowed("firm:delete")
    public Response delete(@PathParam("id") Long id) {
        firmService.deactivate(id);
        return Response.noContent().build();
    }
}
