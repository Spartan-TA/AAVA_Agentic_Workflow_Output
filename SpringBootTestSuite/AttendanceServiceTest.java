package com.example.warehouse.service;

import com.example.warehouse.entity.AttendanceEvent;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.repository.AttendanceRepository;
import com.example.warehouse.repository.EmployeeRepository;
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
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AttendanceService.
 * 
 * Tests cover:
 * - Clock-in and clock-out operations
 * - Event retrieval by employee
 * - Normal cases, boundary conditions, and edge cases
 * - Exception handling for non-existent employees
 * 
 * @author Warehouse Test Team
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private AttendanceEvent clockInEvent;
    private AttendanceEvent clockOutEvent;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();

        clockInEvent = AttendanceEvent.builder()
                .id(1L)
                .employee(testEmployee)
                .timestamp(LocalDateTime.now())
                .eventType(AttendanceEvent.EventType.CLOCK_IN)
                .build();

        clockOutEvent = AttendanceEvent.builder()
                .id(2L)
                .employee(testEmployee)
                .timestamp(LocalDateTime.now().plusHours(8))
                .eventType(AttendanceEvent.EventType.CLOCK_OUT)
                .build();
    }

    // ==================== GET ALL EVENTS TESTS ====================

    /**
     * Test getAllEvents with multiple events - Normal case.
     * Verifies that all attendance events are retrieved correctly.
     */
    @Test
    public void testGetAllEvents_WithMultipleEvents_Success() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceRepository.findAll()).thenReturn(events);

        // Act
        List<AttendanceEvent> result = attendanceService.getAllEvents();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(AttendanceEvent.EventType.CLOCK_IN, result.get(0).getEventType());
        assertEquals(AttendanceEvent.EventType.CLOCK_OUT, result.get(1).getEventType());
        verify(attendanceRepository, times(1)).findAll();
    }

    /**
     * Test getAllEvents with empty list - Boundary condition.
     */
    @Test
    public void testGetAllEvents_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(attendanceRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<AttendanceEvent> result = attendanceService.getAllEvents();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(attendanceRepository, times(1)).findAll();
    }

    /**
     * Test getAllEvents with single event - Edge case.
     */
    @Test
    public void testGetAllEvents_SingleEvent_Success() {
        // Arrange
        when(attendanceRepository.findAll()).thenReturn(Collections.singletonList(clockInEvent));

        // Act
        List<AttendanceEvent> result = attendanceService.getAllEvents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(AttendanceEvent.EventType.CLOCK_IN, result.get(0).getEventType());
        verify(attendanceRepository, times(1)).findAll();
    }

    // ==================== GET EVENTS BY EMPLOYEE TESTS ====================

    /**
     * Test getEventsByEmployee with valid employee ID - Normal case.
     */
    @Test
    public void testGetEventsByEmployee_ValidEmployeeId_Success() {
        // Arrange
        List<AttendanceEvent> events = Arrays.asList(clockInEvent, clockOutEvent);
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(events);

        // Act
        List<AttendanceEvent> result = attendanceService.getEventsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getEmployee().getId());
        verify(attendanceRepository, times(1)).findByEmployeeId(1L);
    }

    /**
     * Test getEventsByEmployee with no events - Boundary condition.
     */
    @Test
    public void testGetEventsByEmployee_NoEvents_ReturnsEmptyList() {
        // Arrange
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<AttendanceEvent> result = attendanceService.getEventsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(attendanceRepository, times(1)).findByEmployeeId(1L);
    }

    /**
     * Test getEventsByEmployee with non-existent employee - Edge case.
     */
    @Test
    public void testGetEventsByEmployee_NonExistentEmployee_ReturnsEmptyList() {
        // Arrange
        when(attendanceRepository.findByEmployeeId(999L)).thenReturn(Collections.emptyList());

        // Act
        List<AttendanceEvent> result = attendanceService.getEventsByEmployee(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(attendanceRepository, times(1)).findByEmployeeId(999L);
    }

    /**
     * Test getEventsByEmployee with null employee ID - Boundary condition.
     */
    @Test
    public void testGetEventsByEmployee_NullEmployeeId_ReturnsEmptyList() {
        // Arrange
        when(attendanceRepository.findByEmployeeId(null)).thenReturn(Collections.emptyList());

        // Act
        List<AttendanceEvent> result = attendanceService.getEventsByEmployee(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(attendanceRepository, times(1)).findByEmployeeId(null);
    }

    // ==================== RECORD EVENT (CLOCK-IN) TESTS ====================

    /**
     * Test recordEvent for clock-in with valid employee - Normal case.
     */
    @Test
    public void testRecordEvent_ClockIn_ValidEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockInEvent);

        // Act
        AttendanceEvent result = attendanceService.recordEvent(1L, AttendanceEvent.EventType.CLOCK_IN);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceEvent.EventType.CLOCK_IN, result.getEventType());
        assertEquals(testEmployee, result.getEmployee());
        assertNotNull(result.getTimestamp());
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    /**
     * Test recordEvent for clock-in with non-existent employee - Edge case.
     */
    @Test
    public void testRecordEvent_ClockIn_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.recordEvent(999L, AttendanceEvent.EventType.CLOCK_IN);
        });
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    /**
     * Test recordEvent for clock-in with null employee ID - Boundary condition.
     */
    @Test
    public void testRecordEvent_ClockIn_NullEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            attendanceService.recordEvent(null, AttendanceEvent.EventType.CLOCK_IN);
        });
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    // ==================== RECORD EVENT (CLOCK-OUT) TESTS ====================

    /**
     * Test recordEvent for clock-out with valid employee - Normal case.
     */
    @Test
    public void testRecordEvent_ClockOut_ValidEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEvent result = attendanceService.recordEvent(1L, AttendanceEvent.EventType.CLOCK_OUT);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceEvent.EventType.CLOCK_OUT, result.getEventType());
        assertEquals(testEmployee, result.getEmployee());
        assertNotNull(result.getTimestamp());
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    /**
     * Test recordEvent for clock-out with non-existent employee - Edge case.
     */
    @Test
    public void testRecordEvent_ClockOut_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.recordEvent(999L, AttendanceEvent.EventType.CLOCK_OUT);
        });
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    /**
     * Test recordEvent for clock-out with inactive employee - Edge case.
     */
    @Test
    public void testRecordEvent_ClockOut_InactiveEmployee_Success() {
        // Arrange
        testEmployee.setActive(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(clockOutEvent);

        // Act
        AttendanceEvent result = attendanceService.recordEvent(1L, AttendanceEvent.EventType.CLOCK_OUT);

        // Assert
        assertNotNull(result);
        assertEquals(AttendanceEvent.EventType.CLOCK_OUT, result.getEventType());
        assertFalse(result.getEmployee().isActive());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    // ==================== MULTIPLE EVENTS SEQUENCE TESTS ====================

    /**
     * Test recording multiple events in sequence - Normal case.
     */
    @Test
    public void testRecordEvent_MultipleEventsSequence_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(clockInEvent)
                .thenReturn(clockOutEvent);

        // Act
        AttendanceEvent clockIn = attendanceService.recordEvent(1L, AttendanceEvent.EventType.CLOCK_IN);
        AttendanceEvent clockOut = attendanceService.recordEvent(1L, AttendanceEvent.EventType.CLOCK_OUT);

        // Assert
        assertNotNull(clockIn);
        assertNotNull(clockOut);
        assertEquals(AttendanceEvent.EventType.CLOCK_IN, clockIn.getEventType());
        assertEquals(AttendanceEvent.EventType.CLOCK_OUT, clockOut.getEventType());
        verify(employeeRepository, times(2)).findById(1L);
        verify(attendanceRepository, times(2)).save(any(AttendanceEvent.class));
    }

    /**
     * Test recording same event type twice - Edge case.
     */
    @Test
    public void testRecordEvent_DuplicateClockIn_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        AttendanceEvent secondClockIn = AttendanceEvent.builder()
                .id(3L)
                .employee(testEmployee)
                .timestamp(LocalDateTime.now().plusMinutes(5))
                .eventType(AttendanceEvent.EventType.CLOCK_IN)
                .build();
        when(attendanceRepository.save(any(AttendanceEvent.class)))
                .thenReturn(clockInEvent)
                .thenReturn(secondClockIn);

        // Act
        AttendanceEvent firstClockIn = attendanceService.recordEvent(1L, AttendanceEvent.EventType.CLOCK_IN);
        AttendanceEvent duplicateClockIn = attendanceService.recordEvent(1L, AttendanceEvent.EventType.CLOCK_IN);

        // Assert
        assertNotNull(firstClockIn);
        assertNotNull(duplicateClockIn);
        assertEquals(AttendanceEvent.EventType.CLOCK_IN, firstClockIn.getEventType());
        assertEquals(AttendanceEvent.EventType.CLOCK_IN, duplicateClockIn.getEventType());
        verify(attendanceRepository, times(2)).save(any(AttendanceEvent.class));
    }

    /**
     * Test recordEvent with negative employee ID - Edge case.
     */
    @Test
    public void testRecordEvent_NegativeEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            attendanceService.recordEvent(-1L, AttendanceEvent.EventType.CLOCK_IN);
        });
        verify(attendanceRepository, never()).save(any(AttendanceEvent.class));
    }

    /**
     * Test recordEvent with null event type - Boundary condition.
     */
    @Test
    public void testRecordEvent_NullEventType_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        AttendanceEvent eventWithNullType = AttendanceEvent.builder()
                .id(1L)
                .employee(testEmployee)
                .timestamp(LocalDateTime.now())
                .eventType(null)
                .build();
        when(attendanceRepository.save(any(AttendanceEvent.class))).thenReturn(eventWithNullType);

        // Act
        AttendanceEvent result = attendanceService.recordEvent(1L, null);

        // Assert
        assertNotNull(result);
        assertNull(result.getEventType());
        verify(attendanceRepository, times(1)).save(any(AttendanceEvent.class));
    }

    /**
     * Test getEventsByEmployee with large number of events - Performance edge case.
     */
    @Test
    public void testGetEventsByEmployee_LargeNumberOfEvents_Success() {
        // Arrange
        List<AttendanceEvent> largeEventList = Arrays.asList(
                clockInEvent, clockOutEvent, clockInEvent, clockOutEvent,
                clockInEvent, clockOutEvent, clockInEvent, clockOutEvent,
                clockInEvent, clockOutEvent
        );
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(largeEventList);

        // Act
        List<AttendanceEvent> result = attendanceService.getEventsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.size());
        verify(attendanceRepository, times(1)).findByEmployeeId(1L);
    }
}