package com.example.ems.attendance;

import com.example.ems.attendance.dto.AttendanceDTO;
import com.example.ems.attendance.entity.AttendanceEntity;
import com.example.ems.attendance.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    private AttendanceDTO validAttendanceDTO;

    @BeforeEach
    void setUp() {
        validAttendanceDTO = new AttendanceDTO();
        validAttendanceDTO.setEmployeeId(1L);
        validAttendanceDTO.setClockInTime("2023-01-01T08:00:00");
        validAttendanceDTO.setClockOutTime("2023-01-01T17:00:00");
        validAttendanceDTO.setDate(LocalDate.of(2023, 1, 1));
    }

    @Test
    @DisplayName("POST /attendance/clock-in - Success")
    void testClockInSuccess() throws Exception {
        when(attendanceService.clockIn(any(AttendanceDTO.class))).thenReturn(validAttendanceDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"clockInTime":"2023-01-01T08:00:00","date":"2023-01-01"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(1));
    }

    @Test
    @DisplayName("POST /attendance/clock-in - Null Input")
    void testClockInNullInput() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /attendance/clock-in - Invalid Format")
    void testClockInInvalidFormat() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"abc"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /attendance/clock-out - Success")
    void testClockOutSuccess() throws Exception {
        when(attendanceService.clockOut(any(AttendanceDTO.class))).thenReturn(validAttendanceDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"clockOutTime":"2023-01-01T17:00:00","date":"2023-01-01"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(1));
    }

    @Test
    @DisplayName("GET /attendance/reports - Success")
    void testGetAttendanceReportsSuccess() throws Exception {
        when(attendanceService.getAttendanceReports(any(), any())).thenReturn(Collections.singletonList(validAttendanceDTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/attendance/reports")
                .param("startDate", "2023-01-01")
                .param("endDate", "2023-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(1));
    }

    @Test
    @DisplayName("GET /attendance/reports - Unauthorized")
    void testGetAttendanceReportsUnauthorized() throws Exception {
        // Simulate unauthorized by not setting authentication
        mockMvc.perform(MockMvcRequestBuilders.get("/attendance/reports"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /attendance/reports - Forbidden")
    void testGetAttendanceReportsForbidden() throws Exception {
        // Simulate forbidden by mocking security context if needed
        // For template, just show forbidden status
        mockMvc.perform(MockMvcRequestBuilders.get("/attendance/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /attendance/clock-in - Duplicate Entry")
    void testClockInDuplicateEntry() throws Exception {
        when(attendanceService.clockIn(any(AttendanceDTO.class))).thenThrow(new IllegalStateException("Duplicate clock-in"));
        mockMvc.perform(MockMvcRequestBuilders.post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"clockInTime":"2023-01-01T08:00:00","date":"2023-01-01"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /attendance/reports - Not Found")
    void testGetAttendanceReportsNotFound() throws Exception {
        when(attendanceService.getAttendanceReports(any(), any())).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.get("/attendance/reports")
                .param("startDate", "2023-01-01")
                .param("endDate", "2023-01-31"))
                .andExpect(status().isNotFound());
    }
}
