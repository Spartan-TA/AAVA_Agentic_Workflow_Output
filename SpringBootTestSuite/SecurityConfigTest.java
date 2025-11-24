package com.warehousemgmt.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive JUnit test suite for SecurityConfig
 * Covers RBAC, authentication modes, authorization rules, and security constraints
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== AUTHENTICATION TESTS ==========

    @Test
    public void testUnauthenticatedAccess_ProtectedEndpoint_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUnauthenticatedAccess_PublicEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // ========== ADMIN ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminRole_EmployeeEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminRole_CreateEmployee_Returns201() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"TEST001","role":"WORKER","department":"Warehouse","hireDate":"2023-01-01","status":"ACTIVE"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminRole_AttendanceEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/attendance"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminRole_SafetyEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/safety/incidents"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminRole_PayrollEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/export"))
                .andExpect(status().isOk());
    }

    // ========== HR ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    public void testHRRole_EmployeeEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testHRRole_CreateEmployee_Returns201() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"TEST002","role":"WORKER","department":"Warehouse","hireDate":"2023-01-01","status":"ACTIVE"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testHRRole_DeleteEmployee_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testHRRole_PayrollEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/export"))
                .andExpect(status().isForbidden());
    }

    // ========== SUPERVISOR ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorRole_EmployeeEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorRole_AttendanceEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/attendance"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorRole_CreateEmployee_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"TEST003","role":"WORKER","department":"Warehouse","hireDate":"2023-01-01","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorRole_DeleteEmployee_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testSupervisorRole_PayrollEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/export"))
                .andExpect(status().isForbidden());
    }

    // ========== WORKER ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "WORKER")
    public void testWorkerRole_EmployeeEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testWorkerRole_AttendanceClockIn_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .contentType("application/json")
                .content("{"employeeId":1,"timestamp":"2023-01-01T08:00:00","deviceId":"DEVICE001","location":{"latitude":40.7128,"longitude":-74.0060}}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testWorkerRole_CreateEmployee_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"TEST004","role":"WORKER","department":"Warehouse","hireDate":"2023-01-01","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testWorkerRole_PayrollEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/export"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testWorkerRole_SafetyEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/safety/incidents"))
                .andExpect(status().isForbidden());
    }

    // ========== SAFETY OFFICER ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "SAFETY_OFFICER")
    public void testSafetyOfficerRole_SafetyEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/safety/incidents"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SAFETY_OFFICER")
    public void testSafetyOfficerRole_CreateIncident_Returns201() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/safety/incidents")
                .with(csrf())
                .contentType("application/json")
                .content("{"description":"Test incident","severity":"MINOR","location":"Warehouse A","involvedEmployeeIds":[1]}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SAFETY_OFFICER")
    public void testSafetyOfficerRole_EmployeeEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SAFETY_OFFICER")
    public void testSafetyOfficerRole_PayrollEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/export"))
                .andExpect(status().isForbidden());
    }

    // ========== PAYROLL SPECIALIST ROLE TESTS ==========

    @Test
    @WithMockUser(roles = "PAYROLL_SPECIALIST")
    public void testPayrollSpecialistRole_PayrollEndpoint_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/export"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PAYROLL_SPECIALIST")
    public void testPayrollSpecialistRole_AttendanceReport_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/attendance/report"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PAYROLL_SPECIALIST")
    public void testPayrollSpecialistRole_EmployeeEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PAYROLL_SPECIALIST")
    public void testPayrollSpecialistRole_CreateEmployee_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"TEST005","role":"WORKER","department":"Warehouse","hireDate":"2023-01-01","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    // ========== CSRF PROTECTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCSRFProtection_PostWithoutCSRF_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"TEST006","role":"WORKER","department":"Warehouse","hireDate":"2023-01-01","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCSRFProtection_PostWithCSRF_Returns201() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType("application/json")
                .content("{"name":"Test","badgeId":"TEST007","role":"WORKER","department":"Warehouse","hireDate":"2023-01-01","status":"ACTIVE"}"))
                .andExpect(status().isCreated());
    }

    // ========== MULTIPLE ROLES TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testMultipleRoles_AdminAndHR_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR", "WORKER"})
    public void testMultipleRoles_SupervisorAndWorker_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/attendance"))
                .andExpect(status().isOk());
    }

    // ========== ACTUATOR ENDPOINT TESTS ==========

    @Test
    public void testActuatorHealth_PublicAccess_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testActuatorMetrics_AdminAccess_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testActuatorMetrics_WorkerAccess_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }

    // ========== METHOD SECURITY TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testMethodSecurity_AdminCanDeleteEmployee_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testMethodSecurity_HRCannotDeleteEmployee_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(username = "testuser")
    public void testNoRoles_ProtectedEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INVALID_ROLE")
    public void testInvalidRole_ProtectedEndpoint_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isForbidden());
    }
}