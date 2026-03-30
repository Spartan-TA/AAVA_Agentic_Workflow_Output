package com.companyname.wems.scheduling.service;

import com.companyname.wems.scheduling.dto.ShiftTemplateRequest;
import com.companyname.wems.scheduling.dto.ShiftAssignmentRequest;
import com.companyname.wems.scheduling.dto.ShiftTemplateResponse;
import com.companyname.wems.scheduling.dto.ShiftAssignmentResponse;
import com.companyname.wems.scheduling.entity.ShiftTemplate;
import com.companyname.wems.scheduling.entity.ShiftAssignment;
import com.companyname.wems.scheduling.entity.BlackoutDate;
import com.companyname.wems.scheduling.repository.ShiftTemplateRepository;
import com.companyname.wems.scheduling.repository.ShiftAssignmentRepository;
import com.companyname.wems.scheduling.repository.BlackoutDateRepository;
import com.companyname.wems.employee.entity.Employee;
import com.companyname.wems.employee.repository.EmployeeRepository;
import com.companyname.wems.exception.BusinessException;
import com.companyname.wems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftService
 * Tests cover shift templates, assignments, conflict detection, and scheduling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Shift Service Tests")
class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private BlackoutDateRepository blackoutDateRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    private Employee validEmployee;
    private ShiftTemplate validShiftTemplate;
    private ShiftTemplateRequest validTemplateRequest;
    private ShiftAssignment validShiftAssignment;
    private ShiftAssignmentRequest validAssignmentRequest;

    @BeforeEach
    void setUp() {
        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP12345")
                .role(Employee.Role.WORKER)
                .department("Shipping")
                .shiftGroup("Morning")
                .status(Employee.Status.ACTIVE)
                .build();

        validShiftTemplate = ShiftTemplate.builder()
                .id(1L)
                .name("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .isRecurring(true)
                .recurrencePattern("DAILY")
                .department("Shipping")
                .maxEmployees(10)
                .build();

        validTemplateRequest = ShiftTemplateRequest.builder()
                .name("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .isRecurring(true)
                .recurrencePattern("DAILY")
                .department("Shipping")
                .maxEmployees(10)
                .build();

        validShiftAssignment = ShiftAssignment.builder()
                .id(1L)
                .employee(validEmployee)
                .shiftTemplate(validShiftTemplate)
                .shiftDate(LocalDate.now())
                .status(ShiftAssignment.Status.SCHEDULED)
                .build();

        validAssignmentRequest = ShiftAssignmentRequest.builder()
                .employeeId(1L)
                .shiftTemplateId(1L)
                .shiftDate(LocalDate.now())
                .build();
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    @DisplayName("Should create shift template with valid input")
    void testCreateShiftTemplate_ValidInput_Success() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateResponse response = shiftService.createShiftTemplate(validTemplateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Morning Shift", response.getName());
        assertEquals(LocalTime.of(8, 0), response.getStartTime());
        assertEquals(LocalTime.of(16, 0), response.getEndTime());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when start time is after end time")
    void testCreateShiftTemplate_InvalidTimeRange_ThrowsException() {
        // Arrange
        validTemplateRequest.setStartTime(LocalTime.of(16, 0));
        validTemplateRequest.setEndTime(LocalTime.of(8, 0));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.createShiftTemplate(validTemplateRequest);
        });
        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when name is null")
    void testCreateShiftTemplate_NullName_ThrowsException() {
        // Arrange
        validTemplateRequest.setName(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.createShiftTemplate(validTemplateRequest);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when max employees is negative")
    void testCreateShiftTemplate_NegativeMaxEmployees_ThrowsException() {
        // Arrange
        validTemplateRequest.setMaxEmployees(-1);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.createShiftTemplate(validTemplateRequest);
        });
    }

    @Test
    @DisplayName("Should create shift template with overnight hours")
    void testCreateShiftTemplate_OvernightShift_Success() {
        // Arrange
        validTemplateRequest.setName("Night Shift");
        validTemplateRequest.setStartTime(LocalTime.of(22, 0));
        validTemplateRequest.setEndTime(LocalTime.of(6, 0)); // Next day
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateResponse response = shiftService.createShiftTemplate(validTemplateRequest);

        // Assert
        assertNotNull(response);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    // ========== UPDATE SHIFT TEMPLATE TESTS ==========

    @Test
    @DisplayName("Should update shift template with valid input")
    void testUpdateShiftTemplate_ValidInput_Success() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateResponse response = shiftService.updateShiftTemplate(1L, validTemplateRequest);

        // Assert
        assertNotNull(response);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when template not found")
    void testUpdateShiftTemplate_NonExistentId_ThrowsException() {
        // Arrange
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.updateShiftTemplate(999L, validTemplateRequest);
        });
    }

    // ========== DELETE SHIFT TEMPLATE TESTS ==========

    @Test
    @DisplayName("Should delete shift template successfully")
    void testDeleteShiftTemplate_ValidId_Success() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.countByShiftTemplate(validShiftTemplate)).thenReturn(0L);
        doNothing().when(shiftTemplateRepository).delete(validShiftTemplate);

        // Act
        shiftService.deleteShiftTemplate(1L);

        // Assert
        verify(shiftTemplateRepository, times(1)).delete(validShiftTemplate);
    }

    @Test
    @DisplayName("Should throw BusinessException when template has active assignments")
    void testDeleteShiftTemplate_HasActiveAssignments_ThrowsException() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.countByShiftTemplate(validShiftTemplate)).thenReturn(5L);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.deleteShiftTemplate(1L);
        });
        verify(shiftTemplateRepository, never()).delete(any(ShiftTemplate.class));
    }

    // ========== ASSIGN SHIFT TESTS ==========

    @Test
    @DisplayName("Should assign shift to employee with valid input")
    void testAssignShift_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndShiftDate(validEmployee, LocalDate.now()))
                .thenReturn(Arrays.asList());
        when(blackoutDateRepository.findByDate(LocalDate.now())).thenReturn(Optional.empty());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(validShiftAssignment);

        // Act
        ShiftAssignmentResponse response = shiftService.assignShift(validAssignmentRequest, "SUPERVISOR001");

        // Assert
        assertNotNull(response);
        assertEquals(ShiftAssignment.Status.SCHEDULED, response.getStatus());
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee not found")
    void testAssignShift_EmployeeNotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(validAssignmentRequest, "SUPERVISOR001");
        });
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when shift template not found")
    void testAssignShift_ShiftTemplateNotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(validAssignmentRequest, "SUPERVISOR001");
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when employee already has shift on date")
    void testAssignShift_ConflictingShift_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndShiftDate(validEmployee, LocalDate.now()))
                .thenReturn(Arrays.asList(validShiftAssignment));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(validAssignmentRequest, "SUPERVISOR001");
        });
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when date is blackout date")
    void testAssignShift_BlackoutDate_ThrowsException() {
        // Arrange
        BlackoutDate blackoutDate = BlackoutDate.builder()
                .id(1L)
                .date(LocalDate.now())
                .reason("Facility Closure")
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndShiftDate(validEmployee, LocalDate.now()))
                .thenReturn(Arrays.asList());
        when(blackoutDateRepository.findByDate(LocalDate.now())).thenReturn(Optional.of(blackoutDate));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(validAssignmentRequest, "SUPERVISOR001");
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when employee is not active")
    void testAssignShift_InactiveEmployee_ThrowsException() {
        // Arrange
        validEmployee.setStatus(Employee.Status.INACTIVE);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(validAssignmentRequest, "SUPERVISOR001");
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when max employees reached")
    void testAssignShift_MaxEmployeesReached_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndShiftDate(validEmployee, LocalDate.now()))
                .thenReturn(Arrays.asList());
        when(blackoutDateRepository.findByDate(LocalDate.now())).thenReturn(Optional.empty());
        when(shiftAssignmentRepository.countByShiftTemplateAndDate(validShiftTemplate, LocalDate.now()))
                .thenReturn(10L); // Max employees reached

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(validAssignmentRequest, "SUPERVISOR001");
        });
    }

    // ========== BULK ASSIGN SHIFTS TESTS ==========

    @Test
    @DisplayName("Should bulk assign shifts to multiple employees")
    void testBulkAssignShifts_ValidInput_Success() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        Employee employee2 = Employee.builder().id(2L).name("Jane Smith").badgeId("EMP67890")
                .status(Employee.Status.ACTIVE).build();
        Employee employee3 = Employee.builder().id(3L).name("Bob Johnson").badgeId("EMP11111")
                .status(Employee.Status.ACTIVE).build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee2));
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee3));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndShiftDate(any(), any())).thenReturn(Arrays.asList());
        when(blackoutDateRepository.findByDate(any())).thenReturn(Optional.empty());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(validShiftAssignment);

        // Act
        List<ShiftAssignmentResponse> responses = shiftService.bulkAssignShifts(
                employeeIds, 1L, LocalDate.now(), "SUPERVISOR001");

        // Assert
        assertNotNull(responses);
        assertEquals(3, responses.size());
        verify(shiftAssignmentRepository, times(3)).save(any(ShiftAssignment.class));
    }

    // ========== UNASSIGN SHIFT TESTS ==========

    @Test
    @DisplayName("Should unassign shift successfully")
    void testUnassignShift_ValidId_Success() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(validShiftAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(validShiftAssignment);

        // Act
        shiftService.unassignShift(1L, "SUPERVISOR001");

        // Assert
        verify(shiftAssignmentRepository, times(1)).save(argThat(assignment -> 
            assignment.getStatus() == ShiftAssignment.Status.CANCELLED
        ));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when assignment not found")
    void testUnassignShift_NonExistentId_ThrowsException() {
        // Arrange
        when(shiftAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.unassignShift(999L, "SUPERVISOR001");
        });
    }

    // ========== GET EMPLOYEE SCHEDULE TESTS ==========

    @Test
    @DisplayName("Should retrieve employee schedule for date range")
    void testGetEmployeeSchedule_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        List<ShiftAssignment> assignments = Arrays.asList(validShiftAssignment);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftAssignmentRepository.findByEmployeeAndDateRange(validEmployee, startDate, endDate))
                .thenReturn(assignments);

        // Act
        List<ShiftAssignmentResponse> responses = shiftService.getEmployeeSchedule(1L, startDate, endDate);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    // ========== BLACKOUT DATE TESTS ==========

    @Test
    @DisplayName("Should create blackout date successfully")
    void testCreateBlackoutDate_ValidInput_Success() {
        // Arrange
        BlackoutDate blackoutDate = BlackoutDate.builder()
                .id(1L)
                .date(LocalDate.now().plusDays(30))
                .reason("Holiday")
                .build();
        when(blackoutDateRepository.save(any(BlackoutDate.class))).thenReturn(blackoutDate);

        // Act
        BlackoutDateResponse response = shiftService.createBlackoutDate(
                LocalDate.now().plusDays(30), "Holiday");

        // Assert
        assertNotNull(response);
        assertEquals("Holiday", response.getReason());
    }

    @Test
    @DisplayName("Should throw BusinessException when blackout date is in past")
    void testCreateBlackoutDate_PastDate_ThrowsException() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.createBlackoutDate(LocalDate.now().minusDays(1), "Past Holiday");
        });
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle shift assignment on leap year date")
    void testAssignShift_LeapYearDate_Success() {
        // Arrange
        validAssignmentRequest.setShiftDate(LocalDate.of(2024, 2, 29));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndShiftDate(any(), any())).thenReturn(Arrays.asList());
        when(blackoutDateRepository.findByDate(any())).thenReturn(Optional.empty());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(validShiftAssignment);

        // Act
        ShiftAssignmentResponse response = shiftService.assignShift(validAssignmentRequest, "SUPERVISOR001");

        // Assert
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should handle shift template with zero max employees")
    void testCreateShiftTemplate_ZeroMaxEmployees_Success() {
        // Arrange
        validTemplateRequest.setMaxEmployees(0);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateResponse response = shiftService.createShiftTemplate(validTemplateRequest);

        // Assert
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should handle shift assignment far in future")
    void testAssignShift_FarFutureDate_Success() {
        // Arrange
        validAssignmentRequest.setShiftDate(LocalDate.now().plusYears(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndShiftDate(any(), any())).thenReturn(Arrays.asList());
        when(blackoutDateRepository.findByDate(any())).thenReturn(Optional.empty());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(validShiftAssignment);

        // Act
        ShiftAssignmentResponse response = shiftService.assignShift(validAssignmentRequest, "SUPERVISOR001");

        // Assert
        assertNotNull(response);
    }
}