package com.warehouse.employee.management.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.management.application.service.AttendanceService;
import com.warehouse.employee.management.application.service.AttendanceReportService;
import com.warehouse.employee.management.domain.attendance.*;
import com.warehouse.employee.management.domain.employee.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for AttendanceController
 * Tests cover:
 * - Clock in/out endpoints
 * - Attendance status queries
 * - Correction workflow
 * - Report generation
 * - Security/authorization
 * - Error handling
 * - Edge cases
 */
@WebMvcTest(AttendanceController.class)
@DisplayName("AttendanceController Integration Tests")
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private AttendanceReportService attendanceReportService;

    private Employee testEmployee;
    private AttendanceRecord testAttendanceRecord;
    private ClockEventMetadata clockMetadata;
    private UUID testEmployeeId;
    private UUID testAttendanceId;

    @BeforeEach
    void setUp() {
        testEmployeeId = UUID.randomUUID();
        testAttendanceId = UUID.randomUUID();

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(testEmployeeId);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");

        // Setup clock metadata
        clockMetadata = new ClockEventMetadata();
        clockMetadata.setDeviceId("DEVICE001");
        clockMetadata.setDeviceType("MOBILE");
        clockMetadata.setIpAddress("192.168.1.100");
        clockMetadata.setLatitude(37.7749);
        clockMetadata.setLongitude(-122.4194);

        // Setup test attendance record
        testAttendanceRecord = new AttendanceRecord();
        testAttendanceRecord.setId(testAttendanceId);
        testAttendanceRecord.setEmployee(testEmployee);
        testAttendanceRecord.setAttendanceDate(LocalDate.now());
        testAttendanceRecord.setClockInTime(LocalDateTime.now().withHour(8).withMinute(0));
        testAttendanceRecord.setClockInMetadata(clockMetadata);
        testAttendanceRecord.setStatus(AttendanceStatus.CLOCKED_IN);
    }

    // ==================== CLOCK IN TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/clock-in - Normal Case - Should Clock In Successfully")
    void testClockIn_NormalCase_Success() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());
        request.put("deviceId", "DEVICE001");
        request.put("deviceType", "MOBILE");
        request.put("ipAddress", "192.168.1.100");
        request.put("latitude", 37.7749);
        request.put("longitude", -122.4194);

        when(attendanceService.clockIn(eq(testEmployeeId), any(ClockEventMetadata.class)))
                .thenReturn(testAttendanceRecord);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAttendanceId.toString()))
                .andExpect(jsonPath("$.status").value("CLOCKED_IN"));

        verify(attendanceService).clockIn(eq(testEmployeeId), any(ClockEventMetadata.class));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("POST /attendance/clock-in - Supervisor Role - Should Clock In Successfully")
    void testClockIn_SupervisorRole_Success() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());
        request.put("deviceId", "DEVICE001");

        when(attendanceService.clockIn(eq(testEmployeeId), any(ClockEventMetadata.class)))
                .thenReturn(testAttendanceRecord);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /attendance/clock-in - Unauthenticated - Should Return Unauthorized")
    void testClockIn_Unauthenticated_Unauthorized() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/clock-in - Missing Employee ID - Should Return Bad Request")
    void testClockIn_MissingEmployeeId_BadRequest() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("deviceId", "DEVICE001");

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/clock-in - Already Clocked In - Should Return Conflict")
    void testClockIn_AlreadyClockedIn_Conflict() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());
        request.put("deviceId", "DEVICE001");

        when(attendanceService.clockIn(eq(testEmployeeId), any(ClockEventMetadata.class)))
                .thenThrow(new IllegalStateException("Already clocked in"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/clock-in - Without Geolocation - Should Clock In Successfully")
    void testClockIn_WithoutGeolocation_Success() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());
        request.put("deviceId", "DEVICE001");
        // No latitude/longitude

        when(attendanceService.clockIn(eq(testEmployeeId), any(ClockEventMetadata.class)))
                .thenReturn(testAttendanceRecord);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ==================== CLOCK OUT TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/clock-out - Normal Case - Should Clock Out Successfully")
    void testClockOut_NormalCase_Success() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());
        request.put("deviceId", "DEVICE001");
        request.put("deviceType", "MOBILE");
        request.put("ipAddress", "192.168.1.100");

        testAttendanceRecord.setClockOutTime(LocalDateTime.now().withHour(17).withMinute(0));
        testAttendanceRecord.setStatus(AttendanceStatus.COMPLETED);
        testAttendanceRecord.setTotalHours(9.0);

        when(attendanceService.clockOut(eq(testEmployeeId), any(ClockEventMetadata.class)))
                .thenReturn(testAttendanceRecord);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-out")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalHours").value(9.0));

        verify(attendanceService).clockOut(eq(testEmployeeId), any(ClockEventMetadata.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/clock-out - Not Clocked In - Should Return Conflict")
    void testClockOut_NotClockedIn_Conflict() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());
        request.put("deviceId", "DEVICE001");

        when(attendanceService.clockOut(eq(testEmployeeId), any(ClockEventMetadata.class)))
                .thenThrow(new IllegalStateException("Not clocked in"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-out")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/clock-out - Employee Not Found - Should Return Not Found")
    void testClockOut_EmployeeNotFound_NotFound() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());
        request.put("deviceId", "DEVICE001");

        when(attendanceService.clockOut(eq(testEmployeeId), any(ClockEventMetadata.class)))
                .thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-out")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== GET CURRENT ATTENDANCE TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("GET /attendance/current/{employeeId} - Clocked In - Should Return Current Attendance")
    void testGetCurrentAttendance_ClockedIn_Success() throws Exception {
        // Arrange
        when(attendanceService.getCurrentAttendance(testEmployeeId))
                .thenReturn(Optional.of(testAttendanceRecord));

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/current/{employeeId}", testEmployeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAttendanceId.toString()))
                .andExpect(jsonPath("$.status").value("CLOCKED_IN"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("GET /attendance/current/{employeeId} - Not Clocked In - Should Return Not Found")
    void testGetCurrentAttendance_NotClockedIn_NotFound() throws Exception {
        // Arrange
        when(attendanceService.getCurrentAttendance(testEmployeeId))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/current/{employeeId}", testEmployeeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("GET /attendance/current/{employeeId} - Supervisor Role - Should Return Attendance")
    void testGetCurrentAttendance_SupervisorRole_Success() throws Exception {
        // Arrange
        when(attendanceService.getCurrentAttendance(testEmployeeId))
                .thenReturn(Optional.of(testAttendanceRecord));

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/current/{employeeId}", testEmployeeId))
                .andExpect(status().isOk());
    }

    // ==================== GET CLOCK STATUS TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("GET /attendance/status/{employeeId} - Clocked In - Should Return True")
    void testGetClockStatus_ClockedIn_ReturnsTrue() throws Exception {
        // Arrange
        when(attendanceService.getCurrentAttendance(testEmployeeId))
                .thenReturn(Optional.of(testAttendanceRecord));

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/status/{employeeId}", testEmployeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isClockedIn").value(true));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("GET /attendance/status/{employeeId} - Not Clocked In - Should Return False")
    void testGetClockStatus_NotClockedIn_ReturnsFalse() throws Exception {
        // Arrange
        when(attendanceService.getCurrentAttendance(testEmployeeId))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/status/{employeeId}", testEmployeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isClockedIn").value(false));
    }

    // ==================== ATTENDANCE CORRECTION TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/corrections - Normal Case - Should Create Correction Request")
    void testRequestCorrection_NormalCase_Success() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("attendanceId", testAttendanceId.toString());
        request.put("requestedById", testEmployeeId.toString());
        request.put("correctionType", "MISSED_CLOCK_IN");
        request.put("correctedTime", LocalDateTime.now().toString());
        request.put("reason", "Forgot to clock in");

        AttendanceCorrection correction = new AttendanceCorrection();
        correction.setId(UUID.randomUUID());
        correction.setStatus(CorrectionStatus.PENDING);
        correction.setCorrectionType(CorrectionType.MISSED_CLOCK_IN);

        when(attendanceService.requestCorrection(
                eq(testAttendanceId), eq(testEmployeeId), any(CorrectionType.class), 
                any(LocalDateTime.class), eq("Forgot to clock in")
        )).thenReturn(correction);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/corrections")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/corrections - Missing Reason - Should Return Bad Request")
    void testRequestCorrection_MissingReason_BadRequest() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("attendanceId", testAttendanceId.toString());
        request.put("requestedById", testEmployeeId.toString());
        request.put("correctionType", "MISSED_CLOCK_IN");
        request.put("correctedTime", LocalDateTime.now().toString());
        // Missing reason

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/corrections")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("POST /attendance/corrections/{id}/approve - Normal Case - Should Approve Correction")
    void testApproveCorrection_NormalCase_Success() throws Exception {
        // Arrange
        UUID correctionId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        Map<String, Object> request = new HashMap<>();
        request.put("reviewerId", reviewerId.toString());

        AttendanceCorrection correction = new AttendanceCorrection();
        correction.setId(correctionId);
        correction.setStatus(CorrectionStatus.APPROVED);

        when(attendanceService.approveCorrection(correctionId, reviewerId))
                .thenReturn(correction);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/corrections/{id}/approve", correctionId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/corrections/{id}/approve - Worker Role - Should Return Forbidden")
    void testApproveCorrection_WorkerRole_Forbidden() throws Exception {
        // Arrange
        UUID correctionId = UUID.randomUUID();
        Map<String, Object> request = new HashMap<>();
        request.put("reviewerId", testEmployeeId.toString());

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/corrections/{id}/approve", correctionId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ==================== REPORT EXPORT TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("GET /attendance/export - Normal Case - Should Export CSV")
    void testExportAttendanceReport_NormalCase_Success() throws Exception {
        // Arrange
        String csvContent = "BadgeID,Name,Date,ClockIn,ClockOut,TotalHours,Status
" +
                            "EMP001,John Doe,2024-01-15,08:00,17:00,9.0,COMPLETED
";
        when(attendanceReportService.exportAttendanceReport(
                any(LocalDate.class), any(LocalDate.class), isNull()
        )).thenReturn(csvContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/export")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(csvContent));
    }

    @Test
    @WithMockUser(roles = "PAYROLL_ADMIN")
    @DisplayName("GET /attendance/export - Payroll Admin Role - Should Export CSV")
    void testExportAttendanceReport_PayrollAdminRole_Success() throws Exception {
        // Arrange
        String csvContent = "BadgeID,Name,Date,ClockIn,ClockOut,TotalHours,Status
";
        when(attendanceReportService.exportAttendanceReport(
                any(LocalDate.class), any(LocalDate.class), isNull()
        )).thenReturn(csvContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/export")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("GET /attendance/export - Worker Role - Should Return Forbidden")
    void testExportAttendanceReport_WorkerRole_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/export")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("GET /attendance/export - With Employee Filter - Should Export Filtered CSV")
    void testExportAttendanceReport_WithEmployeeFilter_Success() throws Exception {
        // Arrange
        String csvContent = "BadgeID,Name,Date,ClockIn,ClockOut,TotalHours,Status
" +
                            "EMP001,John Doe,2024-01-15,08:00,17:00,9.0,COMPLETED
";
        when(attendanceReportService.exportAttendanceReport(
                any(LocalDate.class), any(LocalDate.class), eq(testEmployeeId)
        )).thenReturn(csvContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/export")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .param("employeeId", testEmployeeId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(csvContent));
    }

    // ==================== ATTENDANCE SUMMARY TESTS ====================

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("GET /attendance/summary/{employeeId} - Normal Case - Should Return Summary")
    void testGetAttendanceSummary_NormalCase_Success() throws Exception {
        // Arrange
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDays", 20);
        summary.put("totalHours", 160.0);
        summary.put("overtimeHours", 10.0);
        summary.put("averageHoursPerDay", 8.0);

        when(attendanceReportService.getAttendanceSummary(
                eq(testEmployeeId), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/summary/{employeeId}", testEmployeeId)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDays").value(20))
                .andExpect(jsonPath("$.totalHours").value(160.0))
                .andExpect(jsonPath("$.overtimeHours").value(10.0));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("GET /attendance/summary/{employeeId} - Worker Own Summary - Should Return Summary")
    void testGetAttendanceSummary_WorkerOwnSummary_Success() throws Exception {
        // Arrange
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDays", 20);
        summary.put("totalHours", 160.0);

        when(attendanceReportService.getAttendanceSummary(
                eq(testEmployeeId), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/summary/{employeeId}", testEmployeeId)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk());
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/clock-in - Maximum Geolocation Values - Should Clock In Successfully")
    void testClockIn_MaxGeolocation_Success() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());
        request.put("deviceId", "DEVICE001");
        request.put("latitude", 90.0);  // Max latitude
        request.put("longitude", 180.0); // Max longitude

        when(attendanceService.clockIn(eq(testEmployeeId), any(ClockEventMetadata.class)))
                .thenReturn(testAttendanceRecord);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /attendance/clock-in - Minimum Geolocation Values - Should Clock In Successfully")
    void testClockIn_MinGeolocation_Success() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", testEmployeeId.toString());
        request.put("deviceId", "DEVICE001");
        request.put("latitude", -90.0);  // Min latitude
        request.put("longitude", -180.0); // Min longitude

        when(attendanceService.clockIn(eq(testEmployeeId), any(ClockEventMetadata.class)))
                .thenReturn(testAttendanceRecord);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("GET /attendance/export - Large Date Range - Should Export Successfully")
    void testExportAttendanceReport_LargeDateRange_Success() throws Exception {
        // Arrange
        String csvContent = "BadgeID,Name,Date,ClockIn,ClockOut,TotalHours,Status
";
        when(attendanceReportService.exportAttendanceReport(
                any(LocalDate.class), any(LocalDate.class), isNull()
        )).thenReturn(csvContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendance/export")
                .param("startDate", "2023-01-01")
                .param("endDate", "2024-12-31"))
                .andExpect(status().isOk());
    }
}