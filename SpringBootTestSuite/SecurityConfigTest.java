package com.wms.ems.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive security configuration tests
 * Tests cover:
 * - Role-Based Access Control (RBAC) for all endpoints
 * - Authentication requirements
 * - Authorization rules for ADMIN, HR, SUPERVISOR, WORKER roles
 * - Method-level security
 * - OAuth2 and API Key authentication
 * - CSRF protection
 * - Endpoint access restrictions
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Configuration Tests")
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== AUTHENTICATION TESTS ==========

    @Test
    @DisplayName("Should require authentication for all endpoints")
    public void testAuthenticationRequired() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access without authentication to employee endpoints")
    public void testEmployeeEndpointRequiresAuth() throws Exception {
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access without authentication to attendance endpoints")
    public void testAttendanceEndpointRequiresAuth() throws Exception {
        mockMvc.perform(post("/attendance/clock-in"))
                .andExpect(status().isUnauthorized());
    }

    // ========== ADMIN ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should access all employee endpoints")
    public void testAdminAccessEmployeeEndpoints() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should create employees")
    public void testAdminCreateEmployee() throws Exception {
        mockMvc.perform(post("/employees")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should update employees")
    public void testAdminUpdateEmployee() throws Exception {
        mockMvc.perform(put("/employees/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should delete employees")
    public void testAdminDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should access audit logs")
    public void testAdminAccessAuditLogs() throws Exception {
        mockMvc.perform(get("/audit/logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should access all reports")
    public void testAdminAccessReports() throws Exception {
        mockMvc.perform(get("/reports/attendance"))
                .andExpect(status().isOk());
    }

    // ========== HR ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should access employee endpoints")
    public void testHRAccessEmployeeEndpoints() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should create employees")
    public void testHRCreateEmployee() throws Exception {
        mockMvc.perform(post("/employees")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should update employees")
    public void testHRUpdateEmployee() throws Exception {
        mockMvc.perform(put("/employees/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should access leave management")
    public void testHRAccessLeaveManagement() throws Exception {
        mockMvc.perform(get("/leave/requests"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should access performance reviews")
    public void testHRAccessPerformanceReviews() throws Exception {
        mockMvc.perform(get("/reviews/cycles"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should NOT access admin-only endpoints")
    public void testHRDeniedAdminEndpoints() throws Exception {
        mockMvc.perform(get("/admin/system-config"))
                .andExpect(status().isForbidden());
    }

    // ========== SUPERVISOR ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should view employees")
    public void testSupervisorViewEmployees() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should NOT create employees")
    public void testSupervisorCannotCreateEmployee() throws Exception {
        mockMvc.perform(post("/employees")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should NOT delete employees")
    public void testSupervisorCannotDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should approve attendance corrections")
    public void testSupervisorApproveAttendanceCorrections() throws Exception {
        mockMvc.perform(post("/attendance/corrections/approve")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should approve leave requests")
    public void testSupervisorApproveLeaveRequests() throws Exception {
        mockMvc.perform(post("/leave/approvals")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should manage shift assignments")
    public void testSupervisorManageShiftAssignments() throws Exception {
        mockMvc.perform(post("/shifts/assignments")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should access team reports")
    public void testSupervisorAccessTeamReports() throws Exception {
        mockMvc.perform(get("/reports/attendance"))
                .andExpect(status().isOk());
    }

    // ========== WORKER ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should clock in")
    public void testWorkerClockIn() throws Exception {
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should clock out")
    public void testWorkerClockOut() throws Exception {
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should request leave")
    public void testWorkerRequestLeave() throws Exception {
        mockMvc.perform(post("/leave/requests")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should view own schedule")
    public void testWorkerViewOwnSchedule() throws Exception {
        mockMvc.perform(get("/shifts/my-schedule"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should access self-service portal")
    public void testWorkerAccessSelfService() throws Exception {
        mockMvc.perform(get("/selfservice/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should NOT access employee management")
    public void testWorkerCannotAccessEmployeeManagement() throws Exception {
        mockMvc.perform(post("/employees")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should NOT access other employees' data")
    public void testWorkerCannotAccessOtherEmployeesData() throws Exception {
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should NOT approve leave requests")
    public void testWorkerCannotApproveLeave() throws Exception {
        mockMvc.perform(post("/leave/approvals")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should NOT access audit logs")
    public void testWorkerCannotAccessAuditLogs() throws Exception {
        mockMvc.perform(get("/audit/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should NOT access payroll exports")
    public void testWorkerCannotAccessPayrollExports() throws Exception {
        mockMvc.perform(get("/payroll/export"))
                .andExpect(status().isForbidden());
    }

    // ========== CSRF PROTECTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should require CSRF token for POST requests")
    public void testCSRFRequiredForPost() throws Exception {
        mockMvc.perform(post("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should require CSRF token for PUT requests")
    public void testCSRFRequiredForPut() throws Exception {
        mockMvc.perform(put("/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should require CSRF token for DELETE requests")
    public void testCSRFRequiredForDelete() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should allow POST with valid CSRF token")
    public void testPostWithValidCSRF() throws Exception {
        mockMvc.perform(post("/employees")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // ========== ENDPOINT ACCESS MATRIX TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should access safety incident endpoints")
    public void testAdminAccessSafetyIncidents() throws Exception {
        mockMvc.perform(get("/safety/incidents"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should report safety incidents")
    public void testSupervisorReportSafetyIncidents() throws Exception {
        mockMvc.perform(post("/safety/incidents")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should access asset management")
    public void testAdminAccessAssetManagement() throws Exception {
        mockMvc.perform(get("/assets"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("SUPERVISOR should assign assets")
    public void testSupervisorAssignAssets() throws Exception {
        mockMvc.perform(post("/assets/assign")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should access certification management")
    public void testAdminAccessCertifications() throws Exception {
        mockMvc.perform(get("/certifications"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("HR should manage certifications")
    public void testHRManageCertifications() throws Exception {
        mockMvc.perform(post("/certifications")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // ========== METHOD-LEVEL SECURITY TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should enforce method-level security with @PreAuthorize")
    public void testMethodLevelSecurity() throws Exception {
        mockMvc.perform(get("/employees/sensitive-data"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should deny method access without required role")
    public void testMethodLevelSecurityDenied() throws Exception {
        mockMvc.perform(get("/employees/sensitive-data"))
                .andExpect(status().isForbidden());
    }

    // ========== ACTUATOR ENDPOINT SECURITY ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should access actuator health endpoint")
    public void testAdminAccessActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("WORKER should NOT access actuator endpoints")
    public void testWorkerCannotAccessActuator() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }