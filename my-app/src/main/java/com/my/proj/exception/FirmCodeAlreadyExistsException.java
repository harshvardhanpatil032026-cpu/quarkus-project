package com.my.proj.exception;

public class FirmCodeAlreadyExistsException extends RuntimeException {

    public FirmCodeAlreadyExistsException(String code) {
        super("A firm with code " + code + " already exists");
    }
}
