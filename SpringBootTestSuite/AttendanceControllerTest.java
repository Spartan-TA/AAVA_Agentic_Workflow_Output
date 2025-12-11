package com.wms.employee.controller;

import com.wms.employee.entity.Attendance;
import com.wms.employee.request.AttendanceRequest;
import com.wms.employee.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    private Attendance attendance;
    private AttendanceRequest attendanceRequest;

    @BeforeEach
    void setUp() {
        attendance = new Attendance();
        attendanceRequest = new AttendanceRequest(1L, "D1", "Main Gate", "Morning");
    }

    @Test
    void testClockIn_ValidRequest_Returns200() throws Exception {
        when(attendanceService.clockIn(any())).thenReturn(attendance);
        mockMvc.perform(post("/api/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"deviceId":"D1","location":"Main Gate","shift":"Morning"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testClockIn_InvalidEmployeeId_Returns404() throws Exception {
        when(attendanceService.clockIn(any())).thenThrow(new com.wms.employee.exception.ResourceNotFoundException("Not found"));
        mockMvc.perform(post("/api/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":99,"deviceId":"D1","location":"Main Gate","shift":"Morning"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testClockIn_NullDeviceId_Returns400() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"deviceId":null,"location":"Main Gate","shift":"Morning"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testClockOut_ValidAttendanceId_Returns200() throws Exception {
        when(attendanceService.clockOut(1L)).thenReturn(attendance);
        mockMvc.perform(post("/api/attendance/clock-out/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testClockOut_NonExistentAttendanceId_Returns404() throws Exception {
        when(attendanceService.clockOut(99L)).thenThrow(new com.wms.employee.exception.ResourceNotFoundException("Not found"));
        mockMvc.perform(post("/api/attendance/clock-out/99"))
                .andExpect(status().isNotFound());
    }
}