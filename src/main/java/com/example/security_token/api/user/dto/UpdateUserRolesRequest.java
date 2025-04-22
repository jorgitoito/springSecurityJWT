package com.example.security_token.api.user.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRolesRequest {
    private Set<String> rolesToAdd;
    private Set<String> rolesToRemove;
}
