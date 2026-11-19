package com.my.proj.dto;

import java.util.List;

/** The current user with their roles and permissions. Informational only: the server enforces access itself. */
public record MeResponse(Long id, String name, String email, List<String> roles, List<String> permissions) {
}
