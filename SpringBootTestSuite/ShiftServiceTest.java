package com.warehouse.ems.scheduling.service;

import com.warehouse.ems.scheduling.entity.ShiftTemplate;
import com.warehouse.ems.scheduling.entity.ShiftAssignment;
import com.warehouse.ems.scheduling.repository.ShiftTemplateRepository;
import com.warehouse.ems.scheduling.repository.ShiftAssignmentRepository;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ShiftService covering:
 * - Shift template CRUD operations
 * - Shift assignments
 * - Conflict detection
 * - Bulk assignments
 * - Edge cases and boundary conditions
 */
@ExtendWith(MockitoExtension.class)
public class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate morningShift;
    private ShiftTemplate eveningShift;
    private ShiftAssignment assignment;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test data
        morningShift = ShiftTemplate.builder()
                .id(1L)
                .name("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .recurrencePattern("DAILY")
                .overtimeRule("AFTER_8_HOURS")
                .build();

        eveningShift = ShiftTemplate.builder()
                .id(2L)
                .name("Evening Shift")
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(0, 0))
                .recurrencePattern("DAILY")
                .overtimeRule("AFTER_8_HOURS")
                .build();

        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .status("ACTIVE")
                .deleted(false)
                .build();

        assignment = ShiftAssignment.builder()
                .id(1L)
                .employeeId(1L)
                .shiftTemplateId(1L)
                .assignmentDate(LocalDate.of(2024, 1, 15))
                .status("ASSIGNED")
                .build();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    public void testCreateShiftTemplate_WithValidData_ReturnsCreatedTemplate() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(morningShift);

        // Act
        ShiftTemplate result = shiftService.createTemplate(morningShift);

        // Assert
        assertNotNull(result, "Created shift template should not be null");
        assertEquals("Morning Shift", result.getName(), "Shift name should match");
        assertEquals(LocalTime.of(8, 0), result.getStartTime(), "Start time should match");
        assertEquals(LocalTime.of(16, 0), result.getEndTime(), "End time should match");
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    public void testAssignShift_WithValidData_ReturnsAssignment() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(morningShift));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(assignment);

        // Act
        ShiftAssignment result = shiftService.assignShift(1L, 1L, LocalDate.of(2024, 1, 15));

        // Assert
        assertNotNull(result, "Assignment should not be null");
        assertEquals(1L, result.getEmployeeId(), "Employee ID should match");
        assertEquals(1L, result.getShiftTemplateId(), "Shift template ID should match");
        assertEquals("ASSIGNED", result.getStatus(), "Status should be ASSIGNED");
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    public void testGetEmployeeShifts_ReturnsAllAssignments() {
        // Arrange
        ShiftAssignment assignment2 = ShiftAssignment.builder()
                .id(2L)
                .employeeId(1L)
                .shiftTemplateId(2L)
                .assignmentDate(LocalDate.of(2024, 1, 16))
                .status("ASSIGNED")
                .build();

        when(shiftAssignmentRepository.findByEmployeeIdOrderByAssignmentDateAsc(1L))
                .thenReturn(Arrays.asList(assignment, assignment2));

        // Act
        List<ShiftAssignment> result = shiftService.getEmployeeShifts(1L);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 assignments");
        assertEquals(LocalDate.of(2024, 1, 15), result.get(0).getAssignmentDate());
        assertEquals(LocalDate.of(2024, 1, 16), result.get(1).getAssignmentDate());
        verify(shiftAssignmentRepository, times(1)).findByEmployeeIdOrderByAssignmentDateAsc(1L);
    }

    @Test
    public void testUpdateShiftTemplate_WithValidData_ReturnsUpdatedTemplate() {
        // Arrange
        ShiftTemplate updatedShift = ShiftTemplate.builder()
                .id(1L)
                .name("Updated Morning Shift")
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(15, 0))
                .recurrencePattern("DAILY")
                .overtimeRule("AFTER_8_HOURS")
                .build();

        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(morningShift));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(updatedShift);

        // Act
        ShiftTemplate result = shiftService.updateTemplate(1L, updatedShift);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Morning Shift", result.getName());
        assertEquals(LocalTime.of(7, 0), result.getStartTime());
        verify(shiftTemplateRepository, times(1)).findById(1L);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testAssignShift_WithInvalidEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(999L, 1L, LocalDate.of(2024, 1, 15));
        }, "Should throw exception for invalid employee ID");

        verify(employeeRepository, times(1)).findById(999L);
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    public void testAssignShift_WithInvalidShiftTemplateId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 999L, LocalDate.of(2024, 1, 15));
        }, "Should throw exception for invalid shift template ID");

        verify(shiftTemplateRepository, times(1)).findById(999L);
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    public void testAssignShift_WithConflictingAssignment_ThrowsException() {
        // Arrange
        ShiftAssignment conflictingAssignment = ShiftAssignment.builder()
                .id(2L)
                .employeeId(1L)
                .shiftTemplateId(2L)
                .assignmentDate(LocalDate.of(2024, 1, 15))
                .status("ASSIGNED")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(morningShift));
        when(shiftAssignmentRepository.findConflictingAssignments(1L, LocalDate.of(2024, 1, 15)))
                .thenReturn(Arrays.asList(conflictingAssignment));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            shiftService.assignShift(1L, 1L, LocalDate.of(2024, 1, 15));
        }, "Should throw exception for conflicting shift assignment");

        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    public void testAssignShift_WithDeletedEmployee_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            shiftService.assignShift(1L, 1L, LocalDate.of(2024, 1, 15));
        }, "Should throw exception for deleted employee");

        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    public void testAssignShift_WithInactiveEmployee_ThrowsException() {
        // Arrange
        testEmployee.setStatus("INACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            shiftService.assignShift(1L, 1L, LocalDate.of(2024, 1, 15));
        }, "Should throw exception for inactive employee");

        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    public void testCreateShiftTemplate_WithNullName_ThrowsException() {
        // Arrange
        ShiftTemplate invalidShift = ShiftTemplate.builder()
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createTemplate(invalidShift);
        }, "Should throw exception for null shift name");

        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    public void testCreateShiftTemplate_WithEmptyName_ThrowsException() {
        // Arrange
        ShiftTemplate invalidShift = ShiftTemplate.builder()
                .name("")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createTemplate(invalidShift);
        }, "Should throw exception for empty shift name");

        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    public void testCreateShiftTemplate_WithEndTimeBeforeStartTime_ThrowsException() {
        // Arrange
        ShiftTemplate invalidShift = ShiftTemplate.builder()
                .name("Invalid Shift")
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(8, 0))
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createTemplate(invalidShift);
        }, "Should throw exception when end time is before start time");

        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testAssignShift_ForPastDate_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(morningShift));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 1L, LocalDate.of(2020, 1, 1));
        }, "Should throw exception for past date assignment");

        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    public void testAssignShift_ForToday_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(morningShift));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(assignment);

        // Act
        ShiftAssignment result = shiftService.assignShift(1L, 1L, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getAssignmentDate());
    }

    @Test
    public void testCreateShiftTemplate_WithMidnightStartTime_Success() {
        // Arrange
        ShiftTemplate nightShift = ShiftTemplate.builder()
                .name("Night Shift")
                .startTime(LocalTime.of(0, 0))
                .endTime(LocalTime.of(8, 0))
                .recurrencePattern("DAILY")
                .build();

        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(nightShift);

        // Act
        ShiftTemplate result = shiftService.createTemplate(nightShift);

        // Assert
        assertNotNull(result);
        assertEquals(LocalTime.of(0, 0), result.getStartTime());
    }

    @Test
    public void testBulkAssignShifts_WithValidData_ReturnsAllAssignments() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        Employee emp2 = Employee.builder().id(2L).badgeId("EMP002").status("ACTIVE").deleted(false).build();
        Employee emp3 = Employee.builder().id(3L).badgeId("EMP003").status("ACTIVE").deleted(false).build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(emp2));
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(emp3));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(morningShift));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(assignment);

        // Act
        List<ShiftAssignment> result = shiftService.bulkAssignShifts(employeeIds, 1L, LocalDate.of(2024, 1, 15));

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(shiftAssignmentRepository, times(3)).save(any(ShiftAssignment.class));
    }

    @Test
    public void testGetEmployeeShifts_WithNoAssignments_ReturnsEmptyList() {
        // Arrange
        when(shiftAssignmentRepository.findByEmployeeIdOrderByAssignmentDateAsc(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<ShiftAssignment> result = shiftService.getEmployeeShifts(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(shiftAssignmentRepository, times(1)).findByEmployeeIdOrderByAssignmentDateAsc(1L);
    }
}