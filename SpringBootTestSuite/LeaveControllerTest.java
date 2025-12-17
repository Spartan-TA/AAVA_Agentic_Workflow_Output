package com.warehouse.management.controller;

import com.warehouse.management.service.LeaveService;
import com.warehouse.management.dto.LeaveDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive controller tests for LeaveController
 * Tests REST endpoints, status codes, security, and validation
 */
@WebMvcTest(LeaveController.class)
public class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test GET /api/leaves returns 200 OK")
    void testGetAllLeaves_Returns200() throws Exception {
        mockMvc.perform(get("/api/leaves"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test POST /api/leaves returns 201 Created")
    void testCreateLeave_Returns201() throws Exception {
        String leaveJson = "{"employeeId":100,"startDate":"2024-06-01","endDate":"2024-06-05","type":"SICK"}";
        mockMvc.perform(post("/api/leaves")
            .contentType(MediaType.APPLICATION_JSON)
            .content(leaveJson))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test POST /api/leaves/approve returns 200 OK")
    void testApproveLeave_Returns200() throws Exception {
        String approveJson = "{"leaveId":1,"approved":true}";
        mockMvc.perform(post("/api/leaves/approve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(approveJson))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test POST /api/leaves with invalid data returns 400 Bad Request")
    void testCreateLeave_InvalidData_Returns400() throws Exception {
        String invalidJson = "{"employeeId":null,"startDate":"","endDate":"","type":""}";
        mockMvc.perform(post("/api/leaves")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test GET /api/leaves/{id} returns 200 OK")
    void testGetLeaveById_Returns200() throws Exception {
        mockMvc.perform(get("/api/leaves/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test GET /api/leaves/{id} with invalid ID returns 404 Not Found")
    void testGetLeaveById_InvalidId_Returns404() throws Exception {
        when(leaveService.getLeaveById(999L)).thenThrow(new LeaveNotFoundException());
        mockMvc.perform(get("/api/leaves/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test DELETE /api/leaves/{id} returns 204 No Content")
    void testDeleteLeave_Returns204() throws Exception {
        mockMvc.perform(delete("/api/leaves/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test GET /api/leaves without authentication returns 401 Unauthorized")
    void testGetAllLeaves_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/leaves"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test POST /api/leaves with overlapping dates returns 409 Conflict")
    void testCreateLeave_OverlappingDates_Returns409() throws Exception {
        String leaveJson = "{"employeeId":100,"startDate":"2024-06-01","endDate":"2024-06-05","type":"SICK"}";
        doThrow(new LeaveConflictException()).when(leaveService).createLeave(any(LeaveDTO.class));
        mockMvc.perform(post("/api/leaves")
            .contentType(MediaType.APPLICATION_JSON)
            .content(leaveJson))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Test GET /api/leaves/employee/{id} returns 200 OK")
    void testGetLeavesByEmployee_Returns200() throws Exception {
        mockMvc.perform(get("/api/leaves/employee/100"))
            .andExpect(status().isOk());
    }
}
