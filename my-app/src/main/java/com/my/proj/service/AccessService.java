package com.my.proj.service;

import java.util.List;

import com.my.proj.dto.MeResponse;
import com.my.proj.dto.RoleResponse;
import com.my.proj.entity.User;
import com.my.proj.repository.RoleRepository;
import com.my.proj.repository.UserRepository;
import com.my.proj.repository.UserRoleRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.transaction.Transactional;

/** Reads roles and permissions; the single source of what a user may do. */
@ApplicationScoped
public class AccessService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public AccessService(UserRepository userRepository, UserRoleRepository userRoleRepository,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Permission codes held by the user, or null when the user no longer exists (so a token for a
     * deleted user is refused). Safe to call outside a request context.
     */
    @ActivateRequestContext
    @Transactional
    public List<String> permissionsOrNull(Long userId) {
        if (userRepository.findById(userId) == null) {
            return null;
        }
        return userRoleRepository.permissionCodes(userId);
    }

    @Transactional
    public MeResponse me(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return null;
        }
        return new MeResponse(user.id, user.getName(), user.getEmail(),
                userRoleRepository.roleCodes(userId), userRoleRepository.permissionCodes(userId));
    }

    @Transactional
    public List<RoleResponse> listRoles() {
        return roleRepository.listAllWithPermissions().stream().map(RoleResponse::from).toList();
    }
}
