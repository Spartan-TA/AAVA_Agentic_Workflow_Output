package com.warehouse.management.controller;

import com.warehouse.management.service.ShiftService;
import com.warehouse.management.dto.ShiftDTO;
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
 * Comprehensive controller tests for ShiftController
 * Tests REST endpoints, status codes, security, and validation
 */
@WebMvcTest(ShiftController.class)
public class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftService shiftService;

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test GET /api/shifts returns 200 OK")
    void testGetAllShifts_Returns200() throws Exception {
        mockMvc.perform(get("/api/shifts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test POST /api/shifts returns 201 Created")
    void testCreateShift_Returns201() throws Exception {
        String shiftJson = "{"name":"Morning","startTime":"08:00","endTime":"16:00"}";
        mockMvc.perform(post("/api/shifts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(shiftJson))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test POST /api/shifts with WORKER role returns 403 Forbidden")
    void testCreateShift_WorkerRole_Returns403() throws Exception {
        String shiftJson = "{"name":"Morning","startTime":"08:00","endTime":"16:00"}";
        mockMvc.perform(post("/api/shifts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(shiftJson))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test GET /api/shifts without authentication returns 401 Unauthorized")
    void testGetAllShifts_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/shifts"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test PUT /api/shifts/{id} returns 200 OK")
    void testUpdateShift_Returns200() throws Exception {
        String shiftJson = "{"name":"Evening","startTime":"16:00","endTime":"00:00"}";
        mockMvc.perform(put("/api/shifts/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(shiftJson))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test POST /api/shifts/assign returns 200 OK")
    void testAssignShift_Returns200() throws Exception {
        String assignJson = "{"shiftId":1,"employeeId":100}";
        mockMvc.perform(post("/api/shifts/assign")
            .contentType(MediaType.APPLICATION_JSON)
            .content(assignJson))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test GET /api/shifts/employee/{id} returns 200 OK")
    void testGetShiftsByEmployee_Returns200() throws Exception {
        mockMvc.perform(get("/api/shifts/employee/100"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test POST /api/shifts with invalid data returns 400 Bad Request")
    void testCreateShift_InvalidData_Returns400() throws Exception {
        String invalidJson = "{"name":""}";
        mockMvc.perform(post("/api/shifts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test GET /api/shifts/{id} with invalid ID returns 404 Not Found")
    void testGetShiftById_InvalidId_Returns404() throws Exception {
        when(shiftService.getShiftById(999L)).thenThrow(new ShiftNotFoundException());
        mockMvc.perform(get("/api/shifts/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test DELETE /api/shifts/{id} returns 204 No Content")
    void testDeleteShift_Returns204() throws Exception {
        mockMvc.perform(delete("/api/shifts/1"))
            .andExpect(status().isNoContent());
    }
}
