package com.example.security_token.domain.service;

import com.example.security_token.domain.model.Permission;
import com.example.security_token.domain.model.Role;
import com.example.security_token.domain.model.UserEntity;
import com.example.security_token.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
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
    
    public UserEntity getUserByEmail(String email){
        
        return  userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email "+email));
        
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

        UserEntity user = this.getUserByEmail(email);
        
        // Añadir nuevos roles
        if (rolesToAdd != null && !rolesToAdd.isEmpty()) {
            
            Set<Role> addRoles = roleService.findRolesByNameIn(rolesToAdd);
            user.getRoles().addAll(addRoles);
        }

        // Eliminar roles
        if (rolesToRemove != null && !rolesToRemove.isEmpty()) {
            user.getRoles().removeIf(p -> rolesToRemove.contains(p.getName()));
        }
        return userRepository.save(user);
        
    }
    

}