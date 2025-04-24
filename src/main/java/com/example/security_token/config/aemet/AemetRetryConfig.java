package com.example.security_token.config.aemet;

import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

public class AemetRetryConfig {

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                1000,     // initialInterval
                5000,     // maxInterval
                3       // maxAttempts        
        );
    }

    // Decodificador de errores personalizado
    @Bean
    public ErrorDecoder aemetErrorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == HttpStatus.TOO_MANY_REQUESTS.value() ||
                    response.status() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return new RetryableException(
                        response.status(),
                        "AEMET server error",
                        response.request().httpMethod(),
                        (Long) null,
                        response.request()
                );
            }
            return new RuntimeException("Error en la llamada a AEMET: " + response.status());
        };
    }
}

