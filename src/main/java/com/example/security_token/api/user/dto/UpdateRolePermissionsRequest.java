package com.example.security_token.api.user.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateRolePermissionsRequest {
    private Set<String> permissionsToAdd;
    private Set<String> permissionsToRemove;
}
