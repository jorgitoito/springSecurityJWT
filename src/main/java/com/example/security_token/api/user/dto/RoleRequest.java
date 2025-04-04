package com.example.security_token.api.user.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class RoleRequest {

    private String name;

    private Set<PermissionResponse> permissions = new HashSet<>();
}
