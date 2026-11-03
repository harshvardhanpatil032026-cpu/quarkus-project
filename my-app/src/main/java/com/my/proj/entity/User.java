package com.my.proj.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity 
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User extends PanacheEntity {
    public String name;
    public String email;

    // Salted bcrypt hash. Nullable so rows created before DEMO-86 remain valid.
    @JsonIgnore
    @Column(name = "password_hash")
    public String passwordHash;
}
