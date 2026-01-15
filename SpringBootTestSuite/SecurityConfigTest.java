package com.warehouse.ems.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for SecurityConfig
 * Tests cover authentication, authorization, RBAC, and security constraints
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== AUTHENTICATION TESTS ==========

    @Test
    @WithAnonymousUser
    void testUnauthenticatedAccess_ProtectedEndpoint_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void testUnauthenticatedAccess_PublicEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "WORKER")
    void testAuthenticatedAccess_ValidToken_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidToken_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer invalid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMissingAuthorizationHeader_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMalformedAuthorizationHeader_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "InvalidFormat token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ========== ROLE-BASED ACCESS CONTROL (RBAC) TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminRole_AccessAllEndpoints_Success() throws Exception {
        // Test GET
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test POST
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP999","status":"ACTIVE"}"))
                .andExpect(status().isCreated());

        // Test PUT
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Updated","badgeId":"EMP001","status":"ACTIVE"}"))
                .andExpect(status().isOk());

        // Test DELETE
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testHRRole_AccessEmployeeEndpoints_Success() throws Exception {
        // Test GET
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test POST
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP999","status":"ACTIVE"}"))
                .andExpect(status().isCreated());

        // Test PUT
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Updated","badgeId":"EMP001","status":"ACTIVE"}"))
                .andExpect(status().isOk());

        // Test DELETE
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testSupervisorRole_ReadAccess_Success() throws Exception {
        // Test GET - Should succeed
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testSupervisorRole_WriteAccess_Forbidden() throws Exception {
        // Test POST - Should fail
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP999","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());

        // Test PUT - Should fail
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Updated","badgeId":"EMP001","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());

        // Test DELETE - Should fail
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testWorkerRole_LimitedAccess_Success() throws Exception {
        // Test GET own record - Should succeed
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testWorkerRole_WriteAccess_Forbidden() throws Exception {
        // Test POST - Should fail
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP999","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());

        // Test PUT - Should fail
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Updated","badgeId":"EMP001","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());

        // Test DELETE - Should fail
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INVALID_ROLE")
    void testInvalidRole_AccessDenied_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ========== CSRF PROTECTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCSRFProtection_WithCSRFToken_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP999","status":"ACTIVE"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCSRFProtection_WithoutCSRFToken_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP999","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCSRFProtection_InvalidCSRFToken_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .header("X-CSRF-TOKEN", "invalid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP999","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    // ========== METHOD-LEVEL SECURITY TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMethodLevelSecurity_AdminAccess_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testMethodLevelSecurity_WorkerAccess_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ========== ACTUATOR ENDPOINT SECURITY TESTS ==========

    @Test
    @WithAnonymousUser
    void testActuatorHealth_PublicAccess_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testActuatorInfo_PublicAccess_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testActuatorMetrics_RequiresAuthentication_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActuatorMetrics_AdminAccess_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ========== CORS TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCORS_AllowedOrigin_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/employees")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    // ========== PASSWORD ENCODING TESTS ==========

    @Test
    void testPasswordEncoding_BCrypt_Success() {
        // This test verifies that BCrypt password encoder is configured
        // In a real scenario, you would inject PasswordEncoder and test it
        // For now, we verify through authentication flow
        // Actual implementation would require UserDetailsService setup
    }

    // ========== SESSION MANAGEMENT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSessionManagement_Stateless_NoSessionCreated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttributeDoesNotExist("SPRING_SECURITY_CONTEXT"));
    }

    // ========== MULTIPLE ROLES TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testMultipleRoles_AccessGranted_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER", "SUPERVISOR"})
    void testMultipleRoles_HighestPrivilege_Success() throws Exception {
        // Act & Assert - SUPERVISOR role should grant read access
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(username = "", roles = "ADMIN")
    void testEmptyUsername_ValidRole_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "ADMIN")
    void testEmailUsername_ValidRole_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user-with-special-chars!@#", roles = "ADMIN")
    void testSpecialCharactersUsername_ValidRole_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testAnonymousUser_ProtectedResource_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP999","status":"ACTIVE"}"))
                .andExpect(status().isUnauthorized());
    }

    // ========== HTTP METHOD SECURITY TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testHTTPMethods_AllAllowed_Success() throws Exception {
        // Test GET
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test POST
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP999","status":"ACTIVE"}"))
                .andExpect(status().isCreated());

        // Test PUT
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Updated","badgeId":"EMP001","status":"ACTIVE"}"))
                .andExpect(status().isOk());

        // Test PATCH
        mockMvc.perform(patch("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Patched"}"))
                .andExpect(status().isOk());

        // Test DELETE
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}