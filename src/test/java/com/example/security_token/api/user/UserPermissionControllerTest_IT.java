package com.example.security_token.api.user;

import com.example.security_token.config.security.SecurityConfig;
import com.example.security_token.domain.model.UserEntity;
import com.example.security_token.domain.service.PermissionService;
import com.example.security_token.domain.service.RoleService;
import com.example.security_token.domain.service.UserService;
import com.example.security_token.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test para probar los Roles y Permisos.
 */
@ActiveProfiles("test")
@WebMvcTest(UserPermissionController.class)
@Import(SecurityConfig.class) // Importa tu configuración de seguridad
class UserPermissionControllerTest_IT {

    @Autowired
    private MockMvc mockMvc;

    // necesario para cargar bien el contexto de seguridad
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private RoleService roleService;

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void shouldDenyAccessToNonAdminRole() throws Exception {
        String email = "test@example.com";

        // Crear un mock de UserEntity con roles
        UserEntity mockUser = new UserEntity();
        //   mockUser.setUsername("test");
        //   mockUser.setEmail(email);
        //mockUser.setRoles(Set.of(new Role("USER"))); // Depende de cómo estés manejando los roles en tu app

        // Configura el mock
        when(userService.getUserByEmail(email)).thenReturn(mockUser);

        // Ejecuta la prueba
        mockMvc.perform(get("/api/userPermissions/{email}", email))
                .andExpect(status().isForbidden()) // Espera 403 Forbidden
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAllowAccessToAdminRole() throws Exception {
        String email = "test@example.com";

        UserEntity mockUser = new UserEntity(); // configurar según sea necesario
        when(userService.getUserByEmail(email)).thenReturn(mockUser);

        mockMvc.perform(get("/api/userPermissions/{email}", email))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test", authorities = {"PERMISSION_WRITE", "USER"})
    void shouldAllowAccessToPERMISSION_WRITEAuthority() throws Exception {
        String email = "test@example.com";
        String permissionName = "PERMISSION_WRITE_TEST"; // Nombre del permiso a crear

        // Crear un mock de UserEntity
        UserEntity mockUser = new UserEntity();
        mockUser.setEmail(email);

        // Configura el mock para que userService devuelva el mockUser cuando se consulte por email
        when(userService.getUserByEmail(email)).thenReturn(mockUser);

        // Ejecuta la prueba con el parámetro 'name' (lo que espera tu @RequestParam)
        mockMvc.perform(post("/api/userPermissions/permissions")
                        .param("name", permissionName)) // Pasa correctamente el parámetro 'name'
                .andExpect(status().isCreated()) // Espera que el status sea 201 Created (por el permiso 'PERMISSION_WRITE')
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "test", authorities = {"PERMISSION_READ"})
    void shouldDenyAccessToPERMISSION_WRITEAuthority() throws Exception {
        String email = "test@example.com";
        String permissionName = "PERMISSION_WRITE_TEST"; // Nombre del permiso a crear

        // Crear un mock de UserEntity
        UserEntity mockUser = new UserEntity();
        mockUser.setEmail(email);

        // Configura el mock para que userService devuelva el mockUser cuando se consulte por email
        when(userService.getUserByEmail(email)).thenReturn(mockUser);

        // Ejecuta la prueba con el parámetro 'name' (lo que espera tu @RequestParam)
        mockMvc.perform(post("/api/userPermissions/permissions")
                        .param("name", permissionName)) // Pasa correctamente el parámetro 'name'
                .andExpect(status().isForbidden()) // Espera 403 Forbidden
                .andDo(print());
    }

}
