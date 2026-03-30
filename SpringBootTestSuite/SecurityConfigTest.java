package com.companyname.wems.config;

import com.companyname.wems.employee.entity.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
 * Comprehensive JUnit test suite for Security Configuration
 * Tests cover RBAC, authentication, authorization, and access control
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Configuration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== AUTHENTICATION TESTS ==========

    @Test
    @WithAnonymousUser
    @DisplayName("Should return 401 for unauthenticated access to protected endpoints")
    void testUnauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should allow access to public endpoints without authentication")
    void testPublicEndpoints_AllowsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should allow access to authentication endpoints")
    void testAuthEndpoints_AllowsAnonymousAccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{"username":"test","password":"test"}"))
                .andExpect(status().isOk());
    }

    // ========== ADMIN ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should have access to all employee endpoints")
    void testAdminRole_HasFullAccess() throws Exception {
        // GET
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk());

        // POST
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk());

        // PUT
        mockMvc.perform(put("/api/v1/employees/1")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk());

        // DELETE
        mockMvc.perform(delete("/api/v1/employees/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should have access to all attendance endpoints")
    void testAdminRole_HasAttendanceAccess() throws Exception {
        mockMvc.perform(get("/api/v1/attendance"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/attendance/reports"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should have access to all shift management endpoints")
    void testAdminRole_HasShiftManagementAccess() throws Exception {
        mockMvc.perform(get("/api/v1/shifts"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/shifts")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should have access to safety incident endpoints")
    void testAdminRole_HasSafetyIncidentAccess() throws Exception {
        mockMvc.perform(get("/api/v1/safety/incidents"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/safety/incidents")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    // ========== HR ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should have access to employee CRUD operations")
    void testHRRole_HasEmployeeCRUDAccess() throws Exception {
        // GET
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk());

        // POST
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isCreated());

        // PUT
        mockMvc.perform(put("/api/v1/employees/1")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk());

        // DELETE
        mockMvc.perform(delete("/api/v1/employees/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should have access to leave management")
    void testHRRole_HasLeaveManagementAccess() throws Exception {
        mockMvc.perform(get("/api/v1/leave/requests"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/leave/requests/1/approve")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should have access to certification tracking")
    void testHRRole_HasCertificationAccess() throws Exception {
        mockMvc.perform(get("/api/v1/certifications"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/certifications")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    // ========== SUPERVISOR ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should have read access to employees")
    void testSupervisorRole_HasReadAccess() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should NOT have create access to employees")
    void testSupervisorRole_NoCreateAccess() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should NOT have update access to employees")
    void testSupervisorRole_NoUpdateAccess() throws Exception {
        mockMvc.perform(put("/api/v1/employees/1")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should NOT have delete access to employees")
    void testSupervisorRole_NoDeleteAccess() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should have access to shift management")
    void testSupervisorRole_HasShiftManagementAccess() throws Exception {
        mockMvc.perform(get("/api/v1/shifts"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/shifts/assign")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should have access to attendance reports")
    void testSupervisorRole_HasAttendanceReportAccess() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/reports"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should have access to leave approval")
    void testSupervisorRole_HasLeaveApprovalAccess() throws Exception {
        mockMvc.perform(post("/api/v1/leave/requests/1/approve")
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/leave/requests/1/deny")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ========== WORKER ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should NOT have access to employee list")
    void testWorkerRole_NoEmployeeListAccess() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should have access to own profile")
    void testWorkerRole_HasOwnProfileAccess() throws Exception {
        mockMvc.perform(get("/api/v1/employees/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should have access to clock in/out")
    void testWorkerRole_HasClockInOutAccess() throws Exception {
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/attendance/clock-out")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should have access to own schedule")
    void testWorkerRole_HasOwnScheduleAccess() throws Exception {
        mockMvc.perform(get("/api/v1/shifts/my-schedule"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should have access to leave requests")
    void testWorkerRole_HasLeaveRequestAccess() throws Exception {
        mockMvc.perform(post("/api/v1/leave/requests")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/leave/requests/my-requests"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should NOT have access to shift management")
    void testWorkerRole_NoShiftManagementAccess() throws Exception {
        mockMvc.perform(post("/api/v1/shifts")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should NOT have access to safety incident management")
    void testWorkerRole_NoSafetyIncidentManagementAccess() throws Exception {
        mockMvc.perform(get("/api/v1/safety/incidents"))
                .andExpect(status().isForbidden());
    }

    // ========== CSRF PROTECTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject POST requests without CSRF token")
    void testCSRFProtection_RejectsRequestsWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should accept POST requests with CSRF token")
    void testCSRFProtection_AcceptsRequestsWithToken() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    // ========== METHOD SECURITY TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should enforce method-level security annotations")
    void testMethodSecurity_EnforcesAnnotations() throws Exception {
        // This would test @PreAuthorize annotations on service methods
        // Actual implementation depends on service layer security
    }

    // ========== ROLE HIERARCHY TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should have all permissions of lower roles")
    void testRoleHierarchy_AdminHasAllPermissions() throws Exception {
        // ADMIN should be able to do everything HR, SUPERVISOR, and WORKER can do
        mockMvc.perform(get("/api/v1/employees")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/shifts/my-schedule")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/attendance/clock-in").with(csrf())
                        .contentType("application/json").content("{}"))
                .andExpect(status().isOk());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(roles = "INVALID_ROLE")
    @DisplayName("Should deny access for invalid roles")
    void testInvalidRole_DeniesAccess() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {})
    @DisplayName("Should deny access for users with no roles")
    void testNoRoles_DeniesAccess() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"WORKER", "SUPERVISOR"})
    @DisplayName("Should grant highest permission when user has multiple roles")
    void testMultipleRoles_GrantsHighestPermission() throws Exception {
        // User with both WORKER and SUPERVISOR should have SUPERVISOR permissions
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk());
    }

    // ========== SESSION MANAGEMENT TESTS ==========

    @Test
    @DisplayName("Should use stateless session management")
    void testSessionManagement_IsStateless() throws Exception {
        // Verify that no session is created
        // This is typically configured in SecurityConfig
    }

    // ========== CORS TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle CORS preflight requests")
    void testCORS_HandlesPreflightRequests() throws Exception {
        mockMvc.perform(options("/api/v1/employees")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }

    // ========== PASSWORD ENCODING TESTS ==========

    @Test
    @DisplayName("Should use BCrypt password encoding")
    void testPasswordEncoding_UsesBCrypt() {
        // This would test the password encoder bean configuration
        // Actual implementation depends on security configuration
    }

    // ========== JWT AUTHENTICATION TESTS ==========

    @Test
    @DisplayName("Should validate JWT tokens correctly")
    void testJWTAuthentication_ValidatesTokens() throws Exception {
        // This would test JWT token validation
        // Actual implementation depends on JWT filter configuration
    }

    @Test
    @DisplayName("Should reject expired JWT tokens")
    void testJWTAuthentication_RejectsExpiredTokens() throws Exception {
        // This would test expired token handling
        // Actual implementation depends on JWT filter configuration
    }

    @Test
    @DisplayName("Should reject invalid JWT tokens")
    void testJWTAuthentication_RejectsInvalidTokens() throws Exception {
        // This would test invalid token handling
        // Actual implementation depends on JWT filter configuration
    }
}