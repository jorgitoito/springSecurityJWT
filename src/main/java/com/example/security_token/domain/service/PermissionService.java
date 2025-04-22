package com.example.security_token.domain.service;

import com.example.security_token.api.user.exception.PermissionException;
import com.example.security_token.domain.model.Permission;
import com.example.security_token.domain.repository.PermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository repository;

    /**
     * Crea un nuevo permiso si no existe ya uno con el mismo nombre
     *
     * @param name Nombre del permiso a crear
     * @throws PermissionException Si ya existe un permiso con ese nombre
     */
    @Transactional
    public Permission createPermission(String name) {
        // Validar que el nombre no esté vacío
        if (name == null || name.trim().isEmpty()) {
            throw new PermissionException("El nombre del permiso no puede estar vacío", HttpStatus.BAD_REQUEST);
        }

        // Verificar si ya existe
        if (this.getPermissionByName(name).isPresent()) {
            throw new PermissionException("Ya existe un permiso con el nombre: " + name, HttpStatus.CONFLICT);
        }

        // Crear y guardar el nuevo permiso
        Permission newPermission = new Permission(name);
        return repository.save(newPermission);
    }

    /**
     * Busca un permiso por su nombre
     *
     * @param name Nombre del permiso a buscar
     * @return Optional con el permiso si existe
     */
    public Optional<Permission> getPermissionByName(String name) {
        return repository.findByName(name);
    }

    public Page<Permission> getAllPermissions(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Método para buscar permisos por nombres
    @Transactional(readOnly = true)
    public Set<Permission> findPermissionsByNameIn(Set<String> permissionNames) {
        if (permissionNames == null || permissionNames.isEmpty()) {
            return Set.of();
        }

        Set<Permission> foundPermissions = repository.findByNameIn(permissionNames);

        // Verificar que todos los permisos solicitados existen
        if (foundPermissions.size() != permissionNames.size()) {
            Set<String> foundNames = foundPermissions.stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());

            Set<String> missing = permissionNames.stream()
                    .filter(name -> !foundNames.contains(name))
                    .collect(Collectors.toSet());

            throw new EntityNotFoundException("Permissions not found: " + missing);
        }

        return foundPermissions;
    }


}