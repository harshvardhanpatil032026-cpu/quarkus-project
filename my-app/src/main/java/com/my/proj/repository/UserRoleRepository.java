package com.my.proj.repository;

import java.util.List;

import com.my.proj.entity.UserRole;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRoleRepository implements PanacheRepository<UserRole> {

    public List<String> roleCodes(Long userId) {
        return getEntityManager()
                .createQuery("select ur.role.code from UserRole ur where ur.user.id = :id order by ur.role.code", String.class)
                .setParameter("id", userId).getResultList();
    }

    public List<String> permissionCodes(Long userId) {
        return getEntityManager()
                .createQuery("select distinct p.code from UserRole ur join ur.role r join r.permissions p "
                        + "where ur.user.id = :id order by p.code", String.class)
                .setParameter("id", userId).getResultList();
    }

    /** Removes the user's own grants and detaches them from grants they made to others. */
    public void deleteForUser(Long userId) {
        update("assignedBy = null where assignedBy.id = ?1", userId);
        delete("user.id", userId);
    }
}
