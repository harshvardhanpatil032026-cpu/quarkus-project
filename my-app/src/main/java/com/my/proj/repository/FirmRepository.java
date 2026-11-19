package com.my.proj.repository;

import com.my.proj.entity.Firm;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FirmRepository implements PanacheRepository<Firm> {

    /** True when a firm other than {@code excludeId} (may be null) already uses this code. */
    public boolean codeTaken(String code, Long excludeId) {
        return excludeId == null ? count("code", code) > 0 : count("code = ?1 and id <> ?2", code, excludeId) > 0;
    }
}
