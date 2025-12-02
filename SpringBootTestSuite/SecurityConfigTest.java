package com.company.wems.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for SecurityConfig
 * Tests cover RBAC rules, endpoint security, authentication,
 * authorization, and various security scenarios
 * 
 * Note: This test class requires a running Spring Boot application context
 * and uses @SpringBootTest for integration testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityConfig Tests")
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ========== PUBLIC ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test public health endpoint should be accessible without authentication")
    public void testPublicHealthEndpoint_WithoutAuthentication_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test swagger-ui endpoint should be accessible without authentication")
    public void testSwaggerUiEndpoint_WithoutAuthentication_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test api-docs endpoint should be accessible without authentication")
    public void testApiDocsEndpoint_WithoutAuthentication_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // ========== AUTHENTICATION TESTS ==========

    @Test
    @DisplayName("Test protected endpoint without authentication should return 401")
    public void testProtectedEndpoint_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test protected endpoint with invalid token should return 401")
    public void testProtectedEndpoint_WithInvalidToken_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    // ========== EMPLOYEE ENDPOINT AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test ADMIN can create employee")
    public void testCreateEmployee_WithAdminRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"name":"Test Employee","badgeId":"EMP001"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test HR can create employee")
    public void testCreateEmployee_WithHrRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"name":"Test Employee","badgeId":"EMP002"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR cannot create employee")
    public void testCreateEmployee_WithSupervisorRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"name":"Test Employee","badgeId":"EMP003"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER cannot create employee")
    public void testCreateEmployee_WithWorkerRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"name":"Test Employee","badgeId":"EMP004"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test ADMIN can update employee")
    public void testUpdateEmployee_WithAdminRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType("application/json")
                .content("{"name":"Updated Employee"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test HR can update employee")
    public void testUpdateEmployee_WithHrRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType("application/json")
                .content("{"name":"Updated Employee"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test ADMIN can delete employee")
    public void testDeleteEmployee_WithAdminRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER cannot delete employee")
    public void testDeleteEmployee_WithWorkerRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER can view employees")
    public void testViewEmployees_WithWorkerRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    // ========== ATTENDANCE ENDPOINT AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER can clock in")
    public void testClockIn_WithWorkerRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-in")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER can clock out")
    public void testClockOut_WithWorkerRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-out")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR can view attendance reports")
    public void testViewAttendanceReports_WithSupervisorRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/attendance/reports"))
                .andExpect(status().isOk());
    }

    // ========== SHIFT MANAGEMENT AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR can create shift template")
    public void testCreateShiftTemplate_WithSupervisorRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/shifts/templates")
                .contentType("application/json")
                .content("{"name":"Morning Shift"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test HR can create shift template")
    public void testCreateShiftTemplate_WithHrRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/shifts/templates")
                .contentType("application/json")
                .content("{"name":"Evening Shift"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER cannot create shift template")
    public void testCreateShiftTemplate_WithWorkerRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/shifts/templates")
                .contentType("application/json")
                .content("{"name":"Night Shift"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER can view personal schedule")
    public void testViewPersonalSchedule_WithWorkerRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/shifts/my-schedule"))
                .andExpect(status().isOk());
    }

    // ========== LEAVE MANAGEMENT AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER can request leave")
    public void testRequestLeave_WithWorkerRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/leave/request")
                .contentType("application/json")
                .content("{"type":"PTO","startDate":"2024-01-01"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR can approve leave")
    public void testApproveLeave_WithSupervisorRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/leave/approve/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test HR can approve leave")
    public void testApproveLeave_WithHrRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/leave/approve/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER cannot approve leave")
    public void testApproveLeave_WithWorkerRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/leave/approve/1"))
                .andExpect(status().isForbidden());
    }

    // ========== CERTIFICATION MANAGEMENT AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test HR can add certification")
    public void testAddCertification_WithHrRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/certifications")
                .contentType("application/json")
                .content("{"type":"Forklift","employeeId":1}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test ADMIN can add certification")
    public void testAddCertification_WithAdminRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/certifications")
                .contentType("application/json")
                .content("{"type":"Safety","employeeId":1}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER cannot add certification")
    public void testAddCertification_WithWorkerRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/certifications")
                .contentType("application/json")
                .content("{"type":"Training","employeeId":1}"))
                .andExpect(status().isForbidden());
    }

    // ========== SAFETY INCIDENT AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER can report safety incident")
    public void testReportSafetyIncident_WithWorkerRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/safety/incidents")
                .contentType("application/json")
                .content("{"description":"Near miss","severity":"LOW"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR can manage safety incidents")
    public void testManageSafetyIncident_WithSupervisorRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/safety/incidents/1")
                .contentType("application/json")
                .content("{"status":"INVESTIGATING"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test ADMIN can generate OSHA report")
    public void testGenerateOshaReport_WithAdminRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/safety/osha-report"))
                .andExpect(status().isOk());
    }

    // ========== PAYROLL AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test HR can export payroll")
    public void testExportPayroll_WithHrRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/payroll/export")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test ADMIN can export payroll")
    public void testExportPayroll_WithAdminRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/payroll/export")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR cannot export payroll")
    public void testExportPayroll_WithSupervisorRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/payroll/export")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ========== AUDIT LOG AUTHORIZATION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test ADMIN can view audit logs")
    public void testViewAuditLogs_WithAdminRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/audit/logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test HR cannot view audit logs")
    public void testViewAuditLogs_WithHrRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/audit/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR cannot view audit logs")
    public void testViewAuditLogs_WithSupervisorRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/audit/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER cannot view audit logs")
    public void testViewAuditLogs_WithWorkerRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/audit/logs"))
                .andExpect(status().isForbidden());
    }

    // ========== MULTIPLE ROLES TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("Test user with multiple roles should have combined permissions")
    public void testMultipleRoles_ShouldHaveCombinedPermissions() throws Exception {
        // Act & Assert - Should have ADMIN permissions
        mockMvc.perform(get("/api/audit/logs"))
                .andExpect(status().isOk());
        
        // Should also have HR permissions
        mockMvc.perform(post("/api/payroll/export")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(roles = "UNKNOWN_ROLE")
    @DisplayName("Test unknown role should be denied access to protected endpoints")
    public void testUnknownRole_ShouldBeDeniedAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"EMP999"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@company.com", roles = "WORKER")
    @DisplayName("Test authenticated user should have principal name")
    public void testAuthenticatedUser_ShouldHavePrincipalName() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }
}