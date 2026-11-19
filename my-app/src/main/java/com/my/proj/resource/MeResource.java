package com.my.proj.resource;

import com.my.proj.dto.MeResponse;
import com.my.proj.security.PermissionAugmentor;
import com.my.proj.service.AccessService;

import io.quarkus.security.Authenticated;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/me")
@Produces(MediaType.APPLICATION_JSON)
public class MeResource {

    private final AccessService accessService;
    private final SecurityIdentity identity;

    public MeResource(AccessService accessService, SecurityIdentity identity) {
        this.accessService = accessService;
        this.identity = identity;
    }

    @GET
    @Authenticated
    public MeResponse me() {
        MeResponse me = accessService.me(identity.getAttribute(PermissionAugmentor.USER_ID));
        if (me == null) {
            throw new AuthenticationFailedException();
        }
        return me;
    }
}
