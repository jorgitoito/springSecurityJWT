package com.example.security_token.api.user.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserPermissionResponse {

    private String username;

    private String email;
            
    private Set<String> permissions;
    
    private Set<String> roles;
}
