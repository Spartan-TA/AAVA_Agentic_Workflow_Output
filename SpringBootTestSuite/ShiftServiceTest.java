package com.wms.scheduling.service;

import com.wms.scheduling.domain.ShiftTemplate;
import com.wms.scheduling.domain.ShiftAssignment;
import com.wms.scheduling.domain.ShiftStatus;
import com.wms.scheduling.dto.ShiftTemplateDto;
import com.wms.scheduling.dto.ShiftAssignmentDto;
import com.wms.scheduling.dto.BulkAssignDto;
import com.wms.scheduling.repository.ShiftTemplateRepository;
import com.wms.scheduling.repository.ShiftAssignmentRepository;
import com.wms.employee.domain.Employee;
import com.wms.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
 * Tests cover shift templates, assignments, conflict detection, and edge cases
 */
@DisplayName("Shift Service Tests")
public class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftServiceImpl shiftService;

    private ShiftTemplate testShiftTemplate;
    private ShiftAssignment testShiftAssignment;
    private Employee testEmployee;
    private ShiftTemplateDto shiftTemplateDto;
    private ShiftAssignmentDto shiftAssignmentDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Setup test shift template
        testShiftTemplate = new ShiftTemplate();
        testShiftTemplate.setId(1L);
        testShiftTemplate.setName("Morning Shift");
        testShiftTemplate.setStartTime(LocalTime.of(8, 0));
        testShiftTemplate.setEndTime(LocalTime.of(17, 0));
        testShiftTemplate.setDepartment("Warehouse");
        testShiftTemplate.setRecurrence("DAILY");

        // Setup test shift assignment
        testShiftAssignment = new ShiftAssignment();
        testShiftAssignment.setId(1L);
        testShiftAssignment.setEmployee(testEmployee);
        testShiftAssignment.setShiftTemplate(testShiftTemplate);
        testShiftAssignment.setDate(LocalDate.now());
        testShiftAssignment.setStatus(ShiftStatus.SCHEDULED);

        // Setup DTOs
        shiftTemplateDto = new ShiftTemplateDto();
        shiftTemplateDto.setName("Morning Shift");
        shiftTemplateDto.setStartTime(LocalTime.of(8, 0));
        shiftTemplateDto.setEndTime(LocalTime.of(17, 0));
        shiftTemplateDto.setDepartment("Warehouse");
        shiftTemplateDto.setRecurrence("DAILY");

        shiftAssignmentDto = new ShiftAssignmentDto();
        shiftAssignmentDto.setEmployeeId(1L);
        shiftAssignmentDto.setShiftTemplateId(1L);
        shiftAssignmentDto.setDate(LocalDate.now());
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    @DisplayName("Test create shift template with valid data")
    public void testCreateShiftTemplate_ValidData_Success() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDto result = shiftService.createShiftTemplate(shiftTemplateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test create shift template with null name throws exception")
    public void testCreateShiftTemplate_NullName_ThrowsException() {
        // Arrange
        shiftTemplateDto.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    @Test
    @DisplayName("Test create shift template with null start time throws exception")
    public void testCreateShiftTemplate_NullStartTime_ThrowsException() {
        // Arrange
        shiftTemplateDto.setStartTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    @Test
    @DisplayName("Test create shift template with null end time throws exception")
    public void testCreateShiftTemplate_NullEndTime_ThrowsException() {
        // Arrange
        shiftTemplateDto.setEndTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    @Test
    @DisplayName("Test create shift template with end time before start time throws exception")
    public void testCreateShiftTemplate_EndBeforeStart_ThrowsException() {
        // Arrange
        shiftTemplateDto.setStartTime(LocalTime.of(17, 0));
        shiftTemplateDto.setEndTime(LocalTime.of(8, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    @Test
    @DisplayName("Test create shift template with invalid recurrence throws exception")
    public void testCreateShiftTemplate_InvalidRecurrence_ThrowsException() {
        // Arrange
        shiftTemplateDto.setRecurrence("INVALID");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    // ========== ASSIGN SHIFT TESTS ==========

    @Test
    @DisplayName("Test assign shift with valid data")
    public void testAssignShift_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftAssignmentDto result = shiftService.assignShift(shiftAssignmentDto);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test assign shift with null employee ID throws exception")
    public void testAssignShift_NullEmployeeId_ThrowsException() {
        // Arrange
        shiftAssignmentDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    @Test
    @DisplayName("Test assign shift with non-existent employee throws exception")
    public void testAssignShift_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    @Test
    @DisplayName("Test assign shift with non-existent template throws exception")
    public void testAssignShift_NonExistentTemplate_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    @Test
    @DisplayName("Test assign shift with null date throws exception")
    public void testAssignShift_NullDate_ThrowsException() {
        // Arrange
        shiftAssignmentDto.setDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    @Test
    @DisplayName("Test assign shift with past date throws exception")
    public void testAssignShift_PastDate_ThrowsException() {
        // Arrange
        shiftAssignmentDto.setDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    @DisplayName("Test detect shift conflict when conflict exists")
    public void testDetectConflict_ConflictExists_ReturnsTrue() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testShiftAssignment));

        // Act
        boolean result = shiftService.hasConflict(1L, LocalDate.now());

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Test detect shift conflict when no conflict")
    public void testDetectConflict_NoConflict_ReturnsFalse() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        boolean result = shiftService.hasConflict(1L, LocalDate.now());

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Test detect conflict with null employee ID throws exception")
    public void testDetectConflict_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.hasConflict(null, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Test detect conflict with null date throws exception")
    public void testDetectConflict_NullDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.hasConflict(1L, null);
        });
    }

    @Test
    @DisplayName("Test assign shift with conflict throws exception")
    public void testAssignShift_WithConflict_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testShiftAssignment));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    // ========== BULK ASSIGN TESTS ==========

    @Test
    @DisplayName("Test bulk assign shifts with valid data")
    public void testBulkAssignShifts_ValidData_Success() {
        // Arrange
        BulkAssignDto bulkAssignDto = new BulkAssignDto();
        bulkAssignDto.setEmployeeIds(Arrays.asList(1L, 2L));
        bulkAssignDto.setShiftTemplateId(1L);
        bulkAssignDto.setStartDate(LocalDate.now());
        bulkAssignDto.setEndDate(LocalDate.now().plusDays(7));

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        List<ShiftAssignmentDto> result = shiftService.bulkAssignShifts(bulkAssignDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    @DisplayName("Test bulk assign with null employee IDs throws exception")
    public void testBulkAssignShifts_NullEmployeeIds_ThrowsException() {
        // Arrange
        BulkAssignDto bulkAssignDto = new BulkAssignDto();
        bulkAssignDto.setEmployeeIds(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(bulkAssignDto);
        });
    }

    @Test
    @DisplayName("Test bulk assign with empty employee IDs throws exception")
    public void testBulkAssignShifts_EmptyEmployeeIds_ThrowsException() {
        // Arrange
        BulkAssignDto bulkAssignDto = new BulkAssignDto();
        bulkAssignDto.setEmployeeIds(Arrays.asList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(bulkAssignDto);
        });
    }

    @Test
    @DisplayName("Test bulk assign with null start date throws exception")
    public void testBulkAssignShifts_NullStartDate_ThrowsException() {
        // Arrange
        BulkAssignDto bulkAssignDto = new BulkAssignDto();
        bulkAssignDto.setEmployeeIds(Arrays.asList(1L));
        bulkAssignDto.setStartDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(bulkAssignDto);
        });
    }

    @Test
    @DisplayName("Test bulk assign with end date before start date throws exception")
    public void testBulkAssignShifts_EndBeforeStart_ThrowsException() {
        // Arrange
        BulkAssignDto bulkAssignDto = new BulkAssignDto();
        bulkAssignDto.setEmployeeIds(Arrays.asList(1L));
        bulkAssignDto.setStartDate(LocalDate.now());
        bulkAssignDto.setEndDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(bulkAssignDto);
        });
    }

    // ========== GET SHIFT ASSIGNMENTS TESTS ==========

    @Test
    @DisplayName("Test get shift assignments for employee")
    public void testGetShiftAssignments_ValidEmployee_Success() {
        // Arrange
        when(shiftAssignmentRepository.findByEmployeeId(1L))
                .thenReturn(Arrays.asList(testShiftAssignment));

        // Act
        List<ShiftAssignmentDto> result = shiftService.getShiftAssignmentsForEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get shift assignments with null employee ID throws exception")
    public void testGetShiftAssignments_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.getShiftAssignmentsForEmployee(null);
        });
    }

    @Test
    @DisplayName("Test get shift assignments for date range")
    public void testGetShiftAssignments_DateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(shiftAssignmentRepository.findByEmployeeIdAndDateBetween(1L, startDate, endDate))
                .thenReturn(Arrays.asList(testShiftAssignment));

        // Act
        List<ShiftAssignmentDto> result = shiftService.getShiftAssignmentsForEmployeeAndDateRange(1L, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== UPDATE SHIFT ASSIGNMENT TESTS ==========

    @Test
    @DisplayName("Test update shift assignment status")
    public void testUpdateShiftAssignmentStatus_ValidData_Success() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(testShiftAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftAssignmentDto result = shiftService.updateShiftAssignmentStatus(1L, ShiftStatus.COMPLETED);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test update shift assignment with null ID throws exception")
    public void testUpdateShiftAssignmentStatus_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.updateShiftAssignmentStatus(null, ShiftStatus.COMPLETED);
        });
    }

    @Test
    @DisplayName("Test update shift assignment with null status throws exception")
    public void testUpdateShiftAssignmentStatus_NullStatus_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.updateShiftAssignmentStatus(1L, null);
        });
    }

    // ========== DELETE SHIFT ASSIGNMENT TESTS ==========

    @Test
    @DisplayName("Test delete shift assignment with valid ID")
    public void testDeleteShiftAssignment_ValidId_Success() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(testShiftAssignment));
        doNothing().when(shiftAssignmentRepository).delete(any(ShiftAssignment.class));

        // Act
        shiftService.deleteShiftAssignment(1L);

        // Assert
        verify(shiftAssignmentRepository, times(1)).delete(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test delete shift assignment with null ID throws exception")
    public void testDeleteShiftAssignment_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.deleteShiftAssignment(null);
        });
    }

    @Test
    @DisplayName("Test delete non-existent shift assignment throws exception")
    public void testDeleteShiftAssignment_NonExistent_ThrowsException() {
        // Arrange
        when(shiftAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.deleteShiftAssignment(999L);
        });
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test create overnight shift template")
    public void testCreateShiftTemplate_OvernightShift_Success() {
        // Arrange
        shiftTemplateDto.setStartTime(LocalTime.of(22, 0));
        shiftTemplateDto.setEndTime(LocalTime.of(6, 0));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDto result = shiftService.createShiftTemplate(shiftTemplateDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create 24-hour shift template")
    public void testCreateShiftTemplate_24HourShift_Success() {
        // Arrange
        shiftTemplateDto.setStartTime(LocalTime.of(0, 0));
        shiftTemplateDto.setEndTime(LocalTime.of(23, 59));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDto result = shiftService.createShiftTemplate(shiftTemplateDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test assign shift for today")
    public void testAssignShift_Today_Success() {
        // Arrange
        shiftAssignmentDto.setDate(LocalDate.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftAssignmentDto result = shiftService.assignShift(shiftAssignmentDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test bulk assign for maximum date range")
    public void testBulkAssignShifts_MaxDateRange_Success() {
        // Arrange
        BulkAssignDto bulkAssignDto = new BulkAssignDto();
        bulkAssignDto.setEmployeeIds(Arrays.asList(1L));
        bulkAssignDto.setShiftTemplateId(1L);
        bulkAssignDto.setStartDate(LocalDate.now());
        bulkAssignDto.setEndDate(LocalDate.now().plusDays(365));

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        List<ShiftAssignmentDto> result = shiftService.bulkAssignShifts(bulkAssignDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get shift assignments returns empty list when no assignments")
    public void testGetShiftAssignments_NoAssignments_ReturnsEmptyList() {
        // Arrange
        when(shiftAssignmentRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<ShiftAssignmentDto> result = shiftService.getShiftAssignmentsForEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}