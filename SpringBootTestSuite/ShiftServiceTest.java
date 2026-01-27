package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.ShiftTemplate;
import com.warehouse.employee.domain.EmployeeShiftAssignment;
import com.warehouse.employee.dto.ShiftTemplateDTO;
import com.warehouse.employee.dto.ShiftAssignmentRequest;
import com.warehouse.employee.repository.ShiftTemplateRepository;
import com.warehouse.employee.repository.EmployeeShiftAssignmentRepository;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftService
 * Tests cover shift templates, assignments, rotations, and conflict detection
 */
@DisplayName("Shift Service Tests")
public class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private EmployeeShiftAssignmentRepository assignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate dayShiftTemplate;
    private ShiftTemplate nightShiftTemplate;
    private Employee testEmployee;
    private ShiftTemplateDTO shiftTemplateDTO;
    private ShiftAssignmentRequest assignmentRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup day shift template
        dayShiftTemplate = new ShiftTemplate();
        dayShiftTemplate.setId(1L);
        dayShiftTemplate.setName("Day Shift");
        dayShiftTemplate.setStartTime(LocalTime.of(9, 0));
        dayShiftTemplate.setEndTime(LocalTime.of(17, 0));
        dayShiftTemplate.setRecurring(true);

        // Setup night shift template
        nightShiftTemplate = new ShiftTemplate();
        nightShiftTemplate.setId(2L);
        nightShiftTemplate.setName("Night Shift");
        nightShiftTemplate.setStartTime(LocalTime.of(22, 0));
        nightShiftTemplate.setEndTime(LocalTime.of(6, 0));
        nightShiftTemplate.setRecurring(true);

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Setup shift template DTO
        shiftTemplateDTO = new ShiftTemplateDTO();
        shiftTemplateDTO.setName("Day Shift");
        shiftTemplateDTO.setStartTime(LocalTime.of(9, 0));
        shiftTemplateDTO.setEndTime(LocalTime.of(17, 0));
        shiftTemplateDTO.setRecurring(true);

        // Setup assignment request
        assignmentRequest = new ShiftAssignmentRequest();
        assignmentRequest.setEmployeeId(1L);
        assignmentRequest.setShiftTemplateId(1L);
        assignmentRequest.setDate(LocalDate.now());
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    @DisplayName("Test create shift template - valid data - success")
    public void testCreateShiftTemplate_ValidData_Success() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(dayShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(shiftTemplateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Day Shift", result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test create shift template - null name - throws exception")
    public void testCreateShiftTemplate_NullName_ThrowsException() {
        // Arrange
        shiftTemplateDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Test create shift template - empty name - throws exception")
    public void testCreateShiftTemplate_EmptyName_ThrowsException() {
        // Arrange
        shiftTemplateDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Test create shift template - null start time - throws exception")
    public void testCreateShiftTemplate_NullStartTime_ThrowsException() {
        // Arrange
        shiftTemplateDTO.setStartTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Test create shift template - null end time - throws exception")
    public void testCreateShiftTemplate_NullEndTime_ThrowsException() {
        // Arrange
        shiftTemplateDTO.setEndTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Test create shift template - end time before start time - throws exception")
    public void testCreateShiftTemplate_EndTimeBeforeStartTime_ThrowsException() {
        // Arrange
        shiftTemplateDTO.setStartTime(LocalTime.of(17, 0));
        shiftTemplateDTO.setEndTime(LocalTime.of(9, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Test create shift template - overnight shift - success")
    public void testCreateShiftTemplate_OvernightShift_Success() {
        // Arrange
        shiftTemplateDTO.setName("Night Shift");
        shiftTemplateDTO.setStartTime(LocalTime.of(22, 0));
        shiftTemplateDTO.setEndTime(LocalTime.of(6, 0));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(nightShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(shiftTemplateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Night Shift", result.getName());
    }

    // ========== ASSIGN SHIFT TESTS ==========

    @Test
    @DisplayName("Test assign shift - valid data - success")
    public void testAssignShift_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(dayShiftTemplate));
        when(assignmentRepository.save(any(EmployeeShiftAssignment.class)))
                .thenReturn(new EmployeeShiftAssignment());

        // Act
        shiftService.assignShift(1L, 1L, LocalDate.now());

        // Assert
        verify(assignmentRepository, times(1)).save(any(EmployeeShiftAssignment.class));
    }

    @Test
    @DisplayName("Test assign shift - null employee ID - throws exception")
    public void testAssignShift_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(null, 1L, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Test assign shift - null shift template ID - throws exception")
    public void testAssignShift_NullShiftTemplateId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, null, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Test assign shift - null date - throws exception")
    public void testAssignShift_NullDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 1L, null);
        });
    }

    @Test
    @DisplayName("Test assign shift - non-existent employee - throws exception")
    public void testAssignShift_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(999L, 1L, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Test assign shift - non-existent shift template - throws exception")
    public void testAssignShift_NonExistentShiftTemplate_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 999L, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Test assign shift - past date - throws exception")
    public void testAssignShift_PastDate_ThrowsException() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusDays(30);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 1L, pastDate);
        });
    }

    @Test
    @DisplayName("Test assign shift - future date - success")
    public void testAssignShift_FutureDate_Success() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(dayShiftTemplate));
        when(assignmentRepository.save(any(EmployeeShiftAssignment.class)))
                .thenReturn(new EmployeeShiftAssignment());

        // Act
        shiftService.assignShift(1L, 1L, futureDate);

        // Assert
        verify(assignmentRepository, times(1)).save(any(EmployeeShiftAssignment.class));
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    @DisplayName("Test detect shift conflict - overlapping shifts - returns true")
    public void testDetectShiftConflict_OverlappingShifts_ReturnsTrue() {
        // Arrange
        EmployeeShiftAssignment existingAssignment = new EmployeeShiftAssignment();
        existingAssignment.setEmployee(testEmployee);
        existingAssignment.setShiftTemplate(dayShiftTemplate);
        existingAssignment.setDate(LocalDate.now());

        when(assignmentRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(Arrays.asList(existingAssignment));

        // Act
        boolean hasConflict = shiftService.detectShiftConflict(testEmployee, dayShiftTemplate, LocalDate.now());

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    @DisplayName("Test detect shift conflict - no conflict - returns false")
    public void testDetectShiftConflict_NoConflict_ReturnsFalse() {
        // Arrange
        when(assignmentRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(Arrays.asList());

        // Act
        boolean hasConflict = shiftService.detectShiftConflict(testEmployee, dayShiftTemplate, LocalDate.now());

        // Assert
        assertFalse(hasConflict);
    }

    @Test
    @DisplayName("Test detect shift conflict - different dates - returns false")
    public void testDetectShiftConflict_DifferentDates_ReturnsFalse() {
        // Arrange
        EmployeeShiftAssignment existingAssignment = new EmployeeShiftAssignment();
        existingAssignment.setEmployee(testEmployee);
        existingAssignment.setShiftTemplate(dayShiftTemplate);
        existingAssignment.setDate(LocalDate.now().minusDays(1));

        when(assignmentRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(Arrays.asList());

        // Act
        boolean hasConflict = shiftService.detectShiftConflict(testEmployee, dayShiftTemplate, LocalDate.now());

        // Assert
        assertFalse(hasConflict);
    }

    // ========== GET EMPLOYEE SHIFTS TESTS ==========

    @Test
    @DisplayName("Test get employee shifts - success")
    public void testGetEmployeeShifts_Success() {
        // Arrange
        EmployeeShiftAssignment assignment1 = new EmployeeShiftAssignment();
        assignment1.setEmployee(testEmployee);
        assignment1.setShiftTemplate(dayShiftTemplate);
        assignment1.setDate(LocalDate.now());

        EmployeeShiftAssignment assignment2 = new EmployeeShiftAssignment();
        assignment2.setEmployee(testEmployee);
        assignment2.setShiftTemplate(nightShiftTemplate);
        assignment2.setDate(LocalDate.now().plusDays(1));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(assignmentRepository.findByEmployeeAndDateBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(assignment1, assignment2));

        // Act
        List<EmployeeShiftAssignment> shifts = shiftService.getEmployeeShifts(
                1L, LocalDate.now(), LocalDate.now().plusDays(7));

        // Assert
        assertNotNull(shifts);
        assertEquals(2, shifts.size());
    }

    @Test
    @DisplayName("Test get employee shifts - no shifts - returns empty list")
    public void testGetEmployeeShifts_NoShifts_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(assignmentRepository.findByEmployeeAndDateBetween(any(), any(), any()))
                .thenReturn(Arrays.asList());

        // Act
        List<EmployeeShiftAssignment> shifts = shiftService.getEmployeeShifts(
                1L, LocalDate.now(), LocalDate.now().plusDays(7));

        // Assert
        assertNotNull(shifts);
        assertTrue(shifts.isEmpty());
    }

    // ========== BLACKOUT DATE TESTS ==========

    @Test
    @DisplayName("Test assign shift on blackout date - throws exception")
    public void testAssignShift_BlackoutDate_ThrowsException() {
        // Arrange
        LocalDate blackoutDate = LocalDate.of(2024, 12, 25);
        when(shiftService.isBlackoutDate(blackoutDate)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 1L, blackoutDate);
        });
    }

    @Test
    @DisplayName("Test is blackout date - holiday - returns true")
    public void testIsBlackoutDate_Holiday_ReturnsTrue() {
        // Arrange
        LocalDate holiday = LocalDate.of(2024, 12, 25);

        // Act
        boolean isBlackout = shiftService.isBlackoutDate(holiday);

        // Assert
        assertTrue(isBlackout);
    }

    @Test
    @DisplayName("Test is blackout date - regular day - returns false")
    public void testIsBlackoutDate_RegularDay_ReturnsFalse() {
        // Arrange
        LocalDate regularDay = LocalDate.of(2024, 6, 15);

        // Act
        boolean isBlackout = shiftService.isBlackoutDate(regularDay);

        // Assert
        assertFalse(isBlackout);
    }

    // ========== BULK ASSIGNMENT TESTS ==========

    @Test
    @DisplayName("Test bulk assign shifts - success")
    public void testBulkAssignShifts_Success() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(dayShiftTemplate));
        when(assignmentRepository.save(any(EmployeeShiftAssignment.class)))
                .thenReturn(new EmployeeShiftAssignment());

        // Act
        shiftService.bulkAssignShifts(employeeIds, 1L, LocalDate.now());

        // Assert
        verify(assignmentRepository, times(3)).save(any(EmployeeShiftAssignment.class));
    }

    @Test
    @DisplayName("Test bulk assign shifts - empty employee list - throws exception")
    public void testBulkAssignShifts_EmptyList_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(Arrays.asList(), 1L, LocalDate.now());
        });
    }

    // ========== SHIFT ROTATION TESTS ==========

    @Test
    @DisplayName("Test create shift rotation - success")
    public void testCreateShiftRotation_Success() {
        // Arrange
        List<Long> shiftTemplateIds = Arrays.asList(1L, 2L);
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(dayShiftTemplate));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(assignmentRepository.save(any(EmployeeShiftAssignment.class)))
                .thenReturn(new EmployeeShiftAssignment());

        // Act
        shiftService.createShiftRotation(1L, shiftTemplateIds, LocalDate.now(), 14);

        // Assert
        verify(assignmentRepository, atLeastOnce()).save(any(EmployeeShiftAssignment.class));
    }
}