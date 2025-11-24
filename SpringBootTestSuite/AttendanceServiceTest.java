package com.warehousemgmt.service;

import com.warehousemgmt.domain.AttendanceEvent;
import com.warehousemgmt.domain.AttendanceType;
import com.warehousemgmt.domain.GeoLocation;
import com.warehousemgmt.dto.ClockEventDTO;
import com.warehousemgmt.repository.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService (Epic E04)
 * Covers clock-in/out, geofencing, device validation, and attendance tracking
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private ClockEventDTO validClockInDTO;
    private AttendanceEvent validAttendanceEvent;
    private GeoLocation validLocation;

    @BeforeEach
    public void setUp() {
        validLocation = new GeoLocation();
        validLocation.setLatitude(40.7128);
        validLocation.setLongitude(-74.0060);

        validClockInDTO = new ClockEventDTO();
        validClockInDTO.setEmployeeId(1L);
        validClockInDTO.setTimestamp(LocalDateTime.now());
        validClockInDTO.setDeviceId("DEVICE001");
        validClockInDTO.setLocation(validLocation);

        validAttendanceEvent = new AttendanceEvent();
        validAttendanceEvent.setId(1L);
        validAttendanceEvent.setEmployeeId(1L);
        validAttendanceEvent.setTimestamp(LocalDateTime.now());
        validAttendanceEvent.setType(AttendanceType.CLOCK_IN);
        validAttendanceEvent.setDeviceId("DEVICE001");
        validAttendanceEvent.setLocation(validLocation);
    }

    // ========== CLOCK-IN TESTS ==========

    @Test
    public void testClockIn_ValidInput_Success() {
        // Arrange
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(validAttendanceEvent);

        // Act
        AttendanceEvent result = attendanceService.clockIn(validClockInDTO);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceType.CLOCK_IN, result.getType());
        assertEquals(1L, result.getEmployeeId());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockIn_NullEmployeeId_ThrowsException() {
        // Arrange
        validClockInDTO.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockIn_NullTimestamp_ThrowsException() {
        // Arrange
        validClockInDTO.setTimestamp(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
    }

    @Test
    public void testClockIn_NullDeviceId_ThrowsException() {
        // Arrange
        validClockInDTO.setDeviceId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
    }

    @Test
    public void testClockIn_EmptyDeviceId_ThrowsException() {
        // Arrange
        validClockInDTO.setDeviceId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
    }

    @Test
    public void testClockIn_FutureTimestamp_ThrowsException() {
        // Arrange
        validClockInDTO.setTimestamp(LocalDateTime.now().plusHours(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
    }

    @Test
    public void testClockIn_AlreadyClockedIn_ThrowsException() {
        // Arrange
        when(attendanceRepository.findLatestByEmployeeId(1L)).thenReturn(Optional.of(validAttendanceEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
    }

    // ========== GEOFENCING TESTS ==========

    @Test
    public void testClockIn_WithinGeofence_Success() {
        // Arrange
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(validAttendanceEvent);

        // Act
        AttendanceEvent result = attendanceService.clockIn(validClockInDTO);

        // Assert
        assertNotNull(result);
        assertEquals(40.7128, result.getLocation().getLatitude());
        assertEquals(-74.0060, result.getLocation().getLongitude());
    }

    @Test
    public void testClockIn_OutsideGeofence_ThrowsException() {
        // Arrange
        GeoLocation outsideLocation = new GeoLocation();
        outsideLocation.setLatitude(0.0);
        outsideLocation.setLongitude(0.0);
        validClockInDTO.setLocation(outsideLocation);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
    }

    @Test
    public void testClockIn_NullLocation_ThrowsException() {
        // Arrange
        validClockInDTO.setLocation(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
    }

    @Test
    public void testClockIn_InvalidLatitude_ThrowsException() {
        // Arrange
        validLocation.setLatitude(91.0); // Invalid latitude

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
    }

    @Test
    public void testClockIn_InvalidLongitude_ThrowsException() {
        // Arrange
        validLocation.setLongitude(181.0); // Invalid longitude

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(validClockInDTO);
        });
    }

    // ========== CLOCK-OUT TESTS ==========

    @Test
    public void testClockOut_ValidInput_Success() {
        // Arrange
        ClockEventDTO clockOutDTO = new ClockEventDTO();
        clockOutDTO.setEmployeeId(1L);
        clockOutDTO.setTimestamp(LocalDateTime.now());
        clockOutDTO.setDeviceId("DEVICE001");
        clockOutDTO.setLocation(validLocation);

        AttendanceEvent clockOutEvent = new AttendanceEvent();
        clockOutEvent.setId(2L);
        clockOutEvent.setEmployeeId(1L);
        clockOutEvent.setTimestamp(LocalDateTime.now());
        clockOutEvent.setType(AttendanceType.CLOCK_OUT);
        clockOutEvent.setDeviceId("DEVICE001");
        clockOutEvent.setLocation(validLocation);

        when(attendanceRepository.findLatestByEmployeeId(1L)).thenReturn(Optional.of(validAttendanceEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEvent result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceType.CLOCK_OUT, result.getType());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockOut_NotClockedIn_ThrowsException() {
        // Arrange
        ClockEventDTO clockOutDTO = new ClockEventDTO();
        clockOutDTO.setEmployeeId(1L);
        clockOutDTO.setTimestamp(LocalDateTime.now());
        clockOutDTO.setDeviceId("DEVICE001");
        clockOutDTO.setLocation(validLocation);

        when(attendanceRepository.findLatestByEmployeeId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(clockOutDTO);
        });
    }

    @Test
    public void testClockOut_AlreadyClockedOut_ThrowsException() {
        // Arrange
        ClockEventDTO clockOutDTO = new ClockEventDTO();
        clockOutDTO.setEmployeeId(1L);
        clockOutDTO.setTimestamp(LocalDateTime.now());
        clockOutDTO.setDeviceId("DEVICE001");
        clockOutDTO.setLocation(validLocation);

        AttendanceEvent previousClockOut = new AttendanceEvent();
        previousClockOut.setType(AttendanceType.CLOCK_OUT);

        when(attendanceRepository.findLatestByEmployeeId(1L)).thenReturn(Optional.of(previousClockOut));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.clockOut(clockOutDTO);
        });
    }

    @Test
    public void testClockOut_ClockOutBeforeClockIn_ThrowsException() {
        // Arrange
        ClockEventDTO clockOutDTO = new ClockEventDTO();
        clockOutDTO.setEmployeeId(1L);
        clockOutDTO.setTimestamp(LocalDateTime.now().minusHours(1));
        clockOutDTO.setDeviceId("DEVICE001");
        clockOutDTO.setLocation(validLocation);

        when(attendanceRepository.findLatestByEmployeeId(1L)).thenReturn(Optional.of(validAttendanceEvent));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(clockOutDTO);
        });
    }

    // ========== SHIFT HOURS CALCULATION TESTS ==========

    @Test
    public void testCalculateShiftHours_ValidShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.of(2023, 1, 1, 8, 0);
        LocalDateTime clockOutTime = LocalDateTime.of(2023, 1, 1, 17, 0);

        // Act
        double hours = attendanceService.calculateShiftHours(clockInTime, clockOutTime);

        // Assert
        assertEquals(9.0, hours, 0.01);
    }

    @Test
    public void testCalculateShiftHours_PartialHour_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.of(2023, 1, 1, 8, 0);
        LocalDateTime clockOutTime = LocalDateTime.of(2023, 1, 1, 12, 30);

        // Act
        double hours = attendanceService.calculateShiftHours(clockInTime, clockOutTime);

        // Assert
        assertEquals(4.5, hours, 0.01);
    }

    @Test
    public void testCalculateShiftHours_OvernightShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.of(2023, 1, 1, 22, 0);
        LocalDateTime clockOutTime = LocalDateTime.of(2023, 1, 2, 6, 0);

        // Act
        double hours = attendanceService.calculateShiftHours(clockInTime, clockOutTime);

        // Assert
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    public void testCalculateShiftHours_NullClockIn_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.calculateShiftHours(null, LocalDateTime.now());
        });
    }

    @Test
    public void testCalculateShiftHours_NullClockOut_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.calculateShiftHours(LocalDateTime.now(), null);
        });
    }

    // ========== MISSED PUNCH CORRECTION TESTS ==========

    @Test
    public void testRequestMissedPunchCorrection_ValidRequest_Success() {
        // Arrange
        Long employeeId = 1L;
        LocalDateTime missedTimestamp = LocalDateTime.now().minusHours(2);
        AttendanceType type = AttendanceType.CLOCK_IN;
        String reason = "Forgot to clock in";

        // Act
        attendanceService.requestMissedPunchCorrection(employeeId, missedTimestamp, type, reason);

        // Assert
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testRequestMissedPunchCorrection_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestMissedPunchCorrection(null, LocalDateTime.now(), AttendanceType.CLOCK_IN, "Reason");
        });
    }

    @Test
    public void testRequestMissedPunchCorrection_NullTimestamp_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestMissedPunchCorrection(1L, null, AttendanceType.CLOCK_IN, "Reason");
        });
    }

    @Test
    public void testRequestMissedPunchCorrection_NullType_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestMissedPunchCorrection(1L, LocalDateTime.now(), null, "Reason");
        });
    }

    @Test
    public void testRequestMissedPunchCorrection_EmptyReason_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.requestMissedPunchCorrection(1L, LocalDateTime.now(), AttendanceType.CLOCK_IN, "");
        });
    }

    // ========== ATTENDANCE REPORT TESTS ==========

    @Test
    public void testGenerateAttendanceReport_ValidDateRange_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 1, 31, 23, 59);

        // Act
        attendanceService.generateAttendanceReport(startDate, endDate);

        // Assert
        verify(attendanceRepository, times(1)).findByTimestampBetween(startDate, endDate);
    }

    @Test
    public void testGenerateAttendanceReport_NullStartDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.generateAttendanceReport(null, LocalDateTime.now());
        });
    }

    @Test
    public void testGenerateAttendanceReport_NullEndDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.generateAttendanceReport(LocalDateTime.now(), null);
        });
    }

    @Test
    public void testGenerateAttendanceReport_EndBeforeStart_ThrowsException() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 31, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 1, 1, 0, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.generateAttendanceReport(startDate, endDate);
        });
    }

    // ========== DEVICE VALIDATION TESTS ==========

    @Test
    public void testValidateDevice_ValidDevice_Success() {
        // Arrange
        String deviceId = "DEVICE001";

        // Act
        boolean isValid = attendanceService.validateDevice(deviceId);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateDevice_InvalidDevice_ReturnsFalse() {
        // Arrange
        String deviceId = "INVALID_DEVICE";

        // Act
        boolean isValid = attendanceService.validateDevice(deviceId);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateDevice_NullDevice_ReturnsFalse() {
        // Act
        boolean isValid = attendanceService.validateDevice(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateDevice_EmptyDevice_ReturnsFalse() {
        // Act
        boolean isValid = attendanceService.validateDevice("");

        // Assert
        assertFalse(isValid);
    }
}