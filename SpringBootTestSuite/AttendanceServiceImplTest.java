package com.warehouse.ems.service.attendance;

import com.warehouse.ems.domain.attendance.AttendanceEvent;
import com.warehouse.ems.domain.employee.Employee;
import com.warehouse.ems.domain.employee.Role;
import com.warehouse.ems.dto.attendance.AttendanceEventRequest;
import com.warehouse.ems.dto.attendance.AttendanceEventResponse;
import com.warehouse.ems.mapper.AttendanceEventMapper;
import com.warehouse.ems.repository.attendance.AttendanceEventRepository;
import com.warehouse.ems.repository.employee.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AttendanceServiceImpl.
 * Tests cover clock-in/out operations, attendance queries, hours calculation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceImplTest {

    @Mock
    private AttendanceEventRepository attendanceEventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceEventMapper attendanceEventMapper;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee testEmployee;
    private AttendanceEvent testEvent;
    private AttendanceEventRequest testRequest;
    private AttendanceEventResponse testResponse;

    @BeforeEach
    public void setUp() {
        // Setup test employee
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(Role.WORKER)
                .department("Warehouse")
                .build();

        // Setup test attendance event
        testEvent = AttendanceEvent.builder()
                .id(1L)
                .employee(testEmployee)
                .clockIn(LocalDateTime.now())
                .location("Warehouse A")
                .device("Terminal-01")
                .deleted(false)
                .build();

        // Setup test request
        testRequest = new AttendanceEventRequest();
        testRequest.setEmployeeId(1L);
        testRequest.setLocation("Warehouse A");
        testRequest.setDevice("Terminal-01");

        // Setup test response
        testResponse = AttendanceEventResponse.builder()
                .id(1L)
                .employeeId(1L)
                .clockIn(LocalDateTime.now())
                .location("Warehouse A")
                .device("Terminal-01")
                .build();
    }

    // ==================== CLOCK IN TESTS ====================

    @Test
    public void testClockIn_ValidRequest_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testEvent);
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(testResponse);

        // Act
        AttendanceEventResponse result = attendanceService.clockIn(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertEquals("Warehouse A", result.getLocation());
        assertEquals("Terminal-01", result.getDevice());
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        testRequest.setEmployeeId(999L);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> attendanceService.clockIn(testRequest)
        );
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockIn_NullEmployeeId_ThrowsException() {
        // Arrange
        testRequest.setEmployeeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> attendanceService.clockIn(testRequest));
    }

    @Test
    public void testClockIn_WithoutLocation_Success() {
        // Arrange
        testRequest.setLocation(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testEvent);
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(testResponse);

        // Act
        AttendanceEventResponse result = attendanceService.clockIn(testRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testClockIn_WithoutDevice_Success() {
        // Arrange
        testRequest.setDevice(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testEvent);
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(testResponse);

        // Act
        AttendanceEventResponse result = attendanceService.clockIn(testRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testClockIn_LongLocationString_Success() {
        // Arrange
        String longLocation = "A".repeat(128);
        testRequest.setLocation(longLocation);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testEvent);
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(testResponse);

        // Act
        AttendanceEventResponse result = attendanceService.clockIn(testRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testClockIn_SpecialCharactersInLocation_Success() {
        // Arrange
        testRequest.setLocation("Warehouse A - Section #5 (North)");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testEvent);
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(testResponse);

        // Act
        AttendanceEventResponse result = attendanceService.clockIn(testRequest);

        // Assert
        assertNotNull(result);
    }

    // ==================== CLOCK OUT TESTS ====================

    @Test
    public void testClockOut_ValidEventId_Success() {
        // Arrange
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(testEvent);
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(testResponse);

        // Act
        AttendanceEventResponse result = attendanceService.clockOut(1L);

        // Assert
        assertNotNull(result);
        verify(attendanceEventRepository, times(1)).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockOut_NonExistentEventId_ThrowsException() {
        // Arrange
        when(attendanceEventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> attendanceService.clockOut(999L)
        );
        assertTrue(exception.getMessage().contains("Attendance event not found"));
    }

    @Test
    public void testClockOut_AlreadyClockedOut_ThrowsException() {
        // Arrange
        testEvent.setClockOut(LocalDateTime.now());
        when(attendanceEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> attendanceService.clockOut(1L)
        );
        assertTrue(exception.getMessage().contains("already clocked out"));
        verify(attendanceEventRepository, never()).save(any(AttendanceEvent.class));
    }

    @Test
    public void testClockOut_NullEventId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> attendanceService.clockOut(null));
    }

    @Test
    public void testClockOut_NegativeEventId_ThrowsException() {
        // Arrange
        when(attendanceEventRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> attendanceService.clockOut(-1L));
    }

    @Test
    public void testClockOut_ZeroEventId_ThrowsException() {
        // Arrange
        when(attendanceEventRepository.findById(0L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> attendanceService.clockOut(0L));
    }

    // ==================== GET ATTENDANCE FOR EMPLOYEE TESTS ====================

    @Test
    public void testGetAttendanceForEmployee_ValidInput_Success() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testEvent));
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(testResponse);

        // Act
        List<AttendanceEventResponse> result = attendanceService.getAttendanceForEmployee(1L, date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAttendanceForEmployee_NonExistentEmployee_ThrowsException() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
                () -> attendanceService.getAttendanceForEmployee(999L, date));
    }

    @Test
    public void testGetAttendanceForEmployee_NoRecords_ReturnsEmptyList() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<AttendanceEventResponse> result = attendanceService.getAttendanceForEmployee(1L, date);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetAttendanceForEmployee_PastDate_Success() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2023, 1, 1);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testEvent));
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(testResponse);

        // Act
        List<AttendanceEventResponse> result = attendanceService.getAttendanceForEmployee(1L, pastDate);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testGetAttendanceForEmployee_FutureDate_ReturnsEmptyList() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<AttendanceEventResponse> result = attendanceService.getAttendanceForEmployee(1L, futureDate);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetAttendanceForEmployee_MultipleEvents_Success() {
        // Arrange
        LocalDate date = LocalDate.now();
        AttendanceEvent event2 = AttendanceEvent.builder()
                .id(2L)
                .employee(testEmployee)
                .clockIn(LocalDateTime.now().plusHours(8))
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testEvent, event2));
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(testResponse);

        // Act
        List<AttendanceEventResponse> result = attendanceService.getAttendanceForEmployee(1L, date);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== CALCULATE HOURS WORKED TESTS ====================

    @Test
    public void testCalculateHoursWorked_CompleteShift_Success() {
        // Arrange
        LocalDate date = LocalDate.now();
        LocalDateTime clockIn = LocalDateTime.now().withHour(9).withMinute(0);
        LocalDateTime clockOut = LocalDateTime.now().withHour(17).withMinute(0);
        
        AttendanceEventResponse completeEvent = AttendanceEventResponse.builder()
                .id(1L)
                .employeeId(1L)
                .clockIn(clockIn)
                .clockOut(clockOut)
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testEvent));
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(completeEvent);

        // Act
        double hours = attendanceService.calculateHoursWorked(1L, date);

        // Assert
        assertEquals(8.0, hours, 0.1);
    }

    @Test
    public void testCalculateHoursWorked_NoClockOut_ReturnsZero() {
        // Arrange
        LocalDate date = LocalDate.now();
        AttendanceEventResponse incompleteEvent = AttendanceEventResponse.builder()
                .id(1L)
                .employeeId(1L)
                .clockIn(LocalDateTime.now())
                .clockOut(null)
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testEvent));
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(incompleteEvent);

        // Act
        double hours = attendanceService.calculateHoursWorked(1L, date);

        // Assert
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    public void testCalculateHoursWorked_NoRecords_ReturnsZero() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        double hours = attendanceService.calculateHoursWorked(1L, date);

        // Assert
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    public void testCalculateHoursWorked_PartialHours_Success() {
        // Arrange
        LocalDate date = LocalDate.now();
        LocalDateTime clockIn = LocalDateTime.now().withHour(9).withMinute(0);
        LocalDateTime clockOut = LocalDateTime.now().withHour(13).withMinute(30);
        
        AttendanceEventResponse partialEvent = AttendanceEventResponse.builder()
                .id(1L)
                .employeeId(1L)
                .clockIn(clockIn)
                .clockOut(clockOut)
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testEvent));
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(partialEvent);

        // Act
        double hours = attendanceService.calculateHoursWorked(1L, date);

        // Assert
        assertEquals(4.5, hours, 0.1);
    }

    @Test
    public void testCalculateHoursWorked_MultipleShifts_Success() {
        // Arrange
        LocalDate date = LocalDate.now();
        LocalDateTime clockIn1 = LocalDateTime.now().withHour(6).withMinute(0);
        LocalDateTime clockOut1 = LocalDateTime.now().withHour(10).withMinute(0);
        LocalDateTime clockIn2 = LocalDateTime.now().withHour(14).withMinute(0);
        LocalDateTime clockOut2 = LocalDateTime.now().withHour(18).withMinute(0);
        
        AttendanceEventResponse event1 = AttendanceEventResponse.builder()
                .id(1L)
                .employeeId(1L)
                .clockIn(clockIn1)
                .clockOut(clockOut1)
                .build();
        
        AttendanceEventResponse event2 = AttendanceEventResponse.builder()
                .id(2L)
                .employeeId(1L)
                .clockIn(clockIn2)
                .clockOut(clockOut2)
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testEvent, testEvent));
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class)))
                .thenReturn(event1, event2);

        // Act
        double hours = attendanceService.calculateHoursWorked(1L, date);

        // Assert
        assertEquals(8.0, hours, 0.1);
    }

    @Test
    public void testCalculateHoursWorked_OvertimeShift_Success() {
        // Arrange
        LocalDate date = LocalDate.now();
        LocalDateTime clockIn = LocalDateTime.now().withHour(8).withMinute(0);
        LocalDateTime clockOut = LocalDateTime.now().withHour(20).withMinute(0);
        
        AttendanceEventResponse overtimeEvent = AttendanceEventResponse.builder()
                .id(1L)
                .employeeId(1L)
                .clockIn(clockIn)
                .clockOut(clockOut)
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceEventRepository.findByEmployeeAndClockInBetween(
                any(Employee.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testEvent));
        when(attendanceEventMapper.toResponse(any(AttendanceEvent.class))).thenReturn(overtimeEvent);

        // Act
        double hours = attendanceService.calculateHoursWorked(1L, date);

        // Assert
        assertEquals(12.0, hours, 0.1);
    }

    @Test
    public void testCalculateHoursWorked_NonExistentEmployee_ThrowsException() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
                () -> attendanceService.calculateHoursWorked(999L, date));
    }
}