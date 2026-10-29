package com.my.proj.repository;

import com.my.proj.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class UserRepository implements PanacheRepository<User> {
    
}
