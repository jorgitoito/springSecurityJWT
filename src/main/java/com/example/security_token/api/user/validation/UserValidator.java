package com.example.security_token.api.user.validation;

import com.example.security_token.api.user.dto.UpdateRolePermissionsRequest;
import com.example.security_token.api.user.dto.UpdateUserRolesRequest;

public class UserValidator {

    public static final String PERMISSION_NAME_CANNOT_BE_NULL_OR_EMPTY = "Permission name cannot be null or empty";

    private UserValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static void validateRequestRolesToUpdate(UpdateRolePermissionsRequest request) {
        // Validación básica del request
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        // Validar que al menos una operación fue solicitada
        if ((request.getPermissionsToAdd() == null || request.getPermissionsToAdd().isEmpty()) &&
                (request.getPermissionsToRemove() == null || request.getPermissionsToRemove().isEmpty())) {
            throw new IllegalArgumentException("Must specify permissions to add or remove");
        }

        // Validar nombres de permisos
        validatePermissionNames(request);
    }


    public static void validatePermissionNames(UpdateRolePermissionsRequest request) {
        if (request.getPermissionsToAdd() != null) {
            request.getPermissionsToAdd().forEach(name -> {
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException(PERMISSION_NAME_CANNOT_BE_NULL_OR_EMPTY);
                }
            });
        }

        if (request.getPermissionsToRemove() != null) {
            request.getPermissionsToRemove().forEach(name -> {
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException(PERMISSION_NAME_CANNOT_BE_NULL_OR_EMPTY);
                }
            });
        }
    }


    public static void validateRolesNames(UpdateUserRolesRequest request) {
        if (request.getRolesToAdd() != null) {
            request.getRolesToAdd().forEach(name -> {
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException(PERMISSION_NAME_CANNOT_BE_NULL_OR_EMPTY);
                }
            });
        }

        if (request.getRolesToRemove() != null) {
            request.getRolesToRemove().forEach(name -> {
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException(PERMISSION_NAME_CANNOT_BE_NULL_OR_EMPTY);
                }
            });
        }
    }


}
