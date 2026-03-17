package com.warehouse.ems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.dto.ShiftTemplateRequestDto;
import com.warehouse.ems.entity.ShiftTemplate;
import com.warehouse.ems.entity.ShiftAssignment;
import com.warehouse.ems.service.ShiftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ShiftController.
 * Covers successful requests, error responses, security, and validation.
 */
@WebMvcTest(ShiftController.class)
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftService shiftService;

    @Autowired
    private ObjectMapper objectMapper;

    private ShiftTemplate shiftTemplate;
    private ShiftTemplateRequestDto shiftTemplateRequestDto;
    private ShiftAssignment shiftAssignment;

    @BeforeEach
    void setUp() {
        shiftTemplate = new ShiftTemplate();
        shiftTemplate.setId(1L);
        shiftTemplate.setName("Morning Shift");
        shiftTemplate.setStartTime(LocalTime.of(8, 0));
        shiftTemplate.setEndTime(LocalTime.of(16, 0));
        shiftTemplate.setDaysOfWeek("MON,TUE,WED,THU,FRI");
        shiftTemplate.setMaxEmployees(10);

        shiftTemplateRequestDto = new ShiftTemplateRequestDto();
        shiftTemplateRequestDto.setName("Morning Shift");
        shiftTemplateRequestDto.setStartTime(LocalTime.of(8, 0));
        shiftTemplateRequestDto.setEndTime(LocalTime.of(16, 0));
        shiftTemplateRequestDto.setDaysOfWeek("MON,TUE,WED,THU,FRI");
        shiftTemplateRequestDto.setMaxEmployees(10);

        shiftAssignment = new ShiftAssignment();
        shiftAssignment.setId(1L);
        shiftAssignment.setDate(LocalDate.now());
    }

    /**
     * Test POST /shifts/template with ADMIN role returns 201 Created.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShiftTemplate_AdminRole_ReturnsCreated() throws Exception {
        when(shiftService.createShiftTemplate(any(ShiftTemplateRequestDto.class))).thenReturn(shiftTemplate);
        mockMvc.perform(post("/shifts/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shiftTemplateRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Morning Shift"));
    }

    /**
     * Test POST /shifts/template with non-ADMIN role returns 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateShiftTemplate_NonAdminRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/shifts/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shiftTemplateRequestDto)))
                .andExpect(status().isForbidden());
    }

    /**
     * Test POST /shifts/assign with ADMIN role returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAssignShift_AdminRole_ReturnsOk() throws Exception {
        when(shiftService.assignShift(eq(1L), anyList())).thenReturn(List.of(shiftAssignment));
        mockMvc.perform(post("/shifts/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""shiftId":1," +
                        ""employeeIds":[1]}"))
                .andExpect(status().isOk());
    }

    /**
     * Test GET /shifts/conflict with valid params returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDetectConflict_ValidParams_ReturnsOk() throws Exception {
        when(shiftService.detectConflict(eq(1L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class))).thenReturn(false);
        mockMvc.perform(get("/shifts/conflict?employeeId=1&date=2024-06-01&start=08:00&end=16:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    /**
     * Test GET /shifts/employee/{id} with valid params returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeShifts_ValidParams_ReturnsOk() throws Exception {
        when(shiftService.getEmployeeShifts(eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(shiftAssignment));
        mockMvc.perform(get("/shifts/employee/1?startDate=2024-06-01&endDate=2024-06-07"))
                .andExpect(status().isOk());
    }

    /**
     * Test POST /shifts/template with invalid DTO returns 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShiftTemplate_InvalidDto_ReturnsBadRequest() throws Exception {
        ShiftTemplateRequestDto invalidDto = new ShiftTemplateRequestDto();
        mockMvc.perform(post("/shifts/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
