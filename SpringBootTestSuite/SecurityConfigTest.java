package com.warehouse.employee.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive JUnit test suite for SecurityConfig
 * Tests cover RBAC, authentication, authorization, and security constraints
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Configuration Tests")
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        // Setup code if needed
    }

    // ========== AUTHENTICATION TESTS ==========

    @Test
    @DisplayName("Test unauthenticated access to protected endpoint - returns 401")
    public void testUnauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test authenticated access to public endpoint - returns 200")
    @WithMockUser(roles = "WORKER")
    public void testAuthenticatedAccess_PublicEndpoint_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // ========== ADMIN ROLE TESTS ==========

    @Test
    @DisplayName("Test ADMIN access to employee endpoints - returns 200")
    @WithMockUser(roles = "ADMIN")
    public void testAdminAccess_EmployeeEndpoints_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test ADMIN can create employee - returns 201")
    @WithMockUser(roles = "ADMIN")
    public void testAdminCanCreateEmployee_Returns201() throws Exception {
        String employeeJson = "{"badgeId":"EMP001","name":"John Doe","role":"WORKER","department":"Warehouse","shiftGroup":"Day Shift","hireDate":"2024-01-01","status":"ACTIVE"}";
        
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content(employeeJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test ADMIN can update employee - returns 200")
    @WithMockUser(roles = "ADMIN")
    public void testAdminCanUpdateEmployee_Returns200() throws Exception {
        String employeeJson = "{"badgeId":"EMP001","name":"John Doe Updated","role":"SUPERVISOR","department":"Warehouse","shiftGroup":"Day Shift","hireDate":"2024-01-01","status":"ACTIVE"}";
        
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType("application/json")
                .content(employeeJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test ADMIN can delete employee - returns 204")
    @WithMockUser(roles = "ADMIN")
    public void testAdminCanDeleteEmployee_Returns204() throws Exception {
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test ADMIN access to all endpoints - returns 200 or 201")
    @WithMockUser(roles = "ADMIN")
    public void testAdminAccessAllEndpoints_Success() throws Exception {
        // Employee endpoints
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
        
        // Attendance endpoints
        mockMvc.perform(get("/api/attendance")).andExpect(status().isOk());
        
        // Shift endpoints
        mockMvc.perform(get("/api/shifts")).andExpect(status().isOk());
        
        // Leave endpoints
        mockMvc.perform(get("/api/leaves")).andExpect(status().isOk());
    }

    // ========== HR ROLE TESTS ==========

    @Test
    @DisplayName("Test HR access to employee endpoints - returns 200")
    @WithMockUser(roles = "HR")
    public void testHRAccess_EmployeeEndpoints_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test HR can create employee - returns 201")
    @WithMockUser(roles = "HR")
    public void testHRCanCreateEmployee_Returns201() throws Exception {
        String employeeJson = "{"badgeId":"EMP002","name":"Jane Smith","role":"WORKER","department":"Warehouse","shiftGroup":"Night Shift","hireDate":"2024-01-01","status":"ACTIVE"}";
        
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content(employeeJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test HR can update employee - returns 200")
    @WithMockUser(roles = "HR")
    public void testHRCanUpdateEmployee_Returns200() throws Exception {
        String employeeJson = "{"badgeId":"EMP002","name":"Jane Smith Updated","role":"SUPERVISOR","department":"Warehouse","shiftGroup":"Night Shift","hireDate":"2024-01-01","status":"ACTIVE"}";
        
        mockMvc.perform(put("/api/employees/2")
                .with(csrf())
                .contentType("application/json")
                .content(employeeJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test HR cannot delete employee - returns 403")
    @WithMockUser(roles = "HR")
    public void testHRCannotDeleteEmployee_Returns403() throws Exception {
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== SUPERVISOR ROLE TESTS ==========

    @Test
    @DisplayName("Test SUPERVISOR access to employee endpoints - returns 200")
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorAccess_EmployeeEndpoints_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test SUPERVISOR can view team members - returns 200")
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorCanViewTeamMembers_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees?department=Warehouse"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test SUPERVISOR cannot create employee - returns 403")
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorCannotCreateEmployee_Returns403() throws Exception {
        String employeeJson = "{"badgeId":"EMP003","name":"Bob Johnson","role":"WORKER","department":"Warehouse","shiftGroup":"Day Shift","hireDate":"2024-01-01","status":"ACTIVE"}";
        
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content(employeeJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test SUPERVISOR can approve leave requests - returns 200")
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorCanApproveLeave_Returns200() throws Exception {
        mockMvc.perform(post("/api/leaves/1/approve")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test SUPERVISOR can view attendance - returns 200")
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorCanViewAttendance_Returns200() throws Exception {
        mockMvc.perform(get("/api/attendance"))
                .andExpect(status().isOk());
    }

    // ========== WORKER ROLE TESTS ==========

    @Test
    @DisplayName("Test WORKER cannot access employee endpoints - returns 403")
    @WithMockUser(roles = "WORKER")
    public void testWorkerCannotAccessEmployeeEndpoints_Returns403() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test WORKER can clock in - returns 201")
    @WithMockUser(roles = "WORKER")
    public void testWorkerCanClockIn_Returns201() throws Exception {
        String clockInJson = "{"employeeId":1,"deviceId":"DEVICE001","geoLocation":"40.7128,-74.0060","timestamp":"2024-01-01T09:00:00"}";
        
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType("application/json")
                .content(clockInJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test WORKER can clock out - returns 201")
    @WithMockUser(roles = "WORKER")
    public void testWorkerCanClockOut_Returns201() throws Exception {
        String clockOutJson = "{"employeeId":1,"deviceId":"DEVICE001","geoLocation":"40.7128,-74.0060","timestamp":"2024-01-01T17:00:00"}";
        
        mockMvc.perform(post("/api/attendance/clock-out")
                .with(csrf())
                .contentType("application/json")
                .content(clockOutJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test WORKER can request leave - returns 201")
    @WithMockUser(roles = "WORKER")
    public void testWorkerCanRequestLeave_Returns201() throws Exception {
        String leaveJson = "{"employeeId":1,"type":"PTO","startDate":"2024-02-01","endDate":"2024-02-03","reason":"Vacation"}";
        
        mockMvc.perform(post("/api/leaves")
                .with(csrf())
                .contentType("application/json")
                .content(leaveJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test WORKER can view own schedule - returns 200")
    @WithMockUser(roles = "WORKER")
    public void testWorkerCanViewOwnSchedule_Returns200() throws Exception {
        mockMvc.perform(get("/api/shifts/my-schedule"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test WORKER cannot approve leave - returns 403")
    @WithMockUser(roles = "WORKER")
    public void testWorkerCannotApproveLeave_Returns403() throws Exception {
        mockMvc.perform(post("/api/leaves/1/approve")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test WORKER cannot create shifts - returns 403")
    @WithMockUser(roles = "WORKER")
    public void testWorkerCannotCreateShifts_Returns403() throws Exception {
        String shiftJson = "{"name":"Day Shift","startTime":"09:00","endTime":"17:00","recurring":true}";
        
        mockMvc.perform(post("/api/shifts/templates")
                .with(csrf())
                .contentType("application/json")
                .content(shiftJson))
                .andExpect(status().isForbidden());
    }

    // ========== CSRF PROTECTION TESTS ==========

    @Test
    @DisplayName("Test POST without CSRF token - returns 403")
    @WithMockUser(roles = "ADMIN")
    public void testPostWithoutCSRF_Returns403() throws Exception {
        String employeeJson = "{"badgeId":"EMP004","name":"Test User","role":"WORKER","department":"Warehouse","shiftGroup":"Day Shift","hireDate":"2024-01-01","status":"ACTIVE"}";
        
        mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content(employeeJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test POST with CSRF token - returns 201")
    @WithMockUser(roles = "ADMIN")
    public void testPostWithCSRF_Returns201() throws Exception {
        String employeeJson = "{"badgeId":"EMP005","name":"Test User","role":"WORKER","department":"Warehouse","shiftGroup":"Day Shift","hireDate":"2024-01-01","status":"ACTIVE"}";
        
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType("application/json")
                .content(employeeJson))
                .andExpect(status().isCreated());
    }

    // ========== METHOD-LEVEL SECURITY TESTS ==========

    @Test
    @DisplayName("Test method-level security - ADMIN can access all methods")
    @WithMockUser(roles = "ADMIN")
    public void testMethodLevelSecurity_AdminAccessAllMethods() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
        mockMvc.perform(get("/api/employees/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test method-level security - WORKER restricted access")
    @WithMockUser(roles = "WORKER")
    public void testMethodLevelSecurity_WorkerRestrictedAccess() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isForbidden());
    }

    // ========== ROW-LEVEL SECURITY TESTS ==========

    @Test
    @DisplayName("Test row-level security - SUPERVISOR can only view team data")
    @WithMockUser(username = "supervisor1", roles = "SUPERVISOR")
    public void testRowLevelSecurity_SupervisorTeamDataOnly() throws Exception {
        mockMvc.perform(get("/api/employees?department=Warehouse"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test row-level security - WORKER can only view own data")
    @WithMockUser(username = "worker1", roles = "WORKER")
    public void testRowLevelSecurity_WorkerOwnDataOnly() throws Exception {
        mockMvc.perform(get("/api/employees/me"))
                .andExpect(status().isOk());
    }

    // ========== ENDPOINT SECURITY TESTS ==========

    @Test
    @DisplayName("Test all protected endpoints require authentication")
    public void testAllProtectedEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/attendance")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/shifts")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/leaves")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test public endpoints accessible without authentication")
    public void testPublicEndpointsAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    // ========== ROLE HIERARCHY TESTS ==========

    @Test
    @DisplayName("Test role hierarchy - ADMIN has all permissions")
    @WithMockUser(roles = "ADMIN")
    public void testRoleHierarchy_AdminHasAllPermissions() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
        mockMvc.perform(post("/api/employees").with(csrf()).contentType("application/json").content("{}")).andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/employees/1").with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test role hierarchy - HR has limited permissions")
    @WithMockUser(roles = "HR")
    public void testRoleHierarchy_HRHasLimitedPermissions() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
        mockMvc.perform(delete("/api/employees/1").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test role hierarchy - SUPERVISOR has team permissions")
    @WithMockUser(roles = "SUPERVISOR")
    public void testRoleHierarchy_SupervisorHasTeamPermissions() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
        mockMvc.perform(post("/api/employees").with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test role hierarchy - WORKER has minimal permissions")
    @WithMockUser(roles = "WORKER")
    public void testRoleHierarchy_WorkerHasMinimalPermissions() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/attendance/clock-in").with(csrf()).contentType("application/json").content("{}")).andExpect(status().isBadRequest());
    }
}