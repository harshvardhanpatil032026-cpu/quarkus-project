package com.my.proj.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Body of POST and PUT /firms. Only name and code are required; status is used by PUT only
 * (ACTIVE or INACTIVE; omitted keeps the current status).
 */
public record FirmRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "code is required") String code,
        @Email(message = "contactEmail must be a valid email address") String contactEmail,
        String phone,
        String address,
        @Pattern(regexp = "ACTIVE|INACTIVE", message = "status must be ACTIVE or INACTIVE") String status) {
}
