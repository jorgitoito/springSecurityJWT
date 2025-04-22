package com.example.security_token.api.user;

import com.example.security_token.domain.model.Permission;
import com.example.security_token.domain.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test integracion.
 * Controller --> Service --> Repository --> BBDD
 */

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserPermissionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void cleanDatabase() {
        permissionRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
        // Simula autenticación
    void testCreatePermission() throws Exception {
        String permissionName = "USER_READ";

        mockMvc.perform(post("/api/userPermissions/permissions")
                        .param("name", permissionName))
                .andExpect(status().isCreated());

        // Verificamos que se haya guardado
        Optional<Permission> permission = permissionRepository.findByName(permissionName);
        assertTrue(permission.isPresent());
    }
}
