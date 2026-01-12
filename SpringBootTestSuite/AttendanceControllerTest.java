package com.warehouse.ems.controller;

import com.warehouse.ems.entity.AttendanceEvent;
import com.warehouse.ems.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    private AttendanceEvent event;

    @BeforeEach
    void setUp() {
        event = new AttendanceEvent(1L, 1L, "CLOCK_IN", LocalDateTime.now(), "D1", "Main Gate", 1L, "NONE");
    }

    @Test
    void testClockIn_ValidInput_ReturnsCreated() {
        when(attendanceService.clockIn(1L, "D1", "Main Gate", 1L)).thenReturn(event);

        ResponseEntity<AttendanceEvent> response = attendanceController.clockIn(1L, "D1", "Main Gate", 1L);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(event, response.getBody());
    }

    @Test
    void testClockIn_NullEmployeeId_ReturnsBadRequest() {
        ResponseEntity<AttendanceEvent> response = attendanceController.clockIn(null, "D1", "Main Gate", 1L);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testClockOut_ValidInput_ReturnsCreated() {
        AttendanceEvent clockOutEvent = new AttendanceEvent(2L, 1L, "CLOCK_OUT", LocalDateTime.now(), "D1", "Main Gate", 1L, "NONE");
        when(attendanceService.clockOut(1L, "D1", "Main Gate", 1L)).thenReturn(clockOutEvent);

        ResponseEntity<AttendanceEvent> response = attendanceController.clockOut(1L, "D1", "Main Gate", 1L);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(clockOutEvent, response.getBody());
    }

    @Test
    void testClockOut_InvalidShiftId_ReturnsBadRequest() {
        ResponseEntity<AttendanceEvent> response = attendanceController.clockOut(1L, "D1", "Main Gate", null);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testGetAttendanceEventsByEmployeeId_ReturnsList() {
        List<AttendanceEvent> events = Arrays.asList(event);
        when(attendanceService.getAttendanceEventsByEmployeeId(1L)).thenReturn(events);

        ResponseEntity<List<AttendanceEvent>> response = attendanceController.getAttendanceEventsByEmployeeId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAttendanceEventsByEmployeeId_EmptyList_ReturnsOk() {
        when(attendanceService.getAttendanceEventsByEmployeeId(2L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<AttendanceEvent>> response = attendanceController.getAttendanceEventsByEmployeeId(2L);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }
}