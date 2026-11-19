package com.my.proj.exception;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.my.proj.dto.ErrorResponse;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * JSON ErrorResponse bodies for 401 and 403, like every other error the API returns. These
 * Quarkus security exceptions are mapped here rather than in {@link ApiExceptionMapper} because
 * that mapper's {@code ExceptionMapper<RuntimeException>} does not take them over the framework's
 * own handling; a specific mapper does.
 */
public class SecurityExceptionMappers {

    @ServerExceptionMapper
    public Response unauthorized(UnauthorizedException e) {
        return body(Response.Status.UNAUTHORIZED, "Authentication required");
    }

    @ServerExceptionMapper
    public Response authenticationFailed(AuthenticationFailedException e) {
        return body(Response.Status.UNAUTHORIZED, "Invalid or expired token");
    }

    @ServerExceptionMapper
    public Response forbidden(ForbiddenException e) {
        return body(Response.Status.FORBIDDEN, "You do not have permission to perform this action");
    }

    private static Response body(Response.Status status, String message) {
        return Response.status(status).entity(new ErrorResponse(message)).type(MediaType.APPLICATION_JSON).build();
    }
}
