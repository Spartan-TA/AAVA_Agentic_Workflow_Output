package com.example.warehouse.controller;

import com.example.warehouse.entity.AttendanceEvent;
import com.example.warehouse.enums.EventType;
import com.example.warehouse.service.ClockInOutService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AttendanceControllerTest {

    @Mock private ClockInOutService clockInOutService;
    @InjectMocks private AttendanceController attendanceController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController).build();
    }

    @Test
    void clockIn_ShouldReturnAttendanceEvent() throws Exception {
        AttendanceEvent event = new AttendanceEvent();
        event.setId(1L);
        event.setEmployeeId(1L);
        event.setEventType(EventType.CLOCK_IN);
        event.setEventTimestamp(LocalDateTime.now());

        when(clockInOutService.clockIn(anyLong(), any(), any())).thenReturn(event);

        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"location":"Main Gate","deviceId":"DEV001"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("CLOCK_IN"));
    }

    @Test
    void clockOut_ShouldReturnAttendanceEvent() throws Exception {
        AttendanceEvent event = new AttendanceEvent();
        event.setId(1L);
        event.setEmployeeId(1L);
        event.setEventType(EventType.CLOCK_OUT);
        event.setEventTimestamp(LocalDateTime.now());

        when(clockInOutService.clockOut(anyLong(), any(), any())).thenReturn(event);

        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"location":"Main Gate","deviceId":"DEV001"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("CLOCK_OUT"));
    }

    @Test
    void clockIn_WithMissingEmployeeId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"location":"Main Gate","deviceId":"DEV001"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clockIn_WithNullLocation_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"location":null,"deviceId":"DEV001"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clockOut_WithMissingDeviceId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"location":"Main Gate"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clockIn_WithNonExistingEmployee_ShouldReturnNotFound() throws Exception {
        when(clockInOutService.clockIn(anyLong(), any(), any())).thenThrow(new ResourceNotFoundException("Employee not found"));

        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":99,"location":"Main Gate","deviceId":"DEV001"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void clockOut_WithNonExistingEmployee_ShouldReturnNotFound() throws Exception {
        when(clockInOutService.clockOut(anyLong(), any(), any())).thenThrow(new ResourceNotFoundException("Employee not found"));

        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":99,"location":"Main Gate","deviceId":"DEV001"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void clockIn_WithEmptyLocation_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"location":"","deviceId":"DEV001"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clockOut_WithEmptyDeviceId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"location":"Main Gate","deviceId":""}"))
                .andExpect(status().isBadRequest());
    }
}