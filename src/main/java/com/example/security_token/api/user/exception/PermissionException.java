package com.example.security_token.api.user.exception;

import org.springframework.http.HttpStatus;

public class PermissionException extends RuntimeException {

    private final HttpStatus status;

    public PermissionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
