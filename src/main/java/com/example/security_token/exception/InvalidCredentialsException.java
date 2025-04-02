package com.example.security_token.exception;

// Excepción personalizada para credenciales inválidas
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}