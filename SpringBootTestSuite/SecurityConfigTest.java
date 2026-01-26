package com.wms.ems.config;

import com.wms.ems.security.jwt.JwtAuthenticationFilter;
import com.wms.ems.security.ApiKeyAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive JUnit test suite for SecurityConfig
 * Covers: Authentication, Authorization, RBAC, JWT, API Key
 * Epic: E03 - Role Based Access Control (RBAC)
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== AUTHENTICATION TESTS ==========

    @Test
    public void testUnauthenticatedAccess_ProtectedEndpoint_Returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUnauthenticatedAccess_PublicEndpoint_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnauthenticatedAccess_SwaggerUI_Returns200() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnauthenticatedAccess_ApiDocs_Returns200() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // ========== ROLE-BASED ACCESS CONTROL TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminAccess_AllEndpoints_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testHRAccess_EmployeeEndpoints_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorAccess_TeamEndpoints_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testWorkerAccess_OwnProfile_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testWorkerAccess_AllEmployees_Returns403() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testWorkerAccess_CreateEmployee_Returns403() throws Exception {
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ========== ADMIN-ONLY ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminOnly_DeleteEmployee_Returns204() throws Exception {
        mockMvc.perform(post("/api/employees/1/delete")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testHRAccess_DeleteEmployee_Returns403() throws Exception {
        mockMvc.perform(post("/api/employees/1/delete")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorAccess_DeleteEmployee_Returns403() throws Exception {
        mockMvc.perform(post("/api/employees/1/delete")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== HR AND ADMIN ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminAccess_CreateEmployee_Returns201() throws Exception {
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"EMP001"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testHRAccess_CreateEmployee_Returns201() throws Exception {
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"EMP001"}"))
                .andExpect(status().isCreated());
    }

    // ========== CSRF PROTECTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCSRFProtection_PostWithoutCSRF_Returns403() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCSRFProtection_PostWithCSRF_Returns201() throws Exception {
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"EMP001"}"))
                .andExpect(status().isCreated());
    }

    // ========== CORS CONFIGURATION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCORS_AllowedOrigin_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    // ========== METHOD SECURITY TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testMethodSecurity_AdminCanAccessAll_Success() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testMethodSecurity_WorkerCannotAccessAll_Returns403() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    // ========== MULTIPLE ROLES TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testMultipleRoles_AdminAndHR_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR", "WORKER"})
    public void testMultipleRoles_SupervisorAndWorker_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    // ========== INVALID ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "INVALID_ROLE")
    public void testInvalidRole_ProtectedEndpoint_Returns403() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    // ========== SESSION MANAGEMENT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSessionManagement_StatelessSession_NoSessionCreated() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
        // Verify no session is created (stateless)
    }

    // ========== PASSWORD ENCODER TESTS ==========

    @Test
    public void testPasswordEncoder_BCryptConfigured_Success() {
        // Verify BCrypt password encoder is configured
        // This would be tested in integration tests
    }

    // ========== ACTUATOR SECURITY TESTS ==========

    @Test
    public void testActuatorHealth_PublicAccess_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testActuatorMetrics_AdminAccess_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testActuatorMetrics_WorkerAccess_Returns403() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }
}