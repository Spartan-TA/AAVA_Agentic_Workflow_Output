package com.company.warehousemgmt.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for SecurityConfig
 * Tests cover authentication, authorization, RBAC, and security rules
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== Public Endpoints Tests ==========

    @Test
    @WithAnonymousUser
    void testActuatorHealth_WithoutAuthentication_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testActuatorInfo_WithoutAuthentication_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testSwaggerUI_WithoutAuthentication_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testOpenAPISpec_WithoutAuthentication_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // ========== Authentication Tests ==========

    @Test
    @WithAnonymousUser
    void testProtectedEndpoint_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "WORKER")
    void testProtectedEndpoint_WithAuthentication_ReturnsOkOrForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testPostEndpoint_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void testPutEndpoint_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void testDeleteEndpoint_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ========== RBAC Tests - ADMIN Role ==========

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testGetEmployees_WithAdminRole_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateEmployee_WithAdminRole_ReturnsCreatedOrBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"badgeId":"EMP001","name":"John Doe","role":"WORKER","department":"Warehouse","hireDate":"2023-01-15","status":"ACTIVE"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateEmployee_WithAdminRole_ReturnsOkOrNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType("application/json")
                .content("{"badgeId":"EMP001","name":"John Doe","role":"WORKER","department":"Warehouse","hireDate":"2023-01-15","status":"ACTIVE"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteEmployee_WithAdminRole_ReturnsNoContentOrNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ========== RBAC Tests - HR Role ==========

    @Test
    @WithMockUser(username = "hr", roles = "HR")
    void testGetEmployees_WithHRRole_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "hr", roles = "HR")
    void testCreateEmployee_WithHRRole_ReturnsCreatedOrBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"badgeId":"EMP002","name":"Jane Doe","role":"WORKER","department":"Warehouse","hireDate":"2023-01-15","status":"ACTIVE"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "hr", roles = "HR")
    void testUpdateEmployee_WithHRRole_ReturnsOkOrNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType("application/json")
                .content("{"badgeId":"EMP001","name":"John Doe","role":"WORKER","department":"Warehouse","hireDate":"2023-01-15","status":"ACTIVE"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "hr", roles = "HR")
    void testDeleteEmployee_WithHRRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== RBAC Tests - SUPERVISOR Role ==========

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void testGetEmployees_WithSupervisorRole_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void testCreateEmployee_WithSupervisorRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"badgeId":"EMP003","name":"Bob Smith","role":"WORKER","department":"Warehouse","hireDate":"2023-01-15","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void testUpdateEmployee_WithSupervisorRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType("application/json")
                .content("{"badgeId":"EMP001","name":"John Doe","role":"WORKER","department":"Warehouse","hireDate":"2023-01-15","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = "SUPERVISOR")
    void testDeleteEmployee_WithSupervisorRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== RBAC Tests - WORKER Role ==========

    @Test
    @WithMockUser(username = "worker", roles = "WORKER")
    void testGetEmployees_WithWorkerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "worker", roles = "WORKER")
    void testCreateEmployee_WithWorkerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"badgeId":"EMP004","name":"Alice Johnson","role":"WORKER","department":"Warehouse","hireDate":"2023-01-15","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "worker", roles = "WORKER")
    void testUpdateEmployee_WithWorkerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType("application/json")
                .content("{"badgeId":"EMP001","name":"John Doe","role":"WORKER","department":"Warehouse","hireDate":"2023-01-15","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "worker", roles = "WORKER")
    void testDeleteEmployee_WithWorkerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== CSRF Tests ==========

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testPostEndpoint_WithoutCSRF_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testPutEndpoint_WithoutCSRF_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteEndpoint_WithoutCSRF_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    // ========== Multiple Roles Tests ==========

    @Test
    @WithMockUser(username = "multiRole", roles = {"HR", "SUPERVISOR"})
    void testGetEmployees_WithMultipleRoles_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "multiRole", roles = {"ADMIN", "HR"})
    void testDeleteEmployee_WithMultipleRolesIncludingAdmin_ReturnsNoContentOrNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ========== Edge Case Tests ==========

    @Test
    @WithMockUser(username = "user", roles = "INVALID_ROLE")
    void testGetEmployees_WithInvalidRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {})
    void testGetEmployees_WithNoRoles_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "", roles = "ADMIN")
    void testGetEmployees_WithEmptyUsername_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "ADMIN")
    void testGetNonExistentEndpoint_WithAuthentication_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/nonexistent"))
                .andExpect(status().isNotFound());
    }

    // ========== Actuator Security Tests ==========

    @Test
    @WithAnonymousUser
    void testActuatorMetrics_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testActuatorMetrics_WithAdminRole_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "worker", roles = "WORKER")
    void testActuatorMetrics_WithWorkerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }

    // ========== Content Type Tests ==========

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testPostEndpoint_WithInvalidContentType_ReturnsUnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("text/plain")
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testPostEndpoint_WithXMLContentType_ReturnsUnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/xml")
                .content("<employee></employee>"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========== HTTP Method Tests ==========

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testOptionsRequest_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testHeadRequest_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(head("/api/employees"))
                .andExpect(status().isOk());
    }
}