package com.example.security_token.config;

import com.example.security_token.service.LoginAttemptService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginSecurityConfig {

    @Bean
    public LoginAttemptService loginAttemptService() {
        return new LoginAttemptService();
    }
}
