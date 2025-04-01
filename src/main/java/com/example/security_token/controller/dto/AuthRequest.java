package com.example.security_token.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "El username no puede estar vacío")
    private String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}