package com.warehouse.management.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for Security (RBAC & Authentication)
 * Covers normal, boundary, and edge cases
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Any necessary setup for security context
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR can access /api/shifts")
    void testSupervisorAccessShifts() throws Exception {
        mockMvc.perform(get("/api/shifts"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER cannot create shift")
    void testWorkerCannotCreateShift() throws Exception {
        String shiftJson = "{"name":"Morning","startTime":"08:00","endTime":"16:00"}";
        mockMvc.perform(post("/api/shifts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(shiftJson))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test EMPLOYEE can request leave")
    void testEmployeeCanRequestLeave() throws Exception {
        String leaveJson = "{"employeeId":100,"startDate":"2024-06-01","endDate":"2024-06-05","type":"SICK"}";
        mockMvc.perform(post("/api/leaves")
            .contentType(MediaType.APPLICATION_JSON)
            .content(leaveJson))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR can approve leave")
    void testSupervisorCanApproveLeave() throws Exception {
        String approveJson = "{"leaveId":1,"approved":true}";
        mockMvc.perform(post("/api/leaves/approve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(approveJson))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test unauthenticated user cannot access /api/shifts")
    void testUnauthenticatedCannotAccessShifts() throws Exception {
        mockMvc.perform(get("/api/shifts"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR can delete shift")
    void testSupervisorCanDeleteShift() throws Exception {
        mockMvc.perform(delete("/api/shifts/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER cannot delete shift")
    void testWorkerCannotDeleteShift() throws Exception {
        mockMvc.perform(delete("/api/shifts/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test EMPLOYEE cannot approve leave")
    void testEmployeeCannotApproveLeave() throws Exception {
        String approveJson = "{"leaveId":1,"approved":true}";
        mockMvc.perform(post("/api/leaves/approve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(approveJson))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test SUPERVISOR cannot access worker-only endpoint")
    void testSupervisorCannotAccessWorkerEndpoint() throws Exception {
        mockMvc.perform(get("/api/worker/special"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test WORKER can access worker-only endpoint")
    void testWorkerCanAccessWorkerEndpoint() throws Exception {
        mockMvc.perform(get("/api/worker/special"))
            .andExpect(status().isOk());
    }
}
