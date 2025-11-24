package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.model.Attendance;
import com.warehouse.employee.management.model.Employee;
import com.warehouse.employee.management.service.AttendanceService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for AttendanceController.
 * Tests cover REST endpoints, security, clock-in/out operations, and edge cases.
 * Uses MockMvc for integration testing of controller layer.
 */
@WebMvcTest(AttendanceController.class)
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private Attendance testAttendance;

    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();

        testAttendance = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .clockIn(LocalDateTime.of(2024, 1, 15, 8, 0))
                .clockOut(null)
                .hoursWorked(null)
                .location("Warehouse A")
                .device("Terminal 1")
                .status("PENDING")
                .build();
    }

    // ========== Tests for POST /attendance/clock-in ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testClockIn_AsAdmin_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(anyLong(), anyString(), anyString())).thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "1")
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.location").value("Warehouse A"))
                .andExpect(jsonPath("$.device").value("Terminal 1"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(attendanceService, times(1)).clockIn(1L, "Warehouse A", "Terminal 1");
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testClockIn_AsSupervisor_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(anyLong(), anyString(), anyString())).thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "1")
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_AsWorker_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(anyLong(), anyString(), anyString())).thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "1")
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testClockIn_AsHR_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "1")
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(attendanceService, never()).clockIn(anyLong(), anyString(), anyString());
    }

    @Test
    public void testClockIn_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "1")
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_MissingEmployeeId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_InvalidEmployeeId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "invalid")
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_NonExistentEmployee_ServerError() throws Exception {
        // Arrange
        when(attendanceService.clockIn(anyLong(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Employee not found"));

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "999")
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_EmptyLocation_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(anyLong(), anyString(), anyString())).thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "1")
                .param("location", "")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_EmptyDevice_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(anyLong(), anyString(), anyString())).thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "1")
                .param("location", "Warehouse A")
                .param("device", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_WithoutCsrf_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .param("employeeId", "1")
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ========== Tests for POST /attendance/clock-out ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testClockOut_AsAdmin_Success() throws Exception {
        // Arrange
        Attendance completedAttendance = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .clockIn(LocalDateTime.of(2024, 1, 15, 8, 0))
                .clockOut(LocalDateTime.of(2024, 1, 15, 16, 0))
                .hoursWorked(8.0)
                .location("Warehouse A")
                .device("Terminal 1")
                .status("APPROVED")
                .build();
        
        when(attendanceService.clockOut(anyLong())).thenReturn(completedAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf())
                .param("attendanceId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.hoursWorked").value(8.0))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(attendanceService, times(1)).clockOut(1L);
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testClockOut_AsSupervisor_Success() throws Exception {
        // Arrange
        Attendance completedAttendance = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .clockIn(LocalDateTime.of(2024, 1, 15, 8, 0))
                .clockOut(LocalDateTime.of(2024, 1, 15, 16, 0))
                .hoursWorked(8.0)
                .status("APPROVED")
                .build();
        
        when(attendanceService.clockOut(anyLong())).thenReturn(completedAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf())
                .param("attendanceId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_AsWorker_Success() throws Exception {
        // Arrange
        Attendance completedAttendance = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .clockIn(LocalDateTime.of(2024, 1, 15, 8, 0))
                .clockOut(LocalDateTime.of(2024, 1, 15, 16, 0))
                .hoursWorked(8.0)
                .status("APPROVED")
                .build();
        
        when(attendanceService.clockOut(anyLong())).thenReturn(completedAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf())
                .param("attendanceId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testClockOut_AsHR_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf())
                .param("attendanceId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(attendanceService, never()).clockOut(anyLong());
    }

    @Test
    public void testClockOut_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf())
                .param("attendanceId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_MissingAttendanceId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_InvalidAttendanceId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf())
                .param("attendanceId", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_NonExistentAttendance_ServerError() throws Exception {
        // Arrange
        when(attendanceService.clockOut(anyLong()))
                .thenThrow(new RuntimeException("Attendance record not found"));

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf())
                .param("attendanceId", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_WithoutCsrf_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .param("attendanceId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ========== Tests for GET /attendance/employee/{employeeId} ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAttendanceByEmployee_AsAdmin_Success() throws Exception {
        // Arrange
        Attendance attendance2 = Attendance.builder()
                .id(2L)
                .employee(testEmployee)
                .clockIn(LocalDateTime.of(2024, 1, 16, 8, 0))
                .clockOut(LocalDateTime.of(2024, 1, 16, 16, 0))
                .hoursWorked(8.0)
                .status("APPROVED")
                .build();
        
        List<Attendance> attendanceList = Arrays.asList(testAttendance, attendance2);
        when(attendanceService.getAttendanceByEmployee(anyLong())).thenReturn(attendanceList);

        // Act & Assert
        mockMvc.perform(get("/attendance/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$.length()").value(2));

        verify(attendanceService, times(1)).getAttendanceByEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testGetAttendanceByEmployee_AsHR_Success() throws Exception {
        // Arrange
        List<Attendance> attendanceList = Arrays.asList(testAttendance);
        when(attendanceService.getAttendanceByEmployee(anyLong())).thenReturn(attendanceList);

        // Act & Assert
        mockMvc.perform(get("/attendance/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testGetAttendanceByEmployee_AsSupervisor_Success() throws Exception {
        // Arrange
        List<Attendance> attendanceList = Arrays.asList(testAttendance);
        when(attendanceService.getAttendanceByEmployee(anyLong())).thenReturn(attendanceList);

        // Act & Assert
        mockMvc.perform(get("/attendance/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAttendanceByEmployee_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/attendance/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(attendanceService, never()).getAttendanceByEmployee(anyLong());
    }

    @Test
    public void testGetAttendanceByEmployee_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/attendance/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAttendanceByEmployee_NonExistentEmployee_ServerError() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceByEmployee(anyLong()))
                .thenThrow(new RuntimeException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/attendance/employee/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAttendanceByEmployee_NoRecords_ReturnsEmptyList() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceByEmployee(anyLong())).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/attendance/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAttendanceByEmployee_InvalidEmployeeId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/attendance/employee/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAttendanceByEmployee_ZeroEmployeeId_Success() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceByEmployee(0L)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/attendance/employee/0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAttendanceByEmployee_NegativeEmployeeId_Success() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceByEmployee(-1L)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/attendance/employee/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ========== Additional Edge Case Tests ==========

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_LargeEmployeeId_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(anyLong(), anyString(), anyString())).thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", String.valueOf(Long.MAX_VALUE))
                .param("location", "Warehouse A")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_SpecialCharactersInLocation_Success() throws Exception {
        // Arrange
        when(attendanceService.clockIn(anyLong(), anyString(), anyString())).thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "1")
                .param("location", "Warehouse <>&"'")
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockIn_LongLocationString_Success() throws Exception {
        // Arrange
        String longLocation = "A".repeat(500);
        when(attendanceService.clockIn(anyLong(), anyString(), anyString())).thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .with(csrf())
                .param("employeeId", "1")
                .param("location", longLocation)
                .param("device", "Terminal 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testClockOut_LargeAttendanceId_Success() throws Exception {
        // Arrange
        Attendance completedAttendance = Attendance.builder()
                .id(Long.MAX_VALUE)
                .employee(testEmployee)
                .clockIn(LocalDateTime.of(2024, 1, 15, 8, 0))
                .clockOut(LocalDateTime.of(2024, 1, 15, 16, 0))
                .hoursWorked(8.0)
                .status("APPROVED")
                .build();
        
        when(attendanceService.clockOut(anyLong())).thenReturn(completedAttendance);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .with(csrf())
                .param("attendanceId", String.valueOf(Long.MAX_VALUE))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}