package com.warehouse.ems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.dto.CorrectionRequestDto;
import com.warehouse.ems.entity.AttendanceEvent;
import com.warehouse.ems.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AttendanceController.
 * Covers successful requests, error responses, security, and validation.
 */
@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Autowired
    private ObjectMapper objectMapper;

    private AttendanceEvent attendanceEvent;

    @BeforeEach
    void setUp() {
        attendanceEvent = new AttendanceEvent();
        attendanceEvent.setId(1L);
        attendanceEvent.setClockIn(LocalDateTime.now().minusHours(8));
        attendanceEvent.setClockOut(LocalDateTime.now());
        attendanceEvent.setHoursWorked(8.0);
        attendanceEvent.setDeviceId("DEV1");
        attendanceEvent.setLocation("Main Gate");
        attendanceEvent.setEventType("CLOCK_IN");
    }

    /**
     * Test POST /attendance/clock-in with WORKER role returns 201 Created.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    void testClockIn_WorkerRole_ReturnsCreated() throws Exception {
        when(attendanceService.clockIn(eq(1L), eq("DEV1"), eq("Main Gate"))).thenReturn(attendanceEvent);
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""employeeId":1," +
                        ""deviceId":"DEV1"," +
                        ""location":"Main Gate"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("CLOCK_IN"));
    }

    /**
     * Test POST /attendance/clock-in with unauthorized role returns 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "HR")
    void testClockIn_UnauthorizedRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""employeeId":1," +
                        ""deviceId":"DEV1"," +
                        ""location":"Main Gate"}"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test POST /attendance/clock-out with SUPERVISOR role returns 201 Created.
     */
    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testClockOut_SupervisorRole_ReturnsCreated() throws Exception {
        when(attendanceService.clockOut(eq(1L))).thenReturn(attendanceEvent);
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""employeeId":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("CLOCK_IN"));
    }

    /**
     * Test GET /attendance/employee/{id} with ADMIN role returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAttendanceByEmployee_AdminRole_ReturnsOk() throws Exception {
        when(attendanceService.getAttendanceByEmployee(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(attendanceEvent));
        mockMvc.perform(get("/attendance/employee/1?date=2024-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    /**
     * Test POST /attendance/corrections with SUPERVISOR role returns 201 Created.
     */
    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testCreateCorrection_SupervisorRole_ReturnsCreated() throws Exception {
        CorrectionRequestDto dto = new CorrectionRequestDto();
        dto.setClockIn(LocalDateTime.now().minusHours(7));
        dto.setClockOut(LocalDateTime.now());
        when(attendanceService.createCorrection(eq(1L), any(CorrectionRequestDto.class))).thenReturn(attendanceEvent);
        mockMvc.perform(post("/attendance/corrections")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""eventId":1," +
                        ""clockIn":"2024-06-01T08:00:00"," +
                        ""clockOut":"2024-06-01T16:00:00"}"))
                .andExpect(status().isCreated());
    }

    /**
     * Test POST /attendance/corrections with WORKER role returns 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateCorrection_WorkerRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/attendance/corrections")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""eventId":1," +
                        ""clockIn":"2024-06-01T08:00:00"," +
                        ""clockOut":"2024-06-01T16:00:00"}"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test GET /attendance/employee/{id} with no authentication returns 401 Unauthorized.
     */
    @Test
    void testGetAttendanceByEmployee_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/attendance/employee/1?date=2024-06-01"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test POST /attendance/clock-in with invalid input returns 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    void testClockIn_InvalidInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""employeeId":null," +
                        ""deviceId":""," +
                        ""location":""}"))
                .andExpect(status().isBadRequest());
    }
}
