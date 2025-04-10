package com.example.security_token.api.user;

import com.example.security_token.api.user.dto.UserPermissionResponse;
import com.example.security_token.domain.model.UserEntity;
import com.example.security_token.domain.service.PermissionService;
import com.example.security_token.domain.service.RoleService;
import com.example.security_token.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPermissionControllerTest {

    @InjectMocks
    private UserPermissionController userPermissionController;

    @Mock
    private UserService userService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private RoleService roleService;

    @Test
    void getUserRolesAndPermissionByUserEmail_shouldReturnUserPermissionResponse_whenUserHasAdminRole() {
        // Arrange
        String email = "test@example.com";
        UserEntity mockUser = new UserEntity(); // Asume que tienes una clase User
        // set up mockUser según necesites para el mapper

        when(userService.getUserByEmail(email)).thenReturn(mockUser);

        // Act
        ResponseEntity<UserPermissionResponse> response = userPermissionController.getUserRolesAndPermissionByUserEmail(email);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // puedes validar más atributos si quieres
    }

    @Test
    void shouldFindMockMakerFile() {
        var resource = Thread.currentThread()
                .getContextClassLoader()
                .getResource("org.mockito.plugins.MockMaker");

        assertNotNull(resource, "MockMaker file not found in classpath");
        System.out.println("MockMaker found at: " + resource);
    }
    
}