package com.my.proj.config;

import java.time.Duration;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/** Settings for the JWTs issued on login. */
@ConfigMapping(prefix = "app.auth.jwt")
public interface JwtConfig {

    String issuer();

    /** How long an issued token stays valid. */
    @WithDefault("3600S")
    Duration lifespan();
}
