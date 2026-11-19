package com.my.proj.config;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;

/** Settings for the first platform administrator. */
@ConfigMapping(prefix = "app.bootstrap")
public interface BootstrapConfig {

    /**
     * Email of an existing user who is granted PLATFORM_ADMIN at startup. Unset by default: no one
     * is promoted unless this is configured.
     */
    Optional<String> platformAdminEmail();
}
