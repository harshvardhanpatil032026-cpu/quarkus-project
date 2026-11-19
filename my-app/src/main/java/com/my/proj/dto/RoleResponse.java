package com.my.proj.dto;

import java.util.List;

import com.my.proj.entity.Permission;
import com.my.proj.entity.Role;

public record RoleResponse(Long id, String code, String name, String scope, boolean system, List<String> permissions) {

    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getScope().name(), role.isSystem(),
                role.getPermissions().stream().map(Permission::getCode).sorted().toList());
    }
}
