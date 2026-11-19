package com.my.proj.repository;

import java.util.List;

import com.my.proj.entity.Role;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {

    public List<Role> listAllWithPermissions() {
        return find("select distinct r from Role r left join fetch r.permissions order by r.code").list();
    }
}
