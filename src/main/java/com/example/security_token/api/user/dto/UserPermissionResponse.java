package com.example.security_token.api.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)  // Ignore Null Fields
public class UserPermissionResponse {

    private String username;

    private String email;

    private Set<String> permissions;

    private Set<String> roles;
}
