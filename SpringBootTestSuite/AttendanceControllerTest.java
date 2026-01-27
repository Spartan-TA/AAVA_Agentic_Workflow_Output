package com.warehouse.ems.controller.attendance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.dto.attendance.AttendanceEventRequest;
import com.warehouse.ems.dto.attendance.AttendanceEventResponse;
import com.warehouse.ems.service.attendance.AttendanceService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for AttendanceController.
 * Tests cover all attendance endpoints, security, and error handling.
 */
@WebMvcTest(AttendanceController.class)
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AttendanceService attendanceService;

    private AttendanceEventRequest testRequest;
    private AttendanceEventResponse testResponse;

    @BeforeEach
    public void setUp() {
        testRequest = new AttendanceEventRequest();
        testRequest.setEmployeeId(1L);
        testRequest.setLocation("Warehouse A");
        testRequest.setDevice("Terminal-01");

        testResponse = AttendanceEventResponse.builder()
                .id(1L)
                .employeeId(1L)
                .clockIn(LocalDateTime.now())
                .location("Warehouse A")
                .device("Terminal-01")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== CLOCK IN TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_AsWorker_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(any(AttendanceEventRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.location").value("Warehouse A"));

        verify(attendanceService, times(1)).clockIn(any(AttendanceEventRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testClockIn_AsAdmin_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(any(AttendanceEventRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testClockIn_AsSupervisor_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(any(AttendanceEventRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testClockIn_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_InvalidInput_BadRequest() throws Exception {
        // Arrange - missing required field
        testRequest.setEmployeeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_NonExistentEmployee_NotFound() throws Exception {
        // Arrange
        when(attendanceService.clockIn(any(AttendanceEventRequest.class)))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_WithoutLocation_Success() throws Exception {
        // Arrange
        testRequest.setLocation(null);
        when(attendanceService.clockIn(any(AttendanceEventRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_WithoutDevice_Success() throws Exception {
        // Arrange
        testRequest.setDevice(null);
        when(attendanceService.clockIn(any(AttendanceEventRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk());
    }

    // ==================== CLOCK OUT TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_AsWorker_Success() throws Exception {
        // Arrange
        testResponse.setClockOut(LocalDateTime.now());
        when(attendanceService.clockOut(1L)).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-out/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clockOut").exists());

        verify(attendanceService, times(1)).clockOut(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testClockOut_AsAdmin_Success() throws Exception {
        // Arrange
        testResponse.setClockOut(LocalDateTime.now());
        when(attendanceService.clockOut(1L)).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-out/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    public void testClockOut_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-out/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_NonExistentEvent_NotFound() throws Exception {
        // Arrange
        when(attendanceService.clockOut(999L))
                .thenThrow(new EntityNotFoundException("Attendance event not found"));

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-out/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_AlreadyClockedOut_BadRequest() throws Exception {
        // Arrange
        when(attendanceService.clockOut(1L))
                .thenThrow(new IllegalStateException("Employee has already clocked out"));

        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-out/1")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_InvalidId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/attendance/clock-out/invalid")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET ATTENDANCE FOR EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAttendanceForEmployee_AsWorker_Success() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceForEmployee(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testResponse));

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].employeeId").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAttendanceForEmployee_AsAdmin_Success() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceForEmployee(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testResponse));

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testGetAttendanceForEmployee_AsSupervisor_Success() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceForEmployee(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testResponse));

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAttendanceForEmployee_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1")
                .param("date", "2024-01-15"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAttendanceForEmployee_NonExistentEmployee_NotFound() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceForEmployee(anyLong(), any(LocalDate.class)))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/999")
                .param("date", "2024-01-15"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAttendanceForEmployee_NoRecords_EmptyList() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceForEmployee(anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAttendanceForEmployee_InvalidDateFormat_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1")
                .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAttendanceForEmployee_FutureDate_Success() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceForEmployee(anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1")
                .param("date", "2025-12-31"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAttendanceForEmployee_PastDate_Success() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceForEmployee(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testResponse));

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1")
                .param("date", "2020-01-01"))
                .andExpect(status().isOk());
    }

    // ==================== GET HOURS WORKED TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetHoursWorked_AsWorker_Success() throws Exception {
        // Arrange
        when(attendanceService.calculateHoursWorked(anyLong(), any(LocalDate.class)))
                .thenReturn(8.0);

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1/hours")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().string("8.0"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetHoursWorked_AsAdmin_Success() throws Exception {
        // Arrange
        when(attendanceService.calculateHoursWorked(anyLong(), any(LocalDate.class)))
                .thenReturn(8.0);

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1/hours")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testGetHoursWorked_AsHR_Success() throws Exception {
        // Arrange
        when(attendanceService.calculateHoursWorked(anyLong(), any(LocalDate.class)))
                .thenReturn(8.0);

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1/hours")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetHoursWorked_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1/hours")
                .param("date", "2024-01-15"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetHoursWorked_NoRecords_ReturnsZero() throws Exception {
        // Arrange
        when(attendanceService.calculateHoursWorked(anyLong(), any(LocalDate.class)))
                .thenReturn(0.0);

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1/hours")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().string("0.0"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetHoursWorked_PartialHours_Success() throws Exception {
        // Arrange
        when(attendanceService.calculateHoursWorked(anyLong(), any(LocalDate.class)))
                .thenReturn(4.5);

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1/hours")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetHoursWorked_OvertimeHours_Success() throws Exception {
        // Arrange
        when(attendanceService.calculateHoursWorked(anyLong(), any(LocalDate.class)))
                .thenReturn(12.0);

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1/hours")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().string("12.0"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetHoursWorked_NonExistentEmployee_NotFound() throws Exception {
        // Arrange
        when(attendanceService.calculateHoursWorked(anyLong(), any(LocalDate.class)))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/999/hours")
                .param("date", "2024-01-15"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetHoursWorked_InvalidDateFormat_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1/hours")
                .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetHoursWorked_MissingDateParameter_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/attendance/employee/1/hours"))
                .andExpect(status().isBadRequest());
    }
}