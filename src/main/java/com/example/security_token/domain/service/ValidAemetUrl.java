package com.example.security_token.domain.service;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AemetUrlValidator.class)
public @interface ValidAemetUrl {
    String message() default "Invalid AEMET URL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}