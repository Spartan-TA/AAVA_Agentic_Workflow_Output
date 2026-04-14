package com.wms.ems.service;

import com.wms.ems.dto.ClockInRequestDTO;
import com.wms.ems.dto.ClockOutRequestDTO;
import com.wms.ems.dto.AttendanceEventDTO;
import com.wms.ems.dto.AttendanceCorrectionDTO;
import com.wms.ems.entity.AttendanceEvent;
import com.wms.ems.entity.Employee;
import com.wms.ems.entity.ShiftAssignment;
import com.wms.ems.entity.enums.EventType;
import com.wms.ems.exception.EntityNotFoundException;
import com.wms.ems.exception.ValidationException;
import com.wms.ems.exception.ConflictException;
import com.wms.ems.repository.AttendanceEventRepository;
import com.wms.ems.repository.EmployeeRepository;
import com.wms.ems.repository.ShiftAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService.
 * Tests cover clock-in/out operations, daily hours calculation, corrections, and all edge cases.
 * 
 * @author EMS Test Suite Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Attendance Service Tests")
class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private ShiftAssignment testShift;
    private AttendanceEvent clockInEvent;
    private ClockInRequestDTO clockInRequest;
    private ClockOutRequestDTO clockOutRequest;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");

        // Setup test shift
        testShift = new ShiftAssignment();
        testShift.setId(1L);
        testShift.setEmployee(testEmployee);
        testShift.setShiftDate(LocalDate.now());
        testShift.setStartTime(LocalTime.of(8, 0));
        testShift.setEndTime(LocalTime.of(17, 0));

        // Setup clock-in event
        clockInEvent = new AttendanceEvent();
        clockInEvent.setId(1L);
        clockInEvent.setEmployee(testEmployee);
        clockInEvent.setEventType(EventType.CLOCK_IN);
        clockInEvent.setEventTime(LocalDateTime.now());
        clockInEvent.setDeviceId("DEVICE001");
        clockInEvent.setLatitude(40.7128);
        clockInEvent.setLongitude(-74.0060);

        // Setup clock-in request
        clockInRequest = ClockInRequestDTO.builder()
                .badgeId("EMP001")
                .deviceId("DEVICE001")
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();

        // Setup clock-out request
        clockOutRequest = ClockOutRequestDTO.builder()
                .badgeId("EMP001")
                .deviceId("DEVICE001")
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();
    }

    // ==================== CLOCK-IN TESTS ====================

    @Test
    @DisplayName("Clock In - Valid Request - Success")
    void testClockIn_ValidRequest_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(shiftAssignmentRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
        assertEquals(EventType.CLOCK_IN, result.getEventType());
        assertEquals("EMP001", result.getBadgeId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Clock In - Invalid BadgeId - Throws EntityNotFoundException")
    void testClockIn_InvalidBadgeId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Clock In - Null BadgeId - Throws ValidationException")
    void testClockIn_NullBadgeId_ThrowsValidationException() {
        // Arrange
        clockInRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Clock In - Empty BadgeId - Throws ValidationException")
    void testClockIn_EmptyBadgeId_ThrowsValidationException() {
        // Arrange
        clockInRequest.setBadgeId("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Clock In - Null DeviceId - Throws ValidationException")
    void testClockIn_NullDeviceId_ThrowsValidationException() {
        // Arrange
        clockInRequest.setDeviceId(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Clock In - Already Clocked In - Throws ConflictException")
    void testClockIn_AlreadyClockedIn_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(clockInEvent));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Clock In - No Assigned Shift - Throws ValidationException")
    void testClockIn_NoAssignedShift_ThrowsValidationException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(shiftAssignmentRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Clock In - Invalid Geofence Coordinates - Throws ValidationException")
    void testClockIn_InvalidGeofenceCoordinates_ThrowsValidationException() {
        // Arrange
        clockInRequest.setLatitude(91.0); // Invalid latitude
        clockInRequest.setLongitude(-74.0060);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Clock In - Outside Geofence - Throws ValidationException")
    void testClockIn_OutsideGeofence_ThrowsValidationException() {
        // Arrange
        clockInRequest.setLatitude(0.0);
        clockInRequest.setLongitude(0.0);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(shiftAssignmentRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(testShift));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Clock In - Optional Geofence - Success")
    void testClockIn_OptionalGeofence_Success() {
        // Arrange
        clockInRequest.setLatitude(null);
        clockInRequest.setLongitude(null);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(shiftAssignmentRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
    }

    // ==================== CLOCK-OUT TESTS ====================

    @Test
    @DisplayName("Clock Out - Valid Request - Success")
    void testClockOut_ValidRequest_Success() {
        // Arrange
        AttendanceEvent clockOutEvent = new AttendanceEvent();
        clockOutEvent.setId(2L);
        clockOutEvent.setEmployee(testEmployee);
        clockOutEvent.setEventType(EventType.CLOCK_OUT);
        clockOutEvent.setEventTime(LocalDateTime.now());

        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutRequest);

        // Assert
        assertNotNull(result);
        assertEquals(EventType.CLOCK_OUT, result.getEventType());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Clock Out - Not Clocked In - Throws ConflictException")
    void testClockOut_NotClockedIn_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
    }

    @Test
    @DisplayName("Clock Out - Already Clocked Out - Throws ConflictException")
    void testClockOut_AlreadyClockedOut_ThrowsConflictException() {
        // Arrange
        AttendanceEvent previousClockOut = new AttendanceEvent();
        previousClockOut.setEventType(EventType.CLOCK_OUT);
        previousClockOut.setEventTime(LocalDateTime.now().minusHours(1));

        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(previousClockOut));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
    }

    @Test
    @DisplayName("Clock Out - Null BadgeId - Throws ValidationException")
    void testClockOut_NullBadgeId_ThrowsValidationException() {
        // Arrange
        clockOutRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.clockOut(clockOutRequest);
        });
    }

    // ==================== DAILY HOURS CALCULATION TESTS ====================

    @Test
    @DisplayName("Calculate Daily Hours - Valid Events - Success")
    void testCalculateDailyHours_ValidEvents_Success() {
        // Arrange
        AttendanceEvent clockOut = new AttendanceEvent();
        clockOut.setEventType(EventType.CLOCK_OUT);
        clockOut.setEventTime(LocalDateTime.now().plusHours(8));

        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOut);
        when(attendanceEventRepository.findByEmployeeAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(events);

        // Act
        Double hours = attendanceService.calculateDailyHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertTrue(hours >= 7.9 && hours <= 8.1); // Allow small variance
    }

    @Test
    @DisplayName("Calculate Daily Hours - No Events - Returns Zero")
    void testCalculateDailyHours_NoEvents_ReturnsZero() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        Double hours = attendanceService.calculateDailyHours(1L, LocalDate.now());

        // Assert
        assertEquals(0.0, hours);
    }

    @Test
    @DisplayName("Calculate Daily Hours - Only Clock In - Returns Zero")
    void testCalculateDailyHours_OnlyClockIn_ReturnsZero() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(clockInEvent));

        // Act
        Double hours = attendanceService.calculateDailyHours(1L, LocalDate.now());

        // Assert
        assertEquals(0.0, hours);
    }

    @Test
    @DisplayName("Calculate Daily Hours - Multiple Clock In/Out Pairs - Success")
    void testCalculateDailyHours_MultiplePairs_Success() {
        // Arrange
        AttendanceEvent clockIn1 = new AttendanceEvent();
        clockIn1.setEventType(EventType.CLOCK_IN);
        clockIn1.setEventTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));

        AttendanceEvent clockOut1 = new AttendanceEvent();
        clockOut1.setEventType(EventType.CLOCK_OUT);
        clockOut1.setEventTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)));

        AttendanceEvent clockIn2 = new AttendanceEvent();
        clockIn2.setEventType(EventType.CLOCK_IN);
        clockIn2.setEventTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 0)));

        AttendanceEvent clockOut2 = new AttendanceEvent();
        clockOut2.setEventType(EventType.CLOCK_OUT);
        clockOut2.setEventTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)));

        List<AttendanceEvent> events = Arrays.asList(clockIn1, clockOut1, clockIn2, clockOut2);
        when(attendanceEventRepository.findByEmployeeAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(events);

        // Act
        Double hours = attendanceService.calculateDailyHours(1L, LocalDate.now());

        // Assert
        assertNotNull(hours);
        assertTrue(hours >= 7.9 && hours <= 8.1); // 4 hours + 4 hours
    }

    // ==================== MISSED PUNCH CORRECTION TESTS ====================

    @Test
    @DisplayName("Submit Correction - Valid Request - Success")
    void testSubmitCorrection_ValidRequest_Success() {
        // Arrange
        AttendanceCorrectionDTO correctionDTO = AttendanceCorrectionDTO.builder()
                .employeeId(1L)
                .eventDate(LocalDate.now())
                .eventType(EventType.CLOCK_OUT)
                .eventTime(LocalTime.of(17, 0))
                .reason("Forgot to clock out")
                .build();

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.submitCorrection(correctionDTO);

        // Assert
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    @DisplayName("Submit Correction - Null Reason - Throws ValidationException")
    void testSubmitCorrection_NullReason_ThrowsValidationException() {
        // Arrange
        AttendanceCorrectionDTO correctionDTO = AttendanceCorrectionDTO.builder()
                .employeeId(1L)
                .eventDate(LocalDate.now())
                .eventType(EventType.CLOCK_OUT)
                .eventTime(LocalTime.of(17, 0))
                .reason(null)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.submitCorrection(correctionDTO);
        });
    }

    @Test
    @DisplayName("Submit Correction - Future Date - Throws ValidationException")
    void testSubmitCorrection_FutureDate_ThrowsValidationException() {
        // Arrange
        AttendanceCorrectionDTO correctionDTO = AttendanceCorrectionDTO.builder()
                .employeeId(1L)
                .eventDate(LocalDate.now().plusDays(1))
                .eventType(EventType.CLOCK_OUT)
                .eventTime(LocalTime.of(17, 0))
                .reason("Test")
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            attendanceService.submitCorrection(correctionDTO);
        });
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Clock In - Midnight Shift - Success")
    void testClockIn_MidnightShift_Success() {
        // Arrange
        testShift.setStartTime(LocalTime.of(23, 0));
        testShift.setEndTime(LocalTime.of(7, 0));
        clockInRequest.setBadgeId("EMP001");

        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(shiftAssignmentRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Calculate Daily Hours - 24 Hour Shift - Success")
    void testCalculateDailyHours_24HourShift_Success() {
        // Arrange
        AttendanceEvent clockOut = new AttendanceEvent();
        clockOut.setEventType(EventType.CLOCK_OUT);
        clockOut.setEventTime(clockInEvent.getEventTime().plusHours(24));

        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOut);
        when(attendanceEventRepository.findByEmployeeAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(events);

        // Act
        Double hours = attendanceService.calculateDailyHours(1L, LocalDate.now());

        // Assert
        assertTrue(hours >= 23.9 && hours <= 24.1);
    }

    @Test
    @DisplayName("Clock In - Maximum Latitude - Success")
    void testClockIn_MaximumLatitude_Success() {
        // Arrange
        clockInRequest.setLatitude(90.0);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(shiftAssignmentRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Clock In - Minimum Longitude - Success")
    void testClockIn_MinimumLongitude_Success() {
        // Arrange
        clockInRequest.setLongitude(-180.0);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(shiftAssignmentRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Clock In - Same Second Multiple Attempts - Throws ConflictException")
    void testClockIn_SameSecondMultipleAttempts_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(clockInEvent));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockIn(clockInRequest);
        });
    }

    @Test
    @DisplayName("Calculate Daily Hours - Clock Out Before Clock In - Returns Zero")
    void testCalculateDailyHours_ClockOutBeforeClockIn_ReturnsZero() {
        // Arrange
        AttendanceEvent clockOut = new AttendanceEvent();
        clockOut.setEventType(EventType.CLOCK_OUT);
        clockOut.setEventTime(clockInEvent.getEventTime().minusHours(1));

        List<AttendanceEvent> events = Arrays.asList(clockOut, clockInEvent);
        when(attendanceEventRepository.findByEmployeeAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(events);

        // Act
        Double hours = attendanceService.calculateDailyHours(1L, LocalDate.now());

        // Assert
        assertEquals(0.0, hours);
    }

    @Test
    @DisplayName("Submit Correction - Very Old Date - Success")
    void testSubmitCorrection_VeryOldDate_Success() {
        // Arrange
        AttendanceCorrectionDTO correctionDTO = AttendanceCorrectionDTO.builder()
                .employeeId(1L)
                .eventDate(LocalDate.now().minusYears(1))
                .eventType(EventType.CLOCK_OUT)
                .eventTime(LocalTime.of(17, 0))
                .reason("Historical correction")
                .build();

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.submitCorrection(correctionDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Clock In - Device ID With Special Characters - Success")
    void testClockIn_DeviceIdWithSpecialCharacters_Success() {
        // Arrange
        clockInRequest.setDeviceId("DEVICE-001_TEST@WAREHOUSE");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(shiftAssignmentRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(testShift));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInRequest);

        // Assert
        assertNotNull(result);
    }
}