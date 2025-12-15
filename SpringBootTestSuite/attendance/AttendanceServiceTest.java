package com.example.ems.attendance;

import com.example.ems.attendance.dto.AttendanceDTO;
import com.example.ems.attendance.entity.AttendanceEntity;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceDTO validAttendanceDTO;
    private AttendanceEntity validAttendanceEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validAttendanceDTO = new AttendanceDTO();
        validAttendanceDTO.setEmployeeId(1L);
        validAttendanceDTO.setClockInTime("2023-01-01T08:00:00");
        validAttendanceDTO.setClockOutTime("2023-01-01T17:00:00");
        validAttendanceDTO.setDate(LocalDate.of(2023, 1, 1));

        validAttendanceEntity = new AttendanceEntity();
        validAttendanceEntity.setId(1L);
        validAttendanceEntity.setEmployeeId(1L);
        validAttendanceEntity.setClockInTime("2023-01-01T08:00:00");
        validAttendanceEntity.setClockOutTime("2023-01-01T17:00:00");
        validAttendanceEntity.setDate(LocalDate.of(2023, 1, 1));
    }

    @Test
    @DisplayName("calculateHours - Normal Case")
    void testCalculateHoursNormal() {
        double hours = attendanceService.calculateHours("08:00", "17:00");
        assertEquals(9.0, hours);
    }

    @Test
    @DisplayName("calculateHours - Null Input")
    void testCalculateHoursNullInput() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.calculateHours(null, "17:00"));
        assertThrows(IllegalArgumentException.class, () -> attendanceService.calculateHours("08:00", null));
    }

    @Test
    @DisplayName("calculateHours - Invalid Format")
    void testCalculateHoursInvalidFormat() {
        assertThrows(NumberFormatException.class, () -> attendanceService.calculateHours("abc", "17:00"));
    }

    @Test
    @DisplayName("handleMissedPunch - Normal Case")
    void testHandleMissedPunchNormal() {
        when(attendanceRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(Optional.of(validAttendanceEntity));
        assertDoesNotThrow(() -> attendanceService.handleMissedPunch(1L, LocalDate.of(2023, 1, 1)));
    }

    @Test
    @DisplayName("handleMissedPunch - Not Found")
    void testHandleMissedPunchNotFound() {
        when(attendanceRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> attendanceService.handleMissedPunch(1L, LocalDate.of(2023, 1, 1)));
    }

    @Test
    @DisplayName("validateGeofence - Inside Geofence")
    void testValidateGeofenceInside() {
        boolean result = attendanceService.validateGeofence(40.7128, -74.0060);
        assertTrue(result);
    }

    @Test
    @DisplayName("validateGeofence - Outside Geofence")
    void testValidateGeofenceOutside() {
        boolean result = attendanceService.validateGeofence(0.0, 0.0);
        assertFalse(result);
    }

    @Test
    @DisplayName("validateGeofence - Null Input")
    void testValidateGeofenceNullInput() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.validateGeofence(null, -74.0060));
        assertThrows(IllegalArgumentException.class, () -> attendanceService.validateGeofence(40.7128, null));
    }
}
