package com.example.security_token.api.user;

import com.example.security_token.api.exception.ErrorResponse;
import com.example.security_token.api.user.dto.PermissionResponse;
import com.example.security_token.api.user.dto.RoleRequest;
import com.example.security_token.api.user.dto.RoleResponse;
import com.example.security_token.api.user.dto.UpdateRolePermissionsRequest;
import com.example.security_token.api.user.dto.UserPermissionResponse;
import com.example.security_token.domain.model.Permission;
import com.example.security_token.domain.model.Role;
import com.example.security_token.domain.model.UserEntity;
import com.example.security_token.domain.service.PermissionService;
import com.example.security_token.domain.service.RoleService;
import com.example.security_token.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor  // Genera constructor con parámetros finales para inyección
@RestController
@RequestMapping("/api/userPermissions")
public class UserPermissionController {

    private final UserService userService;

    private final PermissionService permissionService;
    
    private final RoleService roleService;

    @Operation(summary = "Get Roles and Permissions by User email", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPermissionResponse> getUserRolesAndPermissionByUserEmail(@Valid @PathVariable String email) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        this.toUserPermissionResponse(userService.getUserByEmail(email))
                );
    }


    @Operation(summary = "Create Permission", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<Void> createPermission(@Valid @RequestParam String name) {
        permissionService.createPermission(name);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @Operation(summary = "Get All Permissions with pagination",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Number of items per page", example = "10"),
                    @Parameter(name = "sort", description = "Sorting criteria (field,asc|desc)", example = "name,asc")
            })
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_READ') or hasRole('ADMIN')")
    public ResponseEntity<Page<PermissionResponse>> getAllPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        // Procesar el parámetro de ordenamiento
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1
                ? Sort.Direction.fromString(sortParams[1])
                : Sort.Direction.ASC;

        // Crear objeto Pageable para la paginación
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // Obtener los permisos paginados
        Page<Permission> permissionsPage = permissionService.getAllPermissions(pageable);

        // Convertir Page<Permission> a Page<PermissionResponse>
        Page<PermissionResponse> responsePage = permissionsPage.map(this::toPermissionResponse);

        return ResponseEntity.ok(responsePage);
    }

    @Operation(summary = "Create Role", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ROLES_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<Void> createRole(@Valid @RequestBody RoleRequest role) {
        roleService.createRole(role);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @Operation(summary = "Get All Roles with pagination",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Number of items per page", example = "10"),
                    @Parameter(name = "sort", description = "Sorting criteria (field,asc|desc)", example = "name,asc")
            })
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_READ') or hasRole('ADMIN')")
    public ResponseEntity<Page<RoleResponse>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        // Procesar el parámetro de ordenamiento
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1
                ? Sort.Direction.fromString(sortParams[1])
                : Sort.Direction.ASC;

        // Crear objeto Pageable para la paginación
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // Obtener los permisos paginados
        Page<Role> rolesPage = roleService.getAllRoles(pageable);

        // Convertir Page<Permission> a Page<PermissionResponse>
        Page<RoleResponse> responsePage = rolesPage.map(this::toRolesResponse);

        return ResponseEntity.ok(responsePage);
    }

    @Operation(summary = "Update Role Permissions",
            description = "Add or remove permissions from a role by role name",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role updated successfully",
                            content = @Content(schema = @Schema(implementation = RoleResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Role or permission not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden access",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))                    
            })
    @PutMapping("/roles/{roleName}/permissions")
    @PreAuthorize("hasAuthority('ROLES_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> updateRolePermissions(
            @Parameter(description = "Name of the role to update", required = true)
            @PathVariable String roleName,

            @Parameter(description = "Permissions to add and remove")
            @Valid @RequestBody UpdateRolePermissionsRequest request) {

        this.validateRequestRolesToUpdate(request);

        Role updatedRole = roleService.updateRolePermissions(
                roleName,
                request.getPermissionsToAdd() != null ? request.getPermissionsToAdd() : Set.of(),
                request.getPermissionsToRemove() != null ? request.getPermissionsToRemove() : Set.of()
        );

        return ResponseEntity.ok(toRolesResponse(updatedRole));
    }

    private void validateRequestRolesToUpdate(UpdateRolePermissionsRequest request) {
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

    private RoleResponse toRolesResponse(Role role) {
        if (role == null) {
            return null;
        }

        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());

        if (role.getPermissions() != null) {
            Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
                    .map(this::toPermissionResponse)
                    .collect(Collectors.toSet());
            response.setPermissions(permissionResponses);
        }

        return response;
    }


    private UserPermissionResponse toUserPermissionResponse(UserEntity userEntity) {
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
    
    private PermissionResponse toPermissionResponse(Permission permission){
        if (permission == null) {
            return null;
        }
        PermissionResponse permissionResponse = new PermissionResponse();
        permissionResponse.setName(permission.getName());
        return permissionResponse;
    }

    private void validatePermissionNames(UpdateRolePermissionsRequest request) {
        if (request.getPermissionsToAdd() != null) {
            request.getPermissionsToAdd().forEach(name -> {
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException("Permission name cannot be null or empty");
                }
            });
        }

        if (request.getPermissionsToRemove() != null) {
            request.getPermissionsToRemove().forEach(name -> {
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException("Permission name cannot be null or empty");
                }
            });
        }
    }
    
    

}
