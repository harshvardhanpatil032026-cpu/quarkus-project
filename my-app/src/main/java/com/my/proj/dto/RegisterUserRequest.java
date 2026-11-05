package com.my.proj.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "email is required") @Email(message = "email must be a valid email address") String email,
        @NotBlank(message = "password is required") String password) {

    // Never expose the plain-text password, even if the request object is logged.
    @Override
    public String toString() {
        return "RegisterUserRequest[name=" + name + ", email=" + email + ", password=***]";
    }
}
