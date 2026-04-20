package com.turno.crm.exception;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
