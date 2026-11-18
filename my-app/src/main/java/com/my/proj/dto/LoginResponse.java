package com.my.proj.dto;

/** Successful login result. Carries only the token, never any password material. */
public record LoginResponse(String token, String tokenType, long expiresIn) {

    public static LoginResponse bearer(String token, long expiresInSeconds) {
        return new LoginResponse(token, "Bearer", expiresInSeconds);
    }
}
