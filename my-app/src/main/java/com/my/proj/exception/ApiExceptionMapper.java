package com.my.proj.exception;

import com.my.proj.dto.ErrorResponse;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException e) {
        if (e instanceof jakarta.ws.rs.WebApplicationException wae) {
            return wae.getResponse();
        }
        Status status = switch (e) {
            case UserNotFoundException x -> Status.NOT_FOUND;
            case EmailAlreadyExistsException x -> Status.CONFLICT;
            case IllegalArgumentException x -> Status.BAD_REQUEST;
            default -> Status.INTERNAL_SERVER_ERROR;
        };
        String message = status == Status.INTERNAL_SERVER_ERROR ? "Unexpected error" : e.getMessage();
        return Response.status(status)
                .entity(new ErrorResponse(message))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
