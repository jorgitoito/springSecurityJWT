package com.example.security_token.api.auth;

import com.example.security_token.api.auth.dto.AuthRequest;
import com.example.security_token.api.auth.dto.JwtResponse;
import com.example.security_token.api.auth.dto.UserRegisterRequest;
import com.example.security_token.api.auth.dto.UserResponse;
import com.example.security_token.api.auth.exception.InvalidCredentialsException;
import com.example.security_token.api.exception.TooManyFailedLoginAttemptsException;
import com.example.security_token.domain.model.UserEntity;
import com.example.security_token.security.jwt.JwtTokenProvider;
import com.example.security_token.security.service.LoginAttemptService;
import com.example.security_token.domain.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final LoginAttemptService loginAttemptService;  // Instancia de LoginAttemptService

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider, UserService userService, LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {

        // Verificar si el usuario está bloqueado debido a demasiados intentos fallidos
        if (loginAttemptService.isBlocked(authRequest.getUsername())) {
            // Lanzar una excepción personalizada que se maneja en un manejador global
            throw new TooManyFailedLoginAttemptsException("Too many failed login attempts. Please try again later.");
        }

        try {
            // Autenticar al usuario con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            // Establecer la autenticación en el contexto de seguridad de Spring Security
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Obtener los detalles del usuario autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generar el token JWT para el usuario autenticado
            String jwt = jwtTokenProvider.generateToken(authentication);

            // Obtener los roles del usuario
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Devolver la respuesta con el token JWT y los roles del usuario
            JwtResponse response = new JwtResponse(jwt, userDetails.getUsername(), roles);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            // En caso de que las credenciales sean incorrectas, incrementar los intentos fallidos
            loginAttemptService.loginFailed(authRequest.getUsername());

            // Lanzar una excepción para que sea manejada por el manejador de excepciones global
            throw new InvalidCredentialsException("Invalid username or password.");
        }
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
