package com.example.security_token.config.aemet;

import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AemetAuthConfig {

    @Value("${aemet.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return requestTemplate -> {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("API Key de AEMET no configurada");
            }
            requestTemplate.header("api_key", apiKey);
        };
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 2000, 3); // Reintentos para fallos temporales
    }
}