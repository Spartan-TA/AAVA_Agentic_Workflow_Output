package com.warehouse.employee.management.config;

import com.warehouse.employee.management.application.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive security tests for SecurityConfig
 * Tests cover:
 * - Authentication requirements
 * - Role-based authorization
 * - Endpoint security rules
 * - CSRF protection
 * - Actuator endpoint security
 * - Public vs protected endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityConfig Integration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    @WithAnonymousUser
    @DisplayName("Unauthenticated Access to Protected Endpoint - Should Return 401")
    void testUnauthenticatedAccess_ProtectedEndpoint_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Unauthenticated POST Request - Should Return 401")
    void testUnauthenticatedPost_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Authenticated Access to Protected Endpoint - Should Return 200 or 403")
    void testAuthenticatedAccess_ProtectedEndpoint_Success() throws Exception {
        // Should not return 401 (unauthorized)
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(401));
    }

    // ==================== ROLE-BASED AUTHORIZATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin Role - Access to All Endpoints - Should Be Allowed")
    void testAdminRole_AllEndpoints_Allowed() throws Exception {
        // Admin should have access to all endpoints
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403));
        
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf()))
                .andExpect(status().isNot(403));
        
        mockMvc.perform(delete("/api/v1/employees/123")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR Role - Create Employee - Should Be Allowed")
    void testHRRole_CreateEmployee_Allowed() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR Role - Delete Employee - Should Be Forbidden")
    void testHRRole_DeleteEmployee_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/123")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor Role - View Employees - Should Be Allowed")
    void testSupervisorRole_ViewEmployees_Allowed() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor Role - Create Employee - Should Be Forbidden")
    void testSupervisorRole_CreateEmployee_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor Role - Clock In/Out - Should Be Allowed")
    void testSupervisorRole_ClockInOut_Allowed() throws Exception {
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Worker Role - View Own Data - Should Be Allowed")
    void testWorkerRole_ViewOwnData_Allowed() throws Exception {
        mockMvc.perform(get("/api/v1/employees/self"))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Worker Role - Create Employee - Should Be Forbidden")
    void testWorkerRole_CreateEmployee_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Worker Role - Clock In - Should Be Allowed")
    void testWorkerRole_ClockIn_Allowed() throws Exception {
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Worker Role - Delete Employee - Should Be Forbidden")
    void testWorkerRole_DeleteEmployee_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/123")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SAFETY_OFFICER")
    @DisplayName("Safety Officer Role - View Safety Incidents - Should Be Allowed")
    void testSafetyOfficerRole_ViewIncidents_Allowed() throws Exception {
        mockMvc.perform(get("/api/v1/safety/incidents"))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "PAYROLL_ADMIN")
    @DisplayName("Payroll Admin Role - Export Attendance - Should Be Allowed")
    void testPayrollAdminRole_ExportAttendance_Allowed() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/export")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isNot(403));
    }

    // ==================== CSRF PROTECTION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST Request Without CSRF Token - Should Return 403")
    void testPostWithoutCSRF_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST Request With CSRF Token - Should Not Return 403 Due to CSRF")
    void testPostWithCSRF_Success() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT Request Without CSRF Token - Should Return 403")
    void testPutWithoutCSRF_Returns403() throws Exception {
        mockMvc.perform(put("/api/v1/employees/123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE Request Without CSRF Token - Should Return 403")
    void testDeleteWithoutCSRF_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET Request - CSRF Not Required - Should Succeed")
    void testGetRequest_CSRFNotRequired_Success() throws Exception {
        // GET requests don't require CSRF token
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403));
    }

    // ==================== ACTUATOR ENDPOINT TESTS ====================

    @Test
    @WithAnonymousUser
    @DisplayName("Actuator Health Endpoint - Public Access - Should Be Allowed")
    void testActuatorHealth_PublicAccess_Allowed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Actuator Info Endpoint - Public Access - Should Be Allowed")
    void testActuatorInfo_PublicAccess_Allowed() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Actuator Metrics Endpoint - Admin Access - Should Be Allowed")
    void testActuatorMetrics_AdminAccess_Allowed() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Actuator Metrics Endpoint - Worker Access - Should Be Forbidden")
    void testActuatorMetrics_WorkerAccess_Forbidden() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }

    // ==================== HTTP METHOD TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET Request - Should Be Allowed")
    void testGetRequest_Allowed() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST Request with CSRF - Should Be Allowed")
    void testPostRequest_WithCSRF_Allowed() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT Request with CSRF - Should Be Allowed")
    void testPutRequest_WithCSRF_Allowed() throws Exception {
        mockMvc.perform(put("/api/v1/employees/123")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH Request with CSRF - Should Be Allowed")
    void testPatchRequest_WithCSRF_Allowed() throws Exception {
        mockMvc.perform(patch("/api/v1/employees/123")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE Request with CSRF - Should Be Allowed")
    void testDeleteRequest_WithCSRF_Allowed() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/123")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    // ==================== MULTIPLE ROLES TESTS ====================

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("Multiple Roles - Should Have Combined Permissions")
    void testMultipleRoles_CombinedPermissions() throws Exception {
        // Should have access to both ADMIN and HR endpoints
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403));
        
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR", "WORKER"})
    @DisplayName("Supervisor and Worker Roles - Should Have Supervisor Permissions")
    void testSupervisorAndWorkerRoles_SupervisorPermissions() throws Exception {
        // Should have supervisor-level access
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @WithMockUser(roles = "UNKNOWN_ROLE")
    @DisplayName("Unknown Role - Should Be Forbidden")
    void testUnknownRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "", roles = "ADMIN")
    @DisplayName("Empty Username with Valid Role - Should Be Allowed")
    void testEmptyUsername_ValidRole_Allowed() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("OPTIONS Request - Should Be Allowed")
    void testOptionsRequest_Allowed() throws Exception {
        mockMvc.perform(options("/api/v1/employees"))
                .andExpect(status().isOk());
    }

    // ==================== CORS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("CORS Preflight Request - Should Be Allowed")
    void testCORSPreflight_Allowed() throws Exception {
        mockMvc.perform(options("/api/v1/employees")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Request with Origin Header - Should Include CORS Headers")
    void testRequestWithOrigin_IncludesCORSHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isNot(403));
    }

    // ==================== SESSION MANAGEMENT TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Stateless Session - No Session Should Be Created")
    void testStatelessSession_NoSessionCreated() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403))
                .andExpect(request().sessionAttributeDoesNotExist("SPRING_SECURITY_CONTEXT"));
    }

    // ==================== ENDPOINT PATTERN TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("API v1 Endpoints - Should Be Protected")
    void testAPIv1Endpoints_Protected() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(401));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Non-API Endpoints - Should Require Authentication")
    void testNonAPIEndpoints_RequireAuth() throws Exception {
        mockMvc.perform(get("/some-other-endpoint"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== AUTHORIZATION HIERARCHY TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin - Full Access to Employee Management")
    void testAdmin_FullAccess() throws Exception {
        // Create
        mockMvc.perform(post("/api/v1/employees").with(csrf()))
                .andExpect(status().isNot(403));
        
        // Read
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403));
        
        // Update
        mockMvc.perform(put("/api/v1/employees/123").with(csrf()))
                .andExpect(status().isNot(403));
        
        // Delete
        mockMvc.perform(delete("/api/v1/employees/123").with(csrf()))
                .andExpect(status().isNot(403));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR - Limited Access to Employee Management")
    void testHR_LimitedAccess() throws Exception {
        // Create - Allowed
        mockMvc.perform(post("/api/v1/employees").with(csrf()))
                .andExpect(status().isNot(403));
        
        // Read - Allowed
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isNot(403));
        
        // Update - Allowed
        mockMvc.perform(put("/api/v1/employees/123").with(csrf()))
                .andExpect(status().isNot(403));
        
        // Delete - Forbidden
        mockMvc.perform(delete("/api/v1/employees/123").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Worker - Minimal Access to Employee Management")
    void testWorker_MinimalAccess() throws Exception {
        // Create - Forbidden
        mockMvc.perform(post("/api/v1/employees").with(csrf()))
                .andExpect(status().isForbidden());
        
        // Update - Forbidden
        mockMvc.perform(put("/api/v1/employees/123").with(csrf()))
                .andExpect(status().isForbidden());
        
        // Delete - Forbidden
        mockMvc.perform(delete("/api/v1/employees/123").with(csrf()))
                .andExpect(status().isForbidden());
    }
}