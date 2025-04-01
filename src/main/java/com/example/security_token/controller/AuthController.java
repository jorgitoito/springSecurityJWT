package com.example.security_token.controller;

import com.example.security_token.controller.dto.AuthRequest;
import com.example.security_token.controller.dto.JwtResponse;
import com.example.security_token.controller.dto.UserRegisterRequest;
import com.example.security_token.controller.dto.UserResponse;
import com.example.security_token.persistency.UserEntity;
import com.example.security_token.service.JwtTokenProvider;
import com.example.security_token.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Swagger UI:
 * <a href="http://localhost:8080/swagger-ui/index.html">...</a>
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {

        // Autenticar al usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        // Establecer la autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtener detalles del usuario autenticado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generar token JWT
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Obtener roles del usuario
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Devolver la respuesta con el token
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getUsername(),
                roles
        ));
        
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {

        // Verificar si el username o email ya existen
        if (userService.existsByUsername(userRegisterRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: El nombre de usuario ya está en uso!");
        }

        if (userService.existsByEmail(userRegisterRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: El email ya está en uso!");
        }

        // Crear y guardar el nuevo usuario
        UserEntity newUser = new UserEntity();
        newUser.setUsername(userRegisterRequest.getUsername());
        newUser.setEmail(userRegisterRequest.getEmail());
        newUser.setPassword(userRegisterRequest.getPassword());

        UserEntity registeredUser = userService.registerUser(newUser);

        // Devolver respuesta con los datos del usuario creado (sin contraseña)
        return ResponseEntity.ok(new UserResponse(
                registeredUser.getId(),
                registeredUser.getUsername(),
                registeredUser.getEmail()
        ));
    }

}
