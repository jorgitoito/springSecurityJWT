package com.example.security_token.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
}
