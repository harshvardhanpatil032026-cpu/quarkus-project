package com.my.proj.dto;

import java.time.Instant;

import com.my.proj.entity.Firm;

public record FirmResponse(Long id, String name, String code, String contactEmail, String phone, String address,
        String status, Instant createdAt, Instant updatedAt) {

    public static FirmResponse from(Firm f) {
        return new FirmResponse(f.getId(), f.getName(), f.getCode(), f.getContactEmail(), f.getPhone(), f.getAddress(),
                f.getStatus().name(), f.getCreatedAt(), f.getUpdatedAt());
    }
}
