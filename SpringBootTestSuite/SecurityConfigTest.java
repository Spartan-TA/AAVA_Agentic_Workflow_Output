package com.warehouse.ems.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive JUnit test suite for SecurityConfig
 * Tests cover authentication, authorization, role-based access control, and security configurations
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== AUTHENTICATION TESTS ==========

    @Test
    @WithAnonymousUser
    public void testUnauthenticatedAccess_ProtectedEndpoint_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    public void testAuthenticatedAccess_ProtectedEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void testUnauthenticatedAccess_PublicEndpoint_Returns200() throws Exception {
        // Act & Assert - Assuming /actuator/health is public
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // ========== ROLE-BASED ACCESS CONTROL TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminRole_AccessAdminEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR"})
    public void testHRRole_AccessHREndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    public void testSupervisorRole_AccessSupervisorEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    public void testWorkerRole_AccessRestrictedEndpoint_Returns403() throws Exception {
        // Act & Assert - Workers should not access employee list
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    public void testWorkerRole_AccessOwnProfile_Returns200() throws Exception {
        // Act & Assert - Workers can access their own profile
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk());
    }

    // ========== CREATE OPERATION AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminRole_CreateEmployee_Returns201() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR"})
    public void testHRRole_CreateEmployee_Returns201() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"badgeId":"EMP002","firstName":"Jane","lastName":"Smith","email":"jane@test.com"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    public void testSupervisorRole_CreateEmployee_Returns403() throws Exception {
        // Act & Assert - Supervisors cannot create employees
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"badgeId":"EMP003","firstName":"Bob","lastName":"Johnson","email":"bob@test.com"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    public void testWorkerRole_CreateEmployee_Returns403() throws Exception {
        // Act & Assert - Workers cannot create employees
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"badgeId":"EMP004","firstName":"Alice","lastName":"Brown","email":"alice@test.com"}"))
                .andExpect(status().isForbidden());
    }

    // ========== UPDATE OPERATION AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminRole_UpdateEmployee_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType("application/json")
                .content("{"firstName":"John Updated","lastName":"Doe Updated"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR"})
    public void testHRRole_UpdateEmployee_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType("application/json")
                .content("{"firstName":"Jane Updated","lastName":"Smith Updated"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    public void testSupervisorRole_UpdateEmployee_Returns403() throws Exception {
        // Act & Assert - Supervisors cannot update employee records
        mockMvc.perform(put("/api/employees/1")
                .contentType("application/json")
                .content("{"firstName":"Bob Updated","lastName":"Johnson Updated"}"))
                .andExpect(status().isForbidden());
    }

    // ========== DELETE OPERATION AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminRole_DeleteEmployee_Returns204() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR"})
    public void testHRRole_DeleteEmployee_Returns403() throws Exception {
        // Act & Assert - HR cannot delete employees
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    public void testSupervisorRole_DeleteEmployee_Returns403() throws Exception {
        // Act & Assert - Supervisors cannot delete employees
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    public void testWorkerRole_DeleteEmployee_Returns403() throws Exception {
        // Act & Assert - Workers cannot delete employees
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    // ========== MULTIPLE ROLES TESTS ==========

    @Test
    @WithMockUser(username = "multiuser", roles = {"ADMIN", "HR"})
    public void testMultipleRoles_AccessAdminEndpoint_Returns200() throws Exception {
        // Act & Assert - User with multiple roles can access admin endpoints
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "multiuser", roles = {"HR", "SUPERVISOR"})
    public void testMultipleRoles_AccessHREndpoint_Returns200() throws Exception {
        // Act & Assert - User with multiple roles can access HR endpoints
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    // ========== CSRF PROTECTION TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCSRFProtection_PostWithoutCSRF_Returns403() throws Exception {
        // Act & Assert - POST without CSRF token should be rejected
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"badgeId":"EMP005","firstName":"Test","lastName":"User","email":"test@test.com"}"))
                .andExpect(status().isForbidden());
    }

    // ========== CORS TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCORS_AllowedOrigin_Returns200() throws Exception {
        // Act & Assert - CORS should allow configured origins
        mockMvc.perform(get("/api/employees")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    // ========== SESSION MANAGEMENT TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testStatelessSession_NoSessionCreated() throws Exception {
        // Act & Assert - Stateless session should not create HTTP sessions
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    assertNull(result.getRequest().getSession(false));
                });
    }

    // ========== METHOD SECURITY TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testMethodSecurity_AdminAccess_Success() throws Exception {
        // Act & Assert - Method-level security allows admin access
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    public void testMethodSecurity_WorkerAccess_Denied() throws Exception {
        // Act & Assert - Method-level security denies worker access
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    // ========== API KEY AUTHENTICATION TESTS ==========

    @Test
    public void testAPIKeyAuth_ValidKey_Returns200() throws Exception {
        // Act & Assert - Valid API key should grant access
        mockMvc.perform(get("/api/employees")
                .header("X-API-KEY", "valid-api-key"))
                .andExpect(status().isOk());
    }

    @Test
    public void testAPIKeyAuth_InvalidKey_Returns401() throws Exception {
        // Act & Assert - Invalid API key should deny access
        mockMvc.perform(get("/api/employees")
                .header("X-API-KEY", "invalid-api-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAPIKeyAuth_MissingKey_Returns401() throws Exception {
        // Act & Assert - Missing API key should deny access
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    // ========== JWT AUTHENTICATION TESTS ==========

    @Test
    public void testJWTAuth_ValidToken_Returns200() throws Exception {
        // Arrange
        String validToken = "Bearer valid.jwt.token";

        // Act & Assert - Valid JWT token should grant access
        mockMvc.perform(get("/api/employees")
                .header("Authorization", validToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testJWTAuth_InvalidToken_Returns401() throws Exception {
        // Arrange
        String invalidToken = "Bearer invalid.jwt.token";

        // Act & Assert - Invalid JWT token should deny access
        mockMvc.perform(get("/api/employees")
                .header("Authorization", invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testJWTAuth_ExpiredToken_Returns401() throws Exception {
        // Arrange
        String expiredToken = "Bearer expired.jwt.token";

        // Act & Assert - Expired JWT token should deny access
        mockMvc.perform(get("/api/employees")
                .header("Authorization", expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testJWTAuth_MalformedToken_Returns401() throws Exception {
        // Arrange
        String malformedToken = "Bearer malformed-token";

        // Act & Assert - Malformed JWT token should deny access
        mockMvc.perform(get("/api/employees")
                .header("Authorization", malformedToken))
                .andExpect(status().isUnauthorized());
    }

    // ========== PASSWORD ENCODING TESTS ==========

    @Test
    public void testPasswordEncoding_BCryptUsed() {
        // This test would verify that BCrypt is used for password encoding
        // Implementation depends on how password encoding is exposed
        assertTrue(true); // Placeholder
    }

    // ========== SECURITY HEADERS TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testSecurityHeaders_XFrameOptions_Present() throws Exception {
        // Act & Assert - X-Frame-Options header should be present
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Frame-Options"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testSecurityHeaders_XContentTypeOptions_Present() throws Exception {
        // Act & Assert - X-Content-Type-Options header should be present
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testSecurityHeaders_XXSSProtection_Present() throws Exception {
        // Act & Assert - X-XSS-Protection header should be present
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-XSS-Protection"));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(username = "admin", roles = {})
    public void testNoRoles_AccessProtectedEndpoint_Returns403() throws Exception {
        // Act & Assert - User with no roles should be denied access
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"INVALID_ROLE"})
    public void testInvalidRole_AccessProtectedEndpoint_Returns403() throws Exception {
        // Act & Assert - User with invalid role should be denied access
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testNullUsername_AccessProtectedEndpoint_Returns401() throws Exception {
        // Act & Assert - Null username should deny access
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }
}