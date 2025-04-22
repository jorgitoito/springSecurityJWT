package com.example.security_token.api.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Set;

@Data // Lombok
@JsonInclude(JsonInclude.Include.NON_NULL)  // Ignore Null Fields
public class RoleResponse {
    private Long id;
    private String name;
    private Set<PermissionResponse> permissions;
}
