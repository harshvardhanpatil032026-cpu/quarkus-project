package com.my.proj.service;

import java.time.Instant;
import java.util.Set;

import com.my.proj.config.JwtConfig;
import com.my.proj.entity.User;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

/** Issues signed JWTs for authenticated users. */
@ApplicationScoped
public class TokenService {

    private final JwtConfig config;

    public TokenService(JwtConfig config) {
        this.config = config;
    }

    public String issueToken(User user) {
        Instant issuedAt = Instant.now();
        return Jwt.issuer(config.issuer())
                .subject(String.valueOf(user.id))
                .upn(user.email)
                .claim("name", user.name)
                .groups(Set.of("user"))
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(config.lifespan()))
                .sign();
    }

    public long lifespanSeconds() {
        return config.lifespan().toSeconds();
    }
}
