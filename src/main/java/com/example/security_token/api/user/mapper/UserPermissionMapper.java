package com.example.security_token.api.user.mapper;

import com.example.security_token.api.user.dto.PermissionResponse;
import com.example.security_token.api.user.dto.RoleResponse;
import com.example.security_token.api.user.dto.UserPermissionResponse;
import com.example.security_token.domain.model.Permission;
import com.example.security_token.domain.model.Role;
import com.example.security_token.domain.model.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapear objetos de un tipo en otro.
 * 
 * No son objetos complejos, mapear a mano.
 * Para objetos complejos pasar a usar @Mapper MapStruct, que hace lo mismo pero ahorra escribir el codigo....
 */
public class UserPermissionMapper {

    private UserPermissionMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static RoleResponse toRolesResponse(Role role) {
        if (role == null) {
            return null;
        }

        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());

        if (role.getPermissions() != null) {
            Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
                    .map(UserPermissionMapper::toPermissionResponse)
                    .collect(Collectors.toSet());
            response.setPermissions(permissionResponses);
        }

        return response;
    }


    public static UserPermissionResponse toUserPermissionResponse(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        UserPermissionResponse user = new UserPermissionResponse();

        user.setEmail(userEntity.getEmail());
        user.setUsername(userEntity.getUsername());

        // Extraer los nombres de los permisos de las autoridades
        Set<String> permissions = userEntity.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toSet());
        user.setPermissions(permissions);

        // Extraer los nombres de los roles
        Set<String> roles = userEntity.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        user.setRoles(roles);

        return user;
    }

    public static PermissionResponse toPermissionResponse(Permission permission) {
        if (permission == null) {
            return null;
        }
        PermissionResponse permissionResponse = new PermissionResponse();
        permissionResponse.setName(permission.getName());
        return permissionResponse;
    }
}
