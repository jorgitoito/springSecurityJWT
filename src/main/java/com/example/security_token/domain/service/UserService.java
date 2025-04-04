package com.example.security_token.domain.service;

import com.example.security_token.domain.model.Role;
import com.example.security_token.domain.model.UserEntity;
import com.example.security_token.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RoleService roleService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities());
    }

    public UserEntity getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email " + email));

    }

    public UserEntity registerUser(UserEntity user) {
        // Encriptar contraseña antes de guardar
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }


    @Transactional
    public UserEntity updateUserRoles(String email, Set<String> rolesToAdd, Set<String> rolesToRemove) {
        // 1. Obtener el usuario por email
        UserEntity user = this.getUserByEmail(email);

        // 2. Crear una nueva colección para evitar problemas de persistencia
        Set<Role> updatedRoles = new HashSet<>(user.getRoles());

        // 3. Añadir nuevos roles (con verificación)
        if (rolesToAdd != null && !rolesToAdd.isEmpty()) {
            Set<Role> rolesToAddEntities = roleService.findRolesByNameIn(rolesToAdd);
            rolesToAddEntities.forEach(role -> {
                if (updatedRoles.stream().noneMatch(r -> r.getId().equals(role.getId()))) {
                    updatedRoles.add(role);
                }
            });
        }
        log.info("after add: {}", updatedRoles.toString());

        // 4. Eliminar roles (con verificación)
        if (rolesToRemove != null && !rolesToRemove.isEmpty()) {
            updatedRoles.removeIf(role ->
                    rolesToRemove.stream()
                            .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName()))
            );
        }
        log.info("after remove: {}", updatedRoles.toString());

        // 5. Actualizar la relación de roles sin limpiar completamente la colección
        user.setRoles(updatedRoles);
        log.info(String.valueOf(user));

        // 6. Guardar la entidad solo una vez
        return userRepository.save(user);
    }

}