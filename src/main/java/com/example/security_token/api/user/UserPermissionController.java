package com.example.security_token.api.user;

import com.example.security_token.api.product.exception.ProductException;
import com.example.security_token.api.user.dto.UserPermissionResponse;
import com.example.security_token.domain.model.Role;
import com.example.security_token.domain.model.UserEntity;
import com.example.security_token.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor  // Genera constructor con parámetros finales para inyección
@RestController
@RequestMapping("/api/userPermissions")
public class UserPermissionController {

    private final UserService userService;

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
    
}
