package com.my.proj.dto;

/**
 * A password change for a known user. Every rule is checked in UserService.changePassword, so a
 * refusal lists all the broken rules together, including those that need the user's details.
 */
public record ChangePasswordRequest(String currentPassword, String newPassword) {

    // Never expose either password, even if the request object is logged.
    @Override
    public String toString() {
        return "ChangePasswordRequest[currentPassword=***, newPassword=***]";
    }
}
