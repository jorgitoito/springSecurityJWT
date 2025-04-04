package com.example.security_token.domain.service;

import com.example.security_token.api.user.dto.PermissionResponse;
import com.example.security_token.api.user.dto.RoleRequest;
import com.example.security_token.api.user.dto.RoleResponse;
import com.example.security_token.domain.model.Permission;
import com.example.security_token.domain.model.Role;
import com.example.security_token.domain.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository repository;

    private final PermissionService permissionService;


    public RoleService(RoleRepository repository, PermissionService permissionService) {
        this.repository = repository;
        this.permissionService = permissionService;
    }


    /**
     * Creates a new role with the specified permissions
     * Role name must noo exists and Permission must exist.
     *
     * @param roleRequest DTO containing role details and permissions
     * @return RoleResponse with created role data
     */
    @Transactional
    public RoleResponse createRole(RoleRequest roleRequest) {
        // Validación del request
        if (roleRequest == null) {
            throw new IllegalArgumentException("Role request cannot be null");
        }

        String roleName = roleRequest.getName();
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        roleName = roleName.trim();

        // Verificar existencia del rol
        if (repository.existsByNameIgnoreCase(roleName)) {
            //throw new ConflictException(String.format("Role '%s' already exists", roleName));
            throw new IllegalArgumentException(String.format("Role '%s' already exists", roleName));
        }

        // Construir la entidad Role
        Role role = new Role();
        role.setName(roleName);

        // Procesar permisos
        if (roleRequest.getPermissions() != null && !roleRequest.getPermissions().isEmpty()) {
            Set<Permission> permissions = processPermissions(roleRequest.getPermissions());
            role.setPermissions(permissions);
        }

        // Guardar y retornar DTO
        Role savedRole = repository.save(role);
        return toRoleResponse(savedRole);
    }


    public Page<Role> getAllRoles(Pageable pageable) {
        return repository.findAll(pageable);
    }
    
    @Transactional
    public Role updateRolePermissions(String  roleName, Set<String> permissionsToAdd, Set<String> permissionsToRemove) {
        Role role = repository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + roleName));

        // Añadir nuevos permisos
        if (permissionsToAdd != null && !permissionsToAdd.isEmpty()) {
            Set<Permission> addPermissions = permissionService.findPermissionsByNameIn(permissionsToAdd);
            role.getPermissions().addAll(addPermissions);
        }

        // Eliminar permisos
        if (permissionsToRemove != null && !permissionsToRemove.isEmpty()) {
            role.getPermissions().removeIf(p -> permissionsToRemove.contains(p.getName()));
        }

        return repository.save(role);
    }

    // Método para buscar permisos por nombres
    @Transactional(readOnly = true)
    public Set<Role> findRolesByNameIn(Set<String> rolesNames) {

        if (rolesNames == null || rolesNames.isEmpty()) {
            return Set.of();
        }

        Set<Role> foundRoles = repository.findByNameIn(rolesNames);

        // Verificar que todos los roles solicitados existen
        if (foundRoles.size() != rolesNames.size()) {
            Set<String> foundNames = foundRoles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            Set<String> missing = rolesNames.stream()
                    .filter(name -> !foundNames.contains(name))
                    .collect(Collectors.toSet());

            throw new EntityNotFoundException("Roles not found: " + missing);
        }

        return foundRoles;

    }
    
    // ----------------------  PRIVATE  -------------------------------

    private Set<Permission> processPermissions(Set<PermissionResponse> permissionResponses) {
        return permissionResponses.stream()
                .map(permissionResponse -> {
                    String permissionName = permissionResponse.getName();
                    return permissionService.getPermissionByName(permissionName)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Permission not found: " + permissionName));
                })
                .collect(Collectors.toCollection(LinkedHashSet::new)); // Mantiene orden de inserción
    }

    private RoleResponse toRoleResponse(Role role) {
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

    private PermissionResponse toPermissionResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setName(permission.getName());
        return response;
    }
    

}
