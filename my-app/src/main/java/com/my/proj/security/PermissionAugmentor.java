package com.my.proj.security;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import com.my.proj.service.AccessService;

/**
 * After the JWT is verified, replaces the identity's roles with the user's permission codes,
 * read from the database on every request (the token's own groups are ignored). A token whose
 * subject is not an existing user is refused with 401, so permissions cannot outlive the user.
 */
@ApplicationScoped
public class PermissionAugmentor implements SecurityIdentityAugmentor {

    public static final String USER_ID = "userId";

    private final AccessService accessService;

    public PermissionAugmentor(AccessService accessService) {
        this.accessService = accessService;
    }

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (identity.isAnonymous() || !(identity.getPrincipal() instanceof JsonWebToken jwt)) {
            return Uni.createFrom().item(identity);
        }
        Long userId = parse(jwt.getSubject());
        if (userId == null) {
            return Uni.createFrom().failure(new AuthenticationFailedException("Invalid token subject"));
        }
        return context.runBlocking(() -> {
            List<String> permissions = accessService.permissionsOrNull(userId);
            if (permissions == null) {
                throw new AuthenticationFailedException("Unknown user");
            }
            return QuarkusSecurityIdentity.builder().setPrincipal(identity.getPrincipal())
                    .addAttributes(identity.getAttributes()).addCredentials(identity.getCredentials())
                    .addPermissionChecker(identity::checkPermission)
                    .addRoles(new java.util.HashSet<>(permissions)).addAttribute(USER_ID, userId).build();
        });
    }

    private static Long parse(String subject) {
        try {
            return subject == null ? null : Long.valueOf(subject);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
