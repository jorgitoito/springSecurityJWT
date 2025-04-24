package com.example.security_token.config.aemet;

import feign.Response;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class AemetRetryConfig {

    private static final int INITIAL_INTERVAL = 1000; // 1 segundo
    private static final int MAX_INTERVAL = 5000;     // 5 segundos
    private static final int MAX_ATTEMPTS = 3;        // 3 intentos totales

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                INITIAL_INTERVAL,
                MAX_INTERVAL,
                MAX_ATTEMPTS
        );
    }

    @Bean
    public ErrorDecoder aemetErrorDecoder() {
        return (methodKey, response) -> {
            final int status = response.status();
            final String requestMethod = response.request().httpMethod().toString();
            final String requestUrl = response.request().url();

            if (shouldRetry(status)) {
                return createRetryableException(response, status, requestMethod);
            }
            return createAppropriateException(status, requestMethod, requestUrl);
        };
    }

    private boolean shouldRetry(int status) {
        return status == HttpStatus.TOO_MANY_REQUESTS.value() ||
                status >= HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private RetryableException createRetryableException(
            Response response, int status, String requestMethod) {

        long retryAfterMillis = extractRetryAfter(response);

        return new RetryableException(
                status,
                getErrorMessage(status),
                response.request().httpMethod(),
                retryAfterMillis,  // Use Long directly instead of Date
                response.request()
        );
    }

    private long extractRetryAfter(Response response) {
        try {
            if (response.headers().containsKey("Retry-After")) {
                String retryAfter = response.headers().get("Retry-After").iterator().next();
                return Long.parseLong(retryAfter) * 1000; // Convertir a milisegundos
            }
        } catch (Exception e) {
            // Fallback si hay error al parsear el header
        }
        return INITIAL_INTERVAL; // Usar intervalo inicial por defecto
    }

    private String getErrorMessage(int status) {
        if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return "Límite de tasa excedido en AEMET";
        }
        return "Error temporal en servidor AEMET";
    }

    private RuntimeException createAppropriateException(
            int status, String requestMethod, String requestUrl) {

        String errorMessage = String.format(
                "Error %d en %s %s - %s",
                status,
                requestMethod,
                requestUrl,
                getClientErrorDescription(status)
        );

        return new RuntimeException(errorMessage);
    }

    private String getClientErrorDescription(int status) {
        return switch (status) {
            case 400 -> "Solicitud mal formada";
            case 401 -> "No autorizado - Verifique la API Key";
            case 403 -> "Acceso prohibido";
            case 404 -> "Recurso no encontrado";
            default -> "Error en la solicitud";
        };
    }
}