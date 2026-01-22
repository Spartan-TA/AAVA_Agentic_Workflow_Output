package com.warehouse.ems.attendance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive controller tests for Attendance
 * Tests cover: clock-in/out endpoints, validation, security, error handling
 */
@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should clock in successfully")
    void testClockIn() throws Exception {
        // Arrange
        Employee employee = Employee.builder().id(1L).name("John Doe").build();
        AttendanceEvent event = AttendanceEvent.builder()
                .id(1L)
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .deviceId("DEVICE123")
                .location("Warehouse A")
                .build();

        when(attendanceService.clockIn(any(), any(), any())).thenReturn(event);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"deviceId":"DEVICE123","location":"Warehouse A"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("IN"))
                .andExpect(jsonPath("$.deviceId").value("DEVICE123"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 400 when clocking in with invalid data")
    void testClockInInvalidData() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // Missing required fields
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 409 when already clocked in")
    void testDuplicateClockIn() throws Exception {
        // Arrange
        when(attendanceService.clockIn(any(), any(), any()))
                .thenThrow(new IllegalStateException("Already clocked in"));

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"deviceId":"DEVICE123","location":"Warehouse A"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Already clocked in")));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should clock out successfully")
    void testClockOut() throws Exception {
        // Arrange
        Employee employee = Employee.builder().id(1L).name("John Doe").build();
        AttendanceEvent event = AttendanceEvent.builder()
                .id(2L)
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.OUT)
                .deviceId("DEVICE123")
                .location("Warehouse A")
                .build();

        when(attendanceService.clockOut(any(), any(), any())).thenReturn(event);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"deviceId":"DEVICE123","location":"Warehouse A"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("OUT"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 400 when clocking out without clock in")
    void testClockOutWithoutClockIn() throws Exception {
        // Arrange
        when(attendanceService.clockOut(any(), any(), any()))
                .thenThrow(new IllegalStateException("No clock-in found"));

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"deviceId":"DEVICE123","location":"Warehouse A"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("No clock-in found")));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Should get daily attendance")
    void testGetDailyAttendance() throws Exception {
        // Arrange
        Employee employee = Employee.builder().id(1L).name("John Doe").build();
        List<AttendanceEvent> events = Arrays.asList(
            AttendanceEvent.builder()
                .id(1L)
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .build(),
            AttendanceEvent.builder()
                .id(2L)
                .employee(employee)
                .timestamp(LocalDateTime.now().plusHours(8))
                .type(EventType.OUT)
                .build()
        );
        when(attendanceService.getDailyAttendance(any(), any())).thenReturn(events);

        // Act & Assert
        mockMvc.perform(get("/attendance/daily")
                .param("employeeId", "1")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type").value("IN"))
                .andExpect(jsonPath("$[1].type").value("OUT"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Should return empty list when no attendance records")
    void testGetDailyAttendanceEmpty() throws Exception {
        // Arrange
        when(attendanceService.getDailyAttendance(any(), any())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/attendance/daily")
                .param("employeeId", "1")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should export attendance to CSV")
    void testExportAttendanceCSV() throws Exception {
        // Arrange
        String csv = "Employee,Timestamp,Type,Device,Location
John Doe,2024-01-15T09:00:00,IN,DEVICE123,Warehouse A";
        when(attendanceService.exportToCSV(any(), any())).thenReturn(csv);

        // Act & Assert
        mockMvc.perform(get("/attendance/export")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(containsString("John Doe")));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Should create missed punch correction")
    void testCreateMissedPunchCorrection() throws Exception {
        // Arrange
        Employee employee = Employee.builder().id(1L).name("John Doe").build();
        AttendanceEvent correction = AttendanceEvent.builder()
                .id(3L)
                .employee(employee)
                .timestamp(LocalDateTime.now().minusHours(2))
                .type(EventType.OUT)
                .build();

        when(attendanceService.createMissedPunchCorrection(any(), any(), any(), any()))
                .thenReturn(correction);

        // Act & Assert
        mockMvc.perform(post("/attendance/correction")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"type":"OUT","timestamp":"2024-01-15T17:00:00","reason":"Forgot to clock out"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("OUT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should calculate hours worked")
    void testCalculateHoursWorked() throws Exception {
        // Arrange
        when(attendanceService.calculateHours(any(), any())).thenReturn(8.0);

        // Act & Assert
        mockMvc.perform(get("/attendance/hours")
                .param("clockInId", "1")
                .param("clockOutId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours").value(8.0));
    }

    @Test
    @DisplayName("Should return 401 when accessing without authentication")
    void testUnauthorizedAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should validate geofence location")
    void testGeofenceValidation() throws Exception {
        // Arrange
        when(attendanceService.validateGeofence(any(), any(), any())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/attendance/validate-geofence")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"location":"40.7128,-74.0060","warehouseLocation":"40.7128,-74.0060","radius":100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should reject clock-in outside geofence")
    void testGeofenceRejection() throws Exception {
        // Arrange
        when(attendanceService.clockIn(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Location outside geofence"));

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"deviceId":"DEVICE123","location":"34.0522,-118.2437"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("outside geofence")));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should get attendance summary by date range")
    void testGetAttendanceSummary() throws Exception {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(
            AttendanceEvent.builder().id(1L).type(EventType.IN).build(),
            AttendanceEvent.builder().id(2L).type(EventType.OUT).build()
        );
        when(attendanceService.getAttendanceSummary(any(), any())).thenReturn(events);

        // Act & Assert
        mockMvc.perform(get("/attendance/summary")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}