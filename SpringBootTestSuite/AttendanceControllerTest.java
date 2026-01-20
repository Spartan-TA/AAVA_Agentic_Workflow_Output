package com.company.warehouse.attendance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for AttendanceController
 * Tests all attendance REST endpoints
 */
@WebMvcTest(AttendanceController.class)
@DisplayName("Attendance Controller Integration Tests")
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AttendanceService attendanceService;

    private ClockEventRequest validClockRequest;
    private ClockEvent validClockEvent;

    @BeforeEach
    public void setUp() {
        validClockRequest = new ClockEventRequest();
        validClockRequest.setEmployeeId(1L);
        validClockRequest.setLatitude(40.7128);
        validClockRequest.setLongitude(-74.0060);
        validClockRequest.setDeviceId("DEVICE123");

        validClockEvent = new ClockEvent();
        validClockEvent.setId(1L);
        validClockEvent.setEmployeeId(1L);
        validClockEvent.setType(ClockEventType.CLOCK_IN);
        validClockEvent.setLatitude(40.7128);
        validClockEvent.setLongitude(-74.0060);
        validClockEvent.setDeviceId("DEVICE123");
        validClockEvent.setTimestamp(LocalDateTime.now());
    }

    // ========== CLOCK-IN ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test POST /attendance/clock-in with valid data returns 201 Created")
    public void testClockInWithValidData() throws Exception {
        // Arrange
        when(attendanceService.clockIn(anyLong(), anyDouble(), anyDouble(), anyString()))
            .thenReturn(validClockEvent);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.type").value("CLOCK_IN"))
                .andExpect(jsonPath("$.latitude").value(40.7128))
                .andExpect(jsonPath("$.longitude").value(-74.0060))
                .andExpect(jsonPath("$.deviceId").value("DEVICE123"));

        verify(attendanceService, times(1)).clockIn(1L, 40.7128, -74.0060, "DEVICE123");
    }

    @Test
    @DisplayName("Test POST /attendance/clock-in with null geolocation")
    public void testClockInWithNullGeolocation() throws Exception {
        // Arrange
        validClockRequest.setLatitude(null);
        validClockRequest.setLongitude(null);
        validClockEvent.setLatitude(null);
        validClockEvent.setLongitude(null);

        when(attendanceService.clockIn(anyLong(), isNull(), isNull(), anyString()))
            .thenReturn(validClockEvent);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(1));

        verify(attendanceService, times(1)).clockIn(eq(1L), isNull(), isNull(), eq("DEVICE123"));
    }

    @Test
    @DisplayName("Test POST /attendance/clock-in with null device ID")
    public void testClockInWithNullDeviceId() throws Exception {
        // Arrange
        validClockRequest.setDeviceId(null);
        validClockEvent.setDeviceId(null);

        when(attendanceService.clockIn(anyLong(), anyDouble(), anyDouble(), isNull()))
            .thenReturn(validClockEvent);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockRequest)))
                .andExpect(status().isCreated());

        verify(attendanceService, times(1)).clockIn(eq(1L), anyDouble(), anyDouble(), isNull());
    }

    @Test
    @DisplayName("Test POST /attendance/clock-in with missing employee ID returns 400")
    public void testClockInWithMissingEmployeeId() throws Exception {
        // Arrange
        validClockRequest.setEmployeeId(null);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockRequest)))
                .andExpect(status().isBadRequest());

        verify(attendanceService, never()).clockIn(anyLong(), anyDouble(), anyDouble(), anyString());
    }

    @Test
    @DisplayName("Test POST /attendance/clock-in with empty request body returns 400")
    public void testClockInWithEmptyBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(attendanceService, never()).clockIn(anyLong(), anyDouble(), anyDouble(), anyString());
    }

    @Test
    @DisplayName("Test POST /attendance/clock-in with boundary latitude values")
    public void testClockInWithBoundaryLatitude() throws Exception {
        // Arrange - Test minimum latitude
        validClockRequest.setLatitude(-90.0);
        when(attendanceService.clockIn(anyLong(), eq(-90.0), anyDouble(), anyString()))
            .thenReturn(validClockEvent);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockRequest)))
                .andExpect(status().isCreated());

        // Arrange - Test maximum latitude
        validClockRequest.setLatitude(90.0);
        when(attendanceService.clockIn(anyLong(), eq(90.0), anyDouble(), anyString()))
            .thenReturn(validClockEvent);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockRequest)))
                .andExpect(status().isCreated());

        verify(attendanceService, times(2)).clockIn(anyLong(), anyDouble(), anyDouble(), anyString());
    }

    // ========== CLOCK-OUT ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test POST /attendance/clock-out with valid data returns 201 Created")
    public void testClockOutWithValidData() throws Exception {
        // Arrange
        validClockEvent.setType(ClockEventType.CLOCK_OUT);
        when(attendanceService.clockOut(anyLong(), anyDouble(), anyDouble(), anyString()))
            .thenReturn(validClockEvent);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CLOCK_OUT"))
                .andExpect(jsonPath("$.employeeId").value(1));

        verify(attendanceService, times(1)).clockOut(1L, 40.7128, -74.0060, "DEVICE123");
    }

    @Test
    @DisplayName("Test POST /attendance/clock-out with null geolocation")
    public void testClockOutWithNullGeolocation() throws Exception {
        // Arrange
        validClockRequest.setLatitude(null);
        validClockRequest.setLongitude(null);
        validClockEvent.setType(ClockEventType.CLOCK_OUT);

        when(attendanceService.clockOut(anyLong(), isNull(), isNull(), anyString()))
            .thenReturn(validClockEvent);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockRequest)))
                .andExpect(status().isCreated());

        verify(attendanceService, times(1)).clockOut(eq(1L), isNull(), isNull(), anyString());
    }

    @Test
    @DisplayName("Test POST /attendance/clock-out without prior clock-in")
    public void testClockOutWithoutClockIn() throws Exception {
        // Arrange
        validClockEvent.setType(ClockEventType.CLOCK_OUT);
        when(attendanceService.clockOut(anyLong(), anyDouble(), anyDouble(), anyString()))
            .thenReturn(validClockEvent);

        // Act & Assert
        mockMvc.perform(post("/attendance/clock-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validClockRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CLOCK_OUT"));

        verify(attendanceService, times(1)).clockOut(anyLong(), anyDouble(), anyDouble(), anyString());
    }

    // ========== GET HOURS WORKED TESTS ==========

    @Test
    @DisplayName("Test GET /attendance/hours/{employeeId} with valid data returns hours worked")
    public void testGetHoursWorkedWithValidData() throws Exception {
        // Arrange
        Duration duration = Duration.ofHours(8);
        when(attendanceService.calculateHoursWorked(eq(1L), any(LocalDate.class)))
            .thenReturn(duration);

        // Act & Assert
        mockMvc.perform(get("/attendance/hours/1")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.hoursWorked").value(8))
                .andExpect(jsonPath("$.minutesWorked").value(0));

        verify(attendanceService, times(1)).calculateHoursWorked(eq(1L), any(LocalDate.class));
    }

    @Test
    @DisplayName("Test GET /attendance/hours/{employeeId} with partial hours")
    public void testGetHoursWorkedWithPartialHours() throws Exception {
        // Arrange
        Duration duration = Duration.ofMinutes(450); // 7 hours 30 minutes
        when(attendanceService.calculateHoursWorked(eq(1L), any(LocalDate.class)))
            .thenReturn(duration);

        // Act & Assert
        mockMvc.perform(get("/attendance/hours/1")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hoursWorked").value(7))
                .andExpect(jsonPath("$.minutesWorked").value(30));

        verify(attendanceService, times(1)).calculateHoursWorked(eq(1L), any(LocalDate.class));
    }

    @Test
    @DisplayName("Test GET /attendance/hours/{employeeId} with zero hours")
    public void testGetHoursWorkedWithZeroHours() throws Exception {
        // Arrange
        Duration duration = Duration.ZERO;
        when(attendanceService.calculateHoursWorked(eq(1L), any(LocalDate.class)))
            .thenReturn(duration);

        // Act & Assert
        mockMvc.perform(get("/attendance/hours/1")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hoursWorked").value(0))
                .andExpect(jsonPath("$.minutesWorked").value(0));

        verify(attendanceService, times(1)).calculateHoursWorked(eq(1L), any(LocalDate.class));
    }

    @Test
    @DisplayName("Test GET /attendance/hours/{employeeId} with missing date parameter returns 400")
    public void testGetHoursWorkedWithMissingDate() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/attendance/hours/1"))
                .andExpect(status().isBadRequest());

        verify(attendanceService, never()).calculateHoursWorked(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("Test GET /attendance/hours/{employeeId} with invalid date format returns 400")
    public void testGetHoursWorkedWithInvalidDateFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/attendance/hours/1")
                .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());

        verify(attendanceService, never()).calculateHoursWorked(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("Test GET /attendance/hours/{employeeId} with future date")
    public void testGetHoursWorkedWithFutureDate() throws Exception {
        // Arrange
        Duration duration = Duration.ZERO;
        when(attendanceService.calculateHoursWorked(eq(1L), any(LocalDate.class)))
            .thenReturn(duration);

        // Act & Assert
        mockMvc.perform(get("/attendance/hours/1")
                .param("date", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hoursWorked").value(0));

        verify(attendanceService, times(1)).calculateHoursWorked(eq(1L), any(LocalDate.class));
    }

    // ========== GET CLOCK EVENTS TESTS ==========

    @Test
    @DisplayName("Test GET /attendance/events/{employeeId} returns list of events")
    public void testGetClockEventsWithValidEmployeeId() throws Exception {
        // Arrange
        ClockEvent event1 = new ClockEvent();
        event1.setId(1L);
        event1.setEmployeeId(1L);
        event1.setType(ClockEventType.CLOCK_IN);

        ClockEvent event2 = new ClockEvent();
        event2.setId(2L);
        event2.setEmployeeId(1L);
        event2.setType(ClockEventType.CLOCK_OUT);

        List<ClockEvent> events = Arrays.asList(event2, event1);
        when(attendanceService.getEmployeeClockEvents(1L)).thenReturn(events);

        // Act & Assert
        mockMvc.perform(get("/attendance/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].type").value("CLOCK_OUT"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].type").value("CLOCK_IN"));

        verify(attendanceService, times(1)).getEmployeeClockEvents(1L);
    }

    @Test
    @DisplayName("Test GET /attendance/events/{employeeId} with no events returns empty array")
    public void testGetClockEventsWithNoEvents() throws Exception {
        // Arrange
        when(attendanceService.getEmployeeClockEvents(1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/attendance/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(attendanceService, times(1)).getEmployeeClockEvents(1L);
    }

    @Test
    @DisplayName("Test GET /attendance/events/{employeeId} with non-existent employee")
    public void testGetClockEventsWithNonExistentEmployee() throws Exception {
        // Arrange
        when(attendanceService.getEmployeeClockEvents(999L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/attendance/events/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(attendanceService, times(1)).getEmployeeClockEvents(999L);
    }

    @Test
    @DisplayName("Test GET /attendance/events/{employeeId} with negative employee ID")
    public void testGetClockEventsWithNegativeEmployeeId() throws Exception {
        // Arrange
        when(attendanceService.getEmployeeClockEvents(-1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/attendance/events/-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(attendanceService, times(1)).getEmployeeClockEvents(-1L);
    }

    @Test
    @DisplayName("Test GET /attendance/events/{employeeId} with zero employee ID")
    public void testGetClockEventsWithZeroEmployeeId() throws Exception {
        // Arrange
        when(attendanceService.getEmployeeClockEvents(0L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/attendance/events/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(attendanceService, times(1)).getEmployeeClockEvents(0L);
    }
}