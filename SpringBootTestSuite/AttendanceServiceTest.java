package com.company.wems.service;

import com.company.wems.dto.request.AttendanceRequestDTO;
import com.company.wems.dto.response.AttendanceResponseDTO;
import com.company.wems.entity.AttendanceEvent;
import com.company.wems.entity.DailyAttendanceSummary;
import com.company.wems.entity.Employee;
import com.company.wems.exception.BadRequestException;
import com.company.wems.exception.ConflictException;
import com.company.wems.exception.ResourceNotFoundException;
import com.company.wems.repository.AttendanceEventRepository;
import com.company.wems.repository.DailyAttendanceSummaryRepository;
import com.company.wems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Tests clock in/out functionality, hours calculation, geofence validation, and missed punch handling
 * 
 * @author WEMS Test Suite Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private DailyAttendanceSummaryRepository dailyAttendanceSummaryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;
    private AttendanceRequestDTO validClockInRequest;
    private AttendanceRequestDTO validClockOutRequest;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");

        // Setup clock in event
        clockInEvent = new AttendanceEvent();
        clockInEvent.setId(1L);
        clockInEvent.setEmployeeId(1L);
        clockInEvent.setEventType("CLOCK_IN");
        clockInEvent.setEventTime(LocalDateTime.of(2024, 1, 15, 8, 0));
        clockInEvent.setLatitude(new BigDecimal("37.7749"));
        clockInEvent.setLongitude(new BigDecimal("-122.4194"));

        // Setup clock out event
        clockOutEvent = new AttendanceEvent();
        clockOutEvent.setId(2L);
        clockOutEvent.setEmployeeId(1L);
        clockOutEvent.setEventType("CLOCK_OUT");
        clockOutEvent.setEventTime(LocalDateTime.of(2024, 1, 15, 17, 0));
        clockOutEvent.setLatitude(new BigDecimal("37.7749"));
        clockOutEvent.setLongitude(new BigDecimal("-122.4194"));

        // Setup valid clock in request
        validClockInRequest = new AttendanceRequestDTO();
        validClockInRequest.setEmployeeId(1L);
        validClockInRequest.setEventType("CLOCK_IN");
        validClockInRequest.setEventTime(LocalDateTime.now());
        validClockInRequest.setLatitude(new BigDecimal("37.7749"));
        validClockInRequest.setLongitude(new BigDecimal("-122.4194"));

        // Setup valid clock out request
        validClockOutRequest = new AttendanceRequestDTO();
        validClockOutRequest.setEmployeeId(1L);
        validClockOutRequest.setEventType("CLOCK_OUT");
        validClockOutRequest.setEventTime(LocalDateTime.now());
        validClockOutRequest.setLatitude(new BigDecimal("37.7749"));
        validClockOutRequest.setLongitude(new BigDecimal("-122.4194"));
    }

    // ==================== CLOCK IN TESTS ====================

    @Test
    void testClockIn_WithValidData_ReturnsClockInEvent() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeId(anyLong())).thenReturn(Optional.empty());
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceResponseDTO result = attendanceService.clockIn(validClockInRequest);

        // Assert
        assertNotNull(result);
        assertEquals("CLOCK_IN", result.getEventType());
        assertEquals(1L, result.getEmployeeId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithNullEmployeeId_ThrowsBadRequestException() {
        // Arrange
        validClockInRequest.setEmployeeId(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(validClockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithNonExistentEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(validClockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WhenAlreadyClockedIn_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeId(anyLong())).thenReturn(Optional.of(clockInEvent));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockIn(validClockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithNullLatitude_ThrowsBadRequestException() {
        // Arrange
        validClockInRequest.setLatitude(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(validClockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithNullLongitude_ThrowsBadRequestException() {
        // Arrange
        validClockInRequest.setLongitude(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(validClockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithInvalidLatitude_ThrowsBadRequestException() {
        // Arrange
        validClockInRequest.setLatitude(new BigDecimal("91.0")); // Latitude must be between -90 and 90

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(validClockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithInvalidLongitude_ThrowsBadRequestException() {
        // Arrange
        validClockInRequest.setLongitude(new BigDecimal("181.0")); // Longitude must be between -180 and 180

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(validClockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_OutsideGeofence_ThrowsBadRequestException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        validClockInRequest.setLatitude(new BigDecimal("40.7128")); // New York coordinates (outside geofence)
        validClockInRequest.setLongitude(new BigDecimal("-74.0060"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(validClockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockIn_WithFutureTimestamp_ThrowsBadRequestException() {
        // Arrange
        validClockInRequest.setEventTime(LocalDateTime.now().plusHours(1));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockIn(validClockInRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    // ==================== CLOCK OUT TESTS ====================

    @Test
    void testClockOut_WithValidData_ReturnsClockOutEvent() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeId(anyLong())).thenReturn(Optional.of(clockInEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceResponseDTO result = attendanceService.clockOut(validClockOutRequest);

        // Assert
        assertNotNull(result);
        assertEquals("CLOCK_OUT", result.getEventType());
        assertEquals(1L, result.getEmployeeId());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_WithoutClockIn_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeId(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockOut(validClockOutRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_WhenAlreadyClockedOut_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeId(anyLong())).thenReturn(Optional.of(clockOutEvent));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            attendanceService.clockOut(validClockOutRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testClockOut_BeforeClockIn_ThrowsBadRequestException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findLatestEventByEmployeeId(anyLong())).thenReturn(Optional.of(clockInEvent));
        validClockOutRequest.setEventTime(clockInEvent.getEventTime().minusHours(1));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.clockOut(validClockOutRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    // ==================== HOURS CALCULATION TESTS ====================

    @Test
    void testCalculateHours_WithStandardShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 17, 0);

        // Act
        BigDecimal hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(new BigDecimal("9.00"), hours);
    }

    @Test
    void testCalculateHours_WithOvertimeShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 20, 0);

        // Act
        BigDecimal hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(new BigDecimal("12.00"), hours);
    }

    @Test
    void testCalculateHours_WithPartialHours_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 8, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 12, 30);

        // Act
        BigDecimal hours = attendanceService.calculateHours(clockIn, clockOut);

        // Assert
        assertEquals(new BigDecimal("4.50"), hours);
    }

    @Test
    void testCalculateHours_WithNullClockIn_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.calculateHours(null, LocalDateTime.now());
        });
    }

    @Test
    void testCalculateHours_WithNullClockOut_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.calculateHours(LocalDateTime.now(), null);
        });
    }

    @Test
    void testCalculateOvertimeHours_WithStandardShift_ReturnsZero() {
        // Arrange
        BigDecimal totalHours = new BigDecimal("8.00");

        // Act
        BigDecimal overtimeHours = attendanceService.calculateOvertimeHours(totalHours);

        // Assert
        assertEquals(BigDecimal.ZERO, overtimeHours);
    }

    @Test
    void testCalculateOvertimeHours_WithOvertimeShift_ReturnsCorrectOvertime() {
        // Arrange
        BigDecimal totalHours = new BigDecimal("10.00");

        // Act
        BigDecimal overtimeHours = attendanceService.calculateOvertimeHours(totalHours);

        // Assert
        assertEquals(new BigDecimal("2.00"), overtimeHours);
    }

    // ==================== DAILY SUMMARY TESTS ====================

    @Test
    void testGenerateDailySummary_WithCompleteShift_CreatesSummary() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class)))
            .thenReturn(Arrays.asList(clockInEvent, clockOutEvent));
        when(dailyAttendanceSummaryRepository.save(any(DailyAttendanceSummary.class)))
            .thenReturn(new DailyAttendanceSummary());

        // Act
        attendanceService.generateDailySummary(1L, LocalDate.of(2024, 1, 15));

        // Assert
        verify(dailyAttendanceSummaryRepository, times(1)).save(any(DailyAttendanceSummary.class));
    }

    @Test
    void testGenerateDailySummary_WithMissedPunch_MarksMissedPunch() {
        // Arrange
        when(attendanceEventRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class)))
            .thenReturn(Arrays.asList(clockInEvent)); // Only clock in, no clock out
        when(dailyAttendanceSummaryRepository.save(any(DailyAttendanceSummary.class)))
            .thenReturn(new DailyAttendanceSummary());

        // Act
        attendanceService.generateDailySummary(1L, LocalDate.of(2024, 1, 15));

        // Assert
        verify(dailyAttendanceSummaryRepository, times(1)).save(any(DailyAttendanceSummary.class));
    }

    @Test
    void testGetAttendanceHistory_WithValidEmployeeId_ReturnsHistory() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceEventRepository.findByEmployeeIdOrderByEventTimeDesc(anyLong())).thenReturn(events);

        // Act
        List<AttendanceResponseDTO> result = attendanceService.getAttendanceHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetAttendanceHistory_WithNullEmployeeId_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.getAttendanceHistory(null);
        });
    }

    // ==================== MISSED PUNCH CORRECTION TESTS ====================

    @Test
    void testCorrectMissedPunch_WithValidData_CreatesCorrection() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        AttendanceRequestDTO correctionRequest = new AttendanceRequestDTO();
        correctionRequest.setEmployeeId(1L);
        correctionRequest.setEventType("CLOCK_OUT");
        correctionRequest.setEventTime(LocalDateTime.of(2024, 1, 15, 17, 0));
        correctionRequest.setNotes("Missed punch correction");

        // Act
        AttendanceResponseDTO result = attendanceService.correctMissedPunch(correctionRequest);

        // Assert
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void testCorrectMissedPunch_WithNullNotes_ThrowsBadRequestException() {
        // Arrange
        AttendanceRequestDTO correctionRequest = new AttendanceRequestDTO();
        correctionRequest.setEmployeeId(1L);
        correctionRequest.setEventType("CLOCK_OUT");
        correctionRequest.setEventTime(LocalDateTime.now());
        correctionRequest.setNotes(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.correctMissedPunch(correctionRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void testCorrectMissedPunch_WithEmptyNotes_ThrowsBadRequestException() {
        // Arrange
        AttendanceRequestDTO correctionRequest = new AttendanceRequestDTO();
        correctionRequest.setEmployeeId(1L);
        correctionRequest.setEventType("CLOCK_OUT");
        correctionRequest.setEventTime(LocalDateTime.now());
        correctionRequest.setNotes("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.correctMissedPunch(correctionRequest);
        });
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    // ==================== GEOFENCE VALIDATION TESTS ====================

    @Test
    void testValidateGeofence_WithinBoundary_ReturnsTrue() {
        // Arrange
        BigDecimal latitude = new BigDecimal("37.7749");
        BigDecimal longitude = new BigDecimal("-122.4194");

        // Act
        boolean result = attendanceService.validateGeofence(latitude, longitude);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateGeofence_OutsideBoundary_ReturnsFalse() {
        // Arrange
        BigDecimal latitude = new BigDecimal("40.7128"); // New York
        BigDecimal longitude = new BigDecimal("-74.0060");

        // Act
        boolean result = attendanceService.validateGeofence(latitude, longitude);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateGeofence_WithNullLatitude_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.validateGeofence(null, new BigDecimal("-122.4194"));
        });
    }

    @Test
    void testValidateGeofence_WithNullLongitude_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            attendanceService.validateGeofence(new BigDecimal("37.7749"), null);
        });
    }