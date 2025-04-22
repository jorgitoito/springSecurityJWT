package com.example.security_token.api.exception;

import com.example.security_token.api.product.exception.ProductException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    // Manejo de excepciones de validación (para @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(errors);
    }

    // Manejador de excepciones de autenticación
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Error de autenticación", ex.getMessage(), request);
    }

    // Manejador de excepciones de acceso denegado
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Acceso denegado", "No tienes permisos para acceder a este recurso", request);
    }

    // Manejador de credenciales incorrectas
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas", "Revisa el TOKEN enviado", request);
    }

    // Manejador de cuentas deshabilitadas
    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Cuenta deshabilitada", "Tu cuenta está deshabilitada", request);
    }

    // Manejador de cuentas bloqueadas
    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Cuenta bloqueada", "Tu cuenta está bloqueada", request);
    }

    @ExceptionHandler(TooManyFailedLoginAttemptsException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResponseEntity<ErrorResponse> handleTooManyFailedLoginAttemptsException(
            TooManyFailedLoginAttemptsException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Acceso denegado",
                ex.getLocalizedMessage(),
                request.getDescription(false)
        );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-Rate-Limit-Retry-After-Seconds", "15000")
                .body(errorResponse);
    }


    // Manejador de entidad no encontrada
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request);
    }

    // Manejador de nombre de usuario no encontrado
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Username no encontrado", ex.getMessage(), request);
    }

    // Manejo de excepciones personalizadas
    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorResponse> handleProductException(ProductException ex, WebRequest request) {
        return buildErrorResponse(ex.getStatus(), ex.getStatus().getReasonPhrase(), ex.getMessage(), request);
    }

    // Manejador genérico para excepciones no controladas
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", ex.getMessage(), request);
    }

    // Método común para construir la respuesta de error
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String title, String message, WebRequest request) {
        logger.error("Error: {} - {}", title, message);  // Log para depuración
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                title,
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, status);
    }

}