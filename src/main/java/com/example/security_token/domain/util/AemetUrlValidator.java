package com.example.security_token.domain.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;

public class AemetUrlValidator implements ConstraintValidator<ValidAemetUrl, URI> {
    @Override
    public boolean isValid(URI value, ConstraintValidatorContext context) {
        return value != null &&
                value.getHost().endsWith("aemet.es") &&
                value.getScheme().matches("https?");
    }
}
