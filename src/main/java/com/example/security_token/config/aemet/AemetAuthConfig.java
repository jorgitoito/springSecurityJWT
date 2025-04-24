package com.example.security_token.config.aemet;

import feign.RequestInterceptor;
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

            // Headers adicionales recomendados
            requestTemplate.header("Accept", "application/json");
            requestTemplate.header("Cache-Control", "no-cache");
        };
    }

}