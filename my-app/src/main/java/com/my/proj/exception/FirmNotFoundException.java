package com.my.proj.exception;

public class FirmNotFoundException extends RuntimeException {

    public FirmNotFoundException(Long id) {
        super("Firm not found with id: " + id);
    }
}
