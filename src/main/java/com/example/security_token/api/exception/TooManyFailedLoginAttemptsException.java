package com.example.security_token.api.exception;

// Excepción personalizada para demasiados intentos fallidos
public class TooManyFailedLoginAttemptsException extends RuntimeException {
    public TooManyFailedLoginAttemptsException(String message) {
        super(message);
    }
}

