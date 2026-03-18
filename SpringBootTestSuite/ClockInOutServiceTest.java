package com.example.warehouse.service;

import com.example.warehouse.entity.AttendanceEvent;
import com.example.warehouse.enums.EventType;
import com.example.warehouse.exception.BusinessValidationException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.AttendanceEventRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClockInOutServiceTest {

    @Mock private AttendanceEventRepository attendanceEventRepository;
    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private ClockInOutService clockInOutService;

    private AttendanceEvent event;

    @BeforeEach
    void setUp() {
        event = new AttendanceEvent();
        event.setId(1L);
        event.setEmployeeId(1L);
        event.setEventType(EventType.CLOCK_IN);
        event.setEventTimestamp(LocalDateTime.now());
        event.setLocation("Main Gate");
        event.setDeviceId("DEV001");
    }

    @Test
    void clockIn_WithValidData_ShouldReturnAttendanceEvent() {
        // Arrange
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(event);

        // Act
        AttendanceEvent result = clockInOutService.clockIn(1L, "Main Gate", "DEV001");

        // Assert
        assertNotNull(result);
        assertEquals(EventType.CLOCK_IN, result.getEventType());
    }

    @Test
    void clockIn_WithNonExistingEmployee_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.existsById(2L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> clockInOutService.clockIn(2L, "Main Gate", "DEV001"));
    }

    @Test
    void clockIn_WithNullLocation_ShouldThrowBusinessValidationException() {
        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> clockInOutService.clockIn(1L, null, "DEV001"));
    }

    @Test
    void clockOut_WithValidData_ShouldReturnAttendanceEvent() {
        // Arrange
        event.setEventType(EventType.CLOCK_OUT);
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(attendanceEventRepository.save(any(AttendanceEvent.class))).thenReturn(event);

        // Act
        AttendanceEvent result = clockInOutService.clockOut(1L, "Main Gate", "DEV001");

        // Assert
        assertNotNull(result);
        assertEquals(EventType.CLOCK_OUT, result.getEventType());
    }

    @Test
    void clockOut_WithNullDeviceId_ShouldThrowBusinessValidationException() {
        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> clockInOutService.clockOut(1L, "Main Gate", null));
    }

    @Test
    void clockOut_WithNonExistingEmployee_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> clockInOutService.clockOut(99L, "Main Gate", "DEV001"));
    }
}