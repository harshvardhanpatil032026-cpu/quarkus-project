package com.my.proj.dto;

import com.my.proj.entity.User;

/** Public view of a user. Deliberately has no password or password-hash field. */
public record UserResponse(Long id, String name, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id, user.name, user.email);
    }
}
