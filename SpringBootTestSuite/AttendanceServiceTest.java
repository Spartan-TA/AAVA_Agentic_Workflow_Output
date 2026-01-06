package com.company.wms.attendance.service;

import com.company.wms.attendance.dto.AttendanceEventDTO;
import com.company.wms.attendance.dto.ClockEventDTO;
import com.company.wms.attendance.model.AttendanceEvent;
import com.company.wms.attendance.model.AttendanceType;
import com.company.wms.attendance.repository.AttendanceEventRepository;
import com.company.wms.employee.model.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.exception.BusinessException;
import com.company.wms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Covers clock in/out operations, hours calculation, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceEventRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;
    private ClockEventDTO clockInDTO;
    private ClockEventDTO clockOutDTO;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setDepartment("Warehouse");

        // Setup clock in event
        clockInEvent = new AttendanceEvent();
        clockInEvent.setId(1L);
        clockInEvent.setEmployee(testEmployee);
        clockInEvent.setTimestamp(LocalDateTime.now().minusHours(8));
        clockInEvent.setType(AttendanceType.CLOCK_IN);
        clockInEvent.setDeviceId("DEVICE001");
        clockInEvent.setLocation("40.7128,-74.0060");

        // Setup clock out event
        clockOutEvent = new AttendanceEvent();
        clockOutEvent.setId(2L);
        clockOutEvent.setEmployee(testEmployee);
        clockOutEvent.setTimestamp(LocalDateTime.now());
        clockOutEvent.setType(AttendanceType.CLOCK_OUT);
        clockOutEvent.setDeviceId("DEVICE001");
        clockOutEvent.setHoursWorked(new BigDecimal("8.00"));

        // Setup clock in DTO
        clockInDTO = new ClockEventDTO();
        clockInDTO.setBadgeId("EMP001");
        clockInDTO.setDeviceId("DEVICE001");
        clockInDTO.setLocation("40.7128,-74.0060");

        // Setup clock out DTO
        clockOutDTO = new ClockEventDTO();
        clockOutDTO.setBadgeId("EMP001");
        clockOutDTO.setDeviceId("DEVICE001");
        clockOutDTO.setLocation("40.7128,-74.0060");
    }

    // ========== CLOCK IN TESTS ==========

    @Test
    void clockIn_ValidInput_ReturnsAttendanceEventDTO() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInDTO);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceType.CLOCK_IN, result.getType());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void clockIn_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP999"))
            .thenReturn(Optional.empty());

        clockInDTO.setBadgeId("EMP999");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockIn(clockInDTO);
        });
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void clockIn_AlreadyClockedIn_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.of(clockInEvent));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockIn(clockInDTO);
        });
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    void clockIn_NullBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        clockInDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDTO);
        });
    }

    @Test
    void clockIn_EmptyBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        clockInDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockIn(clockInDTO);
        });
    }

    @Test
    void clockIn_WithoutLocation_Success() {
        // Arrange
        clockInDTO.setLocation(null);
        
        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void clockIn_WithoutDeviceId_Success() {
        // Arrange
        clockInDTO.setDeviceId(null);
        
        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInDTO);

        // Assert
        assertNotNull(result);
    }

    // ========== CLOCK OUT TESTS ==========

    @Test
    void clockOut_ValidInput_ReturnsAttendanceEventDTOWithHours() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceType.CLOCK_OUT, result.getType());
        assertNotNull(result.getHoursWorked());
        assertTrue(result.getHoursWorked().compareTo(BigDecimal.ZERO) > 0);
        verify(attendanceRepository, times(2)).save(any(AttendanceEvent.class));
    }

    @Test
    void clockOut_NoOpenClockIn_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(clockOutDTO);
        });
    }

    @Test
    void clockOut_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP999"))
            .thenReturn(Optional.empty());

        clockOutDTO.setBadgeId("EMP999");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.clockOut(clockOutDTO);
        });
    }

    @Test
    void clockOut_NullBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        clockOutDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.clockOut(clockOutDTO);
        });
    }

    // ========== HOURS CALCULATION TESTS ==========

    @Test
    void calculateHours_StandardShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 17, 0);
        
        clockInEvent.setTimestamp(clockIn);
        clockOutEvent.setTimestamp(clockOut);

        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result.getHoursWorked());
        assertEquals(new BigDecimal("8.00"), result.getHoursWorked());
    }

    @Test
    void calculateHours_OvertimeShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 21, 0);
        
        clockInEvent.setTimestamp(clockIn);
        clockOutEvent.setTimestamp(clockOut);

        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result.getHoursWorked());
        assertTrue(result.getHoursWorked().compareTo(new BigDecimal("8.00")) > 0);
    }

    @Test
    void calculateHours_PartialHours_ReturnsDecimalHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 13, 30);
        
        clockInEvent.setTimestamp(clockIn);
        clockOutEvent.setTimestamp(clockOut);

        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result.getHoursWorked());
        assertEquals(new BigDecimal("4.50"), result.getHoursWorked());
    }

    @Test
    void calculateHours_VeryShortShift_ReturnsMinimalHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 15, 9, 15);
        
        clockInEvent.setTimestamp(clockIn);
        clockOutEvent.setTimestamp(clockOut);

        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result.getHoursWorked());
        assertEquals(new BigDecimal("0.25"), result.getHoursWorked());
    }

    @Test
    void calculateHours_OvernightShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 22, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 16, 6, 0);
        
        clockInEvent.setTimestamp(clockIn);
        clockOutEvent.setTimestamp(clockOut);

        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result.getHoursWorked());
        assertEquals(new BigDecimal("8.00"), result.getHoursWorked());
    }

    // ========== CORRECTION TESTS ==========

    @Test
    void submitCorrection_ValidInput_CreatesApprovalTask() {
        // Arrange
        AttendanceEventDTO correctionDTO = new AttendanceEventDTO();
        correctionDTO.setEmployeeId(1L);
        correctionDTO.setTimestamp(LocalDateTime.now().minusDays(1));
        correctionDTO.setType(AttendanceType.CLOCK_IN);
        correctionDTO.setCorrection(true);

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.submitCorrection(correctionDTO);

        // Assert
        assertNotNull(result);
        assertTrue(result.isCorrection());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    void submitCorrection_FutureTimestamp_ThrowsBusinessException() {
        // Arrange
        AttendanceEventDTO correctionDTO = new AttendanceEventDTO();
        correctionDTO.setEmployeeId(1L);
        correctionDTO.setTimestamp(LocalDateTime.now().plusDays(1));
        correctionDTO.setType(AttendanceType.CLOCK_IN);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.submitCorrection(correctionDTO);
        });
    }

    @Test
    void submitCorrection_TooOld_ThrowsBusinessException() {
        // Arrange
        AttendanceEventDTO correctionDTO = new AttendanceEventDTO();
        correctionDTO.setEmployeeId(1L);
        correctionDTO.setTimestamp(LocalDateTime.now().minusDays(31));
        correctionDTO.setType(AttendanceType.CLOCK_IN);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.submitCorrection(correctionDTO);
        });
    }

    // ========== DAILY TOTALS TESTS ==========

    @Test
    void calculateDailyTotals_ValidDate_ReturnsCorrectTotals() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(attendanceRepository.calculateTotalHours(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(new BigDecimal("8.00"));

        // Act
        BigDecimal result = attendanceService.calculateDailyTotals(date);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("8.00"), result);
    }

    @Test
    void calculateDailyTotals_NoEvents_ReturnsZero() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(attendanceRepository.calculateTotalHours(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(BigDecimal.ZERO);

        // Act
        BigDecimal result = attendanceService.calculateDailyTotals(date);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculateDailyTotals_NullDate_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.calculateDailyTotals(null);
        });
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void clockIn_MidnightTimestamp_Success() {
        // Arrange
        LocalDateTime midnight = LocalDateTime.of(2024, 1, 15, 0, 0);
        clockInEvent.setTimestamp(midnight);

        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void clockOut_SameMinuteAsClockIn_ThrowsBusinessException() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        clockInEvent.setTimestamp(now);
        clockOutEvent.setTimestamp(now);

        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.of(clockInEvent));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            attendanceService.clockOut(clockOutDTO);
        });
    }

    @Test
    void clockIn_MaxLengthLocation_Success() {
        // Arrange
        String maxLocation = "40.7128,-74.0060," + "A".repeat(200);
        clockInDTO.setLocation(maxLocation);

        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockInEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockIn(clockInDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void calculateHours_24HourShift_ReturnsCorrectHours() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 16, 0, 0);
        
        clockInEvent.setTimestamp(clockIn);
        clockOutEvent.setTimestamp(clockOut);

        when(employeeRepository.findByBadgeId("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findOpenClockInForEmployee(1L))
            .thenReturn(Optional.of(clockInEvent));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
            .thenReturn(clockOutEvent);

        // Act
        AttendanceEventDTO result = attendanceService.clockOut(clockOutDTO);

        // Assert
        assertNotNull(result.getHoursWorked());
        assertEquals(new BigDecimal("24.00"), result.getHoursWorked());
    }
}