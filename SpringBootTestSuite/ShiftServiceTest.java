package com.company.warehouse.core.service;

import com.company.warehouse.core.domain.ShiftTemplate;
import com.company.warehouse.core.domain.ShiftAssignment;
import com.company.warehouse.core.domain.ShiftAssignment.Status;
import com.company.warehouse.core.domain.Employee;
import com.company.warehouse.core.domain.Warehouse;
import com.company.warehouse.core.repository.ShiftTemplateRepository;
import com.company.warehouse.core.repository.ShiftAssignmentRepository;
import com.company.warehouse.core.repository.EmployeeRepository;
import com.company.warehouse.api.dto.ShiftTemplateDTO;
import com.company.warehouse.api.dto.ShiftAssignmentDTO;
import com.company.warehouse.api.exception.ResourceNotFoundException;
import com.company.warehouse.api.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ShiftService.
 * Tests cover shift templates, assignments, conflict detection, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Shift Service Tests")
class ShiftServiceTest {

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
    private Warehouse testWarehouse;
    private ShiftTemplateDTO testShiftTemplateDTO;
    private ShiftAssignmentDTO testShiftAssignmentDTO;

    @BeforeEach
    void setUp() {
        testWarehouse = Warehouse.builder()
                .id(1L)
                .name("Warehouse A")
                .code("WH-A")
                .build();

        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .build();

        Set<DayOfWeek> daysOfWeek = new HashSet<>(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        
        testShiftTemplate = ShiftTemplate.builder()
                .id(1L)
                .name("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .daysOfWeek(daysOfWeek)
                .warehouse(testWarehouse)
                .build();

        testShiftAssignment = ShiftAssignment.builder()
                .id(1L)
                .employee(testEmployee)
                .shiftTemplate(testShiftTemplate)
                .date(LocalDate.now())
                .status(Status.ASSIGNED)
                .build();

        testShiftTemplateDTO = ShiftTemplateDTO.builder()
                .name("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .daysOfWeek(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"))
                .warehouseId(1L)
                .build();

        testShiftAssignmentDTO = ShiftAssignmentDTO.builder()
                .employeeId(1L)
                .shiftTemplateId(1L)
                .date(LocalDate.now())
                .build();
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    @DisplayName("Should create shift template with valid data")
    void testCreateShiftTemplate_ValidData_Success() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(testShiftTemplateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Should throw exception when creating shift template with null name")
    void testCreateShiftTemplate_NullName_ThrowsException() {
        // Arrange
        testShiftTemplateDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(testShiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Should throw exception when end time before start time")
    void testCreateShiftTemplate_EndTimeBeforeStartTime_ThrowsException() {
        // Arrange
        testShiftTemplateDTO.setStartTime(LocalTime.of(16, 0));
        testShiftTemplateDTO.setEndTime(LocalTime.of(8, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(testShiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Should handle overnight shift (end time next day)")
    void testCreateShiftTemplate_OvernightShift_Success() {
        // Arrange
        testShiftTemplateDTO.setStartTime(LocalTime.of(22, 0));
        testShiftTemplateDTO.setEndTime(LocalTime.of(6, 0));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(testShiftTemplateDTO);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Should create shift template with all days of week")
    void testCreateShiftTemplate_AllDaysOfWeek_Success() {
        // Arrange
        testShiftTemplateDTO.setDaysOfWeek(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(testShiftTemplateDTO);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Should throw exception when creating shift template with empty days of week")
    void testCreateShiftTemplate_EmptyDaysOfWeek_ThrowsException() {
        // Arrange
        testShiftTemplateDTO.setDaysOfWeek(Arrays.asList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(testShiftTemplateDTO);
        });
    }

    // ========== ASSIGN SHIFT TESTS ==========

    @Test
    @DisplayName("Should assign shift to employee with valid data")
    void testAssignShift_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndDate(testEmployee, LocalDate.now())).thenReturn(Optional.empty());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftAssignmentDTO result = shiftService.assignShift(testShiftAssignmentDTO);

        // Assert
        assertNotNull(result);
        assertEquals(Status.ASSIGNED.name(), result.getStatus());
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Should throw exception when assigning shift to non-existent employee")
    void testAssignShift_EmployeeNotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        testShiftAssignmentDTO.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(testShiftAssignmentDTO);
        });
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Should throw exception when assigning non-existent shift template")
    void testAssignShift_ShiftTemplateNotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());
        testShiftAssignmentDTO.setShiftTemplateId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(testShiftAssignmentDTO);
        });
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Should detect conflict when employee already has shift on same date")
    void testAssignShift_ConflictDetected_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(Optional.of(testShiftAssignment));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(testShiftAssignmentDTO);
        });
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Should throw exception when assigning shift for past date")
    void testAssignShift_PastDate_ThrowsException() {
        // Arrange
        testShiftAssignmentDTO.setDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(testShiftAssignmentDTO);
        });
    }

    // ========== BULK ASSIGN TESTS ==========

    @Test
    @DisplayName("Should bulk assign shifts to multiple employees")
    void testBulkAssignShifts_ValidData_Success() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        Employee employee2 = Employee.builder().id(2L).name("Jane Doe").badgeId("EMP002").build();
        Employee employee3 = Employee.builder().id(3L).name("Bob Smith").badgeId("EMP003").build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee2));
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee3));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndDate(any(), any())).thenReturn(Optional.empty());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        List<ShiftAssignmentDTO> result = shiftService.bulkAssignShifts(employeeIds, 1L, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(shiftAssignmentRepository, times(3)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Should skip employees with conflicts during bulk assign")
    void testBulkAssignShifts_WithConflicts_SkipsConflicts() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L);
        Employee employee2 = Employee.builder().id(2L).name("Jane Doe").badgeId("EMP002").build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee2));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(Optional.of(testShiftAssignment));
        when(shiftAssignmentRepository.findByEmployeeAndDate(employee2, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        List<ShiftAssignmentDTO> result = shiftService.bulkAssignShifts(employeeIds, 1L, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Should throw exception when bulk assigning with empty employee list")
    void testBulkAssignShifts_EmptyEmployeeList_ThrowsException() {
        // Arrange
        List<Long> employeeIds = Arrays.asList();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(employeeIds, 1L, LocalDate.now());
        });
    }

    // ========== GET EMPLOYEE SCHEDULE TESTS ==========

    @Test
    @DisplayName("Should get employee schedule for date range")
    void testGetEmployeeSchedule_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftAssignmentRepository.findByEmployeeAndDateBetween(testEmployee, startDate, endDate))
                .thenReturn(Arrays.asList(testShiftAssignment));

        // Act
        List<ShiftAssignmentDTO> result = shiftService.getEmployeeSchedule(1L, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(shiftAssignmentRepository, times(1)).findByEmployeeAndDateBetween(testEmployee, startDate, endDate);
    }

    @Test
    @DisplayName("Should return empty schedule when no assignments found")
    void testGetEmployeeSchedule_NoAssignments_ReturnsEmptyList() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftAssignmentRepository.findByEmployeeAndDateBetween(testEmployee, startDate, endDate))
                .thenReturn(Arrays.asList());

        // Act
        List<ShiftAssignmentDTO> result = shiftService.getEmployeeSchedule(1L, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when end date before start date")
    void testGetEmployeeSchedule_EndDateBeforeStartDate_ThrowsException() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.getEmployeeSchedule(1L, startDate, endDate);
        });
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    @DisplayName("Should detect overlapping shift times")
    void testDetectConflicts_OverlappingShifts_DetectsConflict() {
        // Arrange
        ShiftTemplate overlappingShift = ShiftTemplate.builder()
                .id(2L)
                .name("Afternoon Shift")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(22, 0))
                .build();
        
        ShiftAssignment existingAssignment = ShiftAssignment.builder()
                .employee(testEmployee)
                .shiftTemplate(testShiftTemplate)
                .date(LocalDate.now())
                .build();

        when(shiftAssignmentRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(Optional.of(existingAssignment));

        // Act
        boolean hasConflict = shiftService.hasConflict(testEmployee, overlappingShift, LocalDate.now());

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    @DisplayName("Should not detect conflict for non-overlapping shifts")
    void testDetectConflicts_NonOverlappingShifts_NoConflict() {
        // Arrange
        ShiftTemplate nonOverlappingShift = ShiftTemplate.builder()
                .id(2L)
                .name("Night Shift")
                .startTime(LocalTime.of(22, 0))
                .endTime(LocalTime.of(6, 0))
                .build();
        
        when(shiftAssignmentRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(Optional.empty());

        // Act
        boolean hasConflict = shiftService.hasConflict(testEmployee, nonOverlappingShift, LocalDate.now());

        // Assert
        assertFalse(hasConflict);
    }

    // ========== BLACKOUT DATE TESTS ==========

    @Test
    @DisplayName("Should prevent shift assignment on blackout date")
    void testAssignShift_BlackoutDate_ThrowsException() {
        // Arrange
        LocalDate blackoutDate = LocalDate.now().plusDays(1);
        testShiftAssignmentDTO.setDate(blackoutDate);
        when(shiftService.isBlackoutDate(blackoutDate)).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(testShiftAssignmentDTO);
        });
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle shift assignment for weekend")
    void testAssignShift_Weekend_Success() {
        // Arrange
        LocalDate saturday = LocalDate.now().with(DayOfWeek.SATURDAY);
        testShiftAssignmentDTO.setDate(saturday);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndDate(testEmployee, saturday)).thenReturn(Optional.empty());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftAssignmentDTO result = shiftService.assignShift(testShiftAssignmentDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle shift assignment for far future date")
    void testAssignShift_FarFutureDate_Success() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusMonths(6);
        testShiftAssignmentDTO.setDate(futureDate);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findByEmployeeAndDate(testEmployee, futureDate)).thenReturn(Optional.empty());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftAssignmentDTO result = shiftService.assignShift(testShiftAssignmentDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle 24-hour shift")
    void testCreateShiftTemplate_24HourShift_Success() {
        // Arrange
        testShiftTemplateDTO.setStartTime(LocalTime.of(0, 0));
        testShiftTemplateDTO.setEndTime(LocalTime.of(23, 59));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(testShiftTemplateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle shift cancellation")
    void testCancelShift_ValidAssignment_Success() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(testShiftAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        shiftService.cancelShift(1L);

        // Assert
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-existent shift")
    void testCancelShift_NotFound_ThrowsException() {
        // Arrange
        when(shiftAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.cancelShift(999L);
        });
    }
}