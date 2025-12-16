package com.companyname.wems.attendance.controller;

import com.companyname.wems.attendance.service.AttendanceService;
import com.companyname.wems.attendance.dto.ClockInRequest;
import com.companyname.wems.attendance.dto.AttendanceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Autowired
    private ObjectMapper objectMapper;

    private AttendanceDTO testAttendanceDTO;
    private ClockInRequest validClockInRequest;

    @BeforeEach
    void setUp() {
        testAttendanceDTO = new AttendanceDTO();
        testAttendanceDTO.setId(1L);
        testAttendanceDTO.setEmployeeId(100L);
        testAttendanceDTO.setClockIn("2023-06-01T08:00:00");
        testAttendanceDTO.setClockOut("2023-06-01T16:00:00");

        validClockInRequest = new ClockInRequest();
        validClockInRequest.setEmployeeId(100L);
        validClockInRequest.setClockIn("2023-06-01T08:00:00");
    }

    @Test
    void testClockIn_ValidInput_Returns201() throws Exception {
        when(attendanceService.clockIn(any(ClockInRequest.class))).thenReturn(testAttendanceDTO);
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockInRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(100));
    }

    @Test
    void testClockIn_InvalidInput_Returns400() throws Exception {
        validClockInRequest.setClockIn("");
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockInRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testClockOut_ValidInput_Returns200() throws Exception {
        when(attendanceService.clockOut(any(Long.class), any(String.class))).thenReturn(testAttendanceDTO);
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":100,"clockOut":"2023-06-01T16:00:00"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOut").value("2023-06-01T16:00:00"));
    }

    @Test
    void testClockOut_InvalidInput_Returns400() throws Exception {
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":null,"clockOut":""}"))
                .andExpect(status().isBadRequest());
    }
}
