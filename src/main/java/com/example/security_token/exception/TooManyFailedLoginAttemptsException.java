package com.example.security_token.exception;

// Excepción personalizada para demasiados intentos fallidos
public class TooManyFailedLoginAttemptsException extends RuntimeException {
    public TooManyFailedLoginAttemptsException(String message) {
        super(message);
    }
}

