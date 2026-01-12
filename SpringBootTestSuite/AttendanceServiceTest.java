package com.warehouse.ems.service;

import com.warehouse.ems.entity.AttendanceEvent;
import com.warehouse.ems.repository.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceEvent event;

    @BeforeEach
    void setUp() {
        event = new AttendanceEvent(1L, 1L, "CLOCK_IN", LocalDateTime.now(), "D1", "Main Gate", 1L, "NONE");
    }

    @Test
    void testClockIn_ValidInput_ReturnsEvent() {
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(event);

        AttendanceEvent result = attendanceService.clockIn(1L, "D1", "Main Gate", 1L);

        assertNotNull(result);
        assertEquals("CLOCK_IN", result.getType());
        verify(attendanceRepository).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_NullEmployeeId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null, "D1", "Main Gate", 1L));
    }

    @Test
    void testClockOut_ValidInput_ReturnsEvent() {
        AttendanceEvent clockOutEvent = new AttendanceEvent(2L, 1L, "CLOCK_OUT", LocalDateTime.now(), "D1", "Main Gate", 1L, "NONE");
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        AttendanceEvent result = attendanceService.clockOut(1L, "D1", "Main Gate", 1L);

        assertNotNull(result);
        assertEquals("CLOCK_OUT", result.getType());
    }

    @Test
    void testClockOut_InvalidShiftId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockOut(1L, "D1", "Main Gate", null));
    }

    @Test
    void testGetAttendanceEventsByEmployeeId_ReturnsList() {
        List<AttendanceEvent> events = Arrays.asList(event);
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(events);

        List<AttendanceEvent> result = attendanceService.getAttendanceEventsByEmployeeId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetAttendanceEventsByEmployeeId_EmptyList() {
        when(attendanceRepository.findByEmployeeId(2L)).thenReturn(Collections.emptyList());

        List<AttendanceEvent> result = attendanceService.getAttendanceEventsByEmployeeId(2L);

        assertTrue(result.isEmpty());
    }
}