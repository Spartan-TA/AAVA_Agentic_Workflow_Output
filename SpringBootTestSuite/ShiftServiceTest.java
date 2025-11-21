package com.warehouse.ems.service;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.entity.Shift;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.exception.ShiftConflictException;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftService
 * Tests cover shift templates, assignments, overtime rules, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    private Employee testEmployee;
    private Shift testShift;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setShiftGroup("A");

        testShift = new Shift();
        testShift.setId(1L);
        testShift.setShiftName("Morning Shift");
        testShift.setStartTime(LocalTime.of(8, 0));
        testShift.setEndTime(LocalTime.of(16, 0));
        testShift.setShiftGroup("A");
        testShift.setRotationDays(5);
        testShift.setOvertimeThreshold(8.0);
        testShift.setIsTemplate(true);
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    public void testCreateShiftTemplate_ValidInput_Success() {
        // Arrange
        when(shiftRepository.save(any(Shift.class))).thenReturn(testShift);

        // Act
        Shift result = shiftService.createShiftTemplate("Morning Shift", 
                LocalTime.of(8, 0), LocalTime.of(16, 0), "A", 5, 8.0);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getShiftName());
        assertEquals(LocalTime.of(8, 0), result.getStartTime());
        assertEquals(LocalTime.of(16, 0), result.getEndTime());
        assertTrue(result.getIsTemplate());
        verify(shiftRepository, times(1)).save(any(Shift.class));
    }

    @Test
    public void testCreateShiftTemplate_NullShiftName_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(null, 
                    LocalTime.of(8, 0), LocalTime.of(16, 0), "A", 5, 8.0);
        });
    }

    @Test
    public void testCreateShiftTemplate_EmptyShiftName_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate("", 
                    LocalTime.of(8, 0), LocalTime.of(16, 0), "A", 5, 8.0);
        });
    }

    @Test
    public void testCreateShiftTemplate_NullStartTime_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate("Morning Shift", 
                    null, LocalTime.of(16, 0), "A", 5, 8.0);
        });
    }

    @Test
    public void testCreateShiftTemplate_NullEndTime_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate("Morning Shift", 
                    LocalTime.of(8, 0), null, "A", 5, 8.0);
        });
    }

    @Test
    public void testCreateShiftTemplate_EndTimeBeforeStartTime_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate("Invalid Shift", 
                    LocalTime.of(16, 0), LocalTime.of(8, 0), "A", 5, 8.0);
        });
    }

    @Test
    public void testCreateShiftTemplate_NegativeRotationDays_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate("Morning Shift", 
                    LocalTime.of(8, 0), LocalTime.of(16, 0), "A", -1, 8.0);
        });
    }

    @Test
    public void testCreateShiftTemplate_NegativeOvertimeThreshold_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate("Morning Shift", 
                    LocalTime.of(8, 0), LocalTime.of(16, 0), "A", 5, -1.0);
        });
    }

    @Test
    public void testCreateShiftTemplate_NightShift_Success() {
        // Arrange
        testShift.setShiftName("Night Shift");
        testShift.setStartTime(LocalTime.of(22, 0));
        testShift.setEndTime(LocalTime.of(6, 0));
        when(shiftRepository.save(any(Shift.class))).thenReturn(testShift);

        // Act
        Shift result = shiftService.createShiftTemplate("Night Shift", 
                LocalTime.of(22, 0), LocalTime.of(6, 0), "C", 5, 8.0);

        // Assert
        assertNotNull(result);
        assertEquals("Night Shift", result.getShiftName());
        verify(shiftRepository, times(1)).save(any(Shift.class));
    }

    // ========== ASSIGN SHIFT TESTS ==========

    @Test
    public void testAssignShift_ValidAssignment_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(shiftRepository.findConflictingShifts(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Arrays.asList());
        when(shiftRepository.save(any(Shift.class))).thenReturn(testShift);

        // Act
        Shift result = shiftService.assignShift(1L, 1L, LocalDate.now().plusDays(1));

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsTemplate());
        verify(shiftRepository, times(1)).save(any(Shift.class));
    }

    @Test
    public void testAssignShift_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(999L, 1L, LocalDate.now().plusDays(1));
        });
    }

    @Test
    public void testAssignShift_NonExistentShift_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(1L, 999L, LocalDate.now().plusDays(1));
        });
    }

    @Test
    public void testAssignShift_ConflictingShift_ThrowsException() {
        // Arrange
        Shift conflictingShift = new Shift();
        conflictingShift.setId(2L);
        conflictingShift.setStartTime(LocalTime.of(7, 0));
        conflictingShift.setEndTime(LocalTime.of(15, 0));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(shiftRepository.findConflictingShifts(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Arrays.asList(conflictingShift));

        // Act & Assert
        assertThrows(ShiftConflictException.class, () -> {
            shiftService.assignShift(1L, 1L, LocalDate.now().plusDays(1));
        });
    }

    @Test
    public void testAssignShift_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(null, 1L, LocalDate.now().plusDays(1));
        });
    }

    @Test
    public void testAssignShift_NullShiftId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, null, LocalDate.now().plusDays(1));
        });
    }

    @Test
    public void testAssignShift_NullDate_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 1L, null);
        });
    }

    @Test
    public void testAssignShift_PastDate_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 1L, LocalDate.now().minusDays(1));
        });
    }

    // ========== GET SHIFTS TESTS ==========

    @Test
    public void testGetShiftsByEmployee_ValidEmployee_ReturnsShifts() {
        // Arrange
        List<Shift> shifts = Arrays.asList(testShift);
        when(shiftRepository.findByEmployeeId(1L)).thenReturn(shifts);

        // Act
        List<Shift> result = shiftService.getShiftsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetShiftsByEmployee_NoShifts_ReturnsEmptyList() {
        // Arrange
        when(shiftRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<Shift> result = shiftService.getShiftsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetShiftsByEmployee_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.getShiftsByEmployee(null);
        });
    }

    @Test
    public void testGetShiftTemplates_ReturnsTemplatesOnly() {
        // Arrange
        List<Shift> templates = Arrays.asList(testShift);
        when(shiftRepository.findByIsTemplateTrue()).thenReturn(templates);

        // Act
        List<Shift> result = shiftService.getShiftTemplates();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsTemplate());
    }

    // ========== UPDATE SHIFT TESTS ==========

    @Test
    public void testUpdateShift_ValidUpdate_Success() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(shiftRepository.save(any(Shift.class))).thenReturn(testShift);

        // Act
        Shift result = shiftService.updateShift(1L, "Updated Shift", 
                LocalTime.of(9, 0), LocalTime.of(17, 0));

        // Assert
        assertNotNull(result);
        verify(shiftRepository, times(1)).save(any(Shift.class));
    }

    @Test
    public void testUpdateShift_NonExistentShift_ThrowsException() {
        // Arrange
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.updateShift(999L, "Updated Shift", 
                    LocalTime.of(9, 0), LocalTime.of(17, 0));
        });
    }

    @Test
    public void testUpdateShift_NullShiftId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.updateShift(null, "Updated Shift", 
                    LocalTime.of(9, 0), LocalTime.of(17, 0));
        });
    }

    // ========== DELETE SHIFT TESTS ==========

    @Test
    public void testDeleteShift_ValidShift_Success() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        doNothing().when(shiftRepository).delete(any(Shift.class));

        // Act
        shiftService.deleteShift(1L);

        // Assert
        verify(shiftRepository, times(1)).delete(testShift);
    }

    @Test
    public void testDeleteShift_NonExistentShift_ThrowsException() {
        // Arrange
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.deleteShift(999L);
        });
    }

    @Test
    public void testDeleteShift_NullShiftId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.deleteShift(null);
        });
    }

    // ========== OVERTIME CALCULATION TESTS ==========

    @Test
    public void testCalculateOvertime_NoOvertime_ReturnsZero() {
        // Arrange
        double hoursWorked = 8.0;

        // Act
        double overtime = shiftService.calculateOvertime(testShift, hoursWorked);

        // Assert
        assertEquals(0.0, overtime);
    }

    @Test
    public void testCalculateOvertime_WithOvertime_ReturnsOvertimeHours() {
        // Arrange
        double hoursWorked = 10.0;

        // Act
        double overtime = shiftService.calculateOvertime(testShift, hoursWorked);

        // Assert
        assertEquals(2.0, overtime);
    }

    @Test
    public void testCalculateOvertime_NullShift_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.calculateOvertime(null, 10.0);
        });
    }

    @Test
    public void testCalculateOvertime_NegativeHours_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.calculateOvertime(testShift, -1.0);
        });
    }

    @Test
    public void testCalculateOvertime_BoundaryCase_ReturnsZero() {
        // Arrange - Exactly at threshold
        double hoursWorked = 8.0;

        // Act
        double overtime = shiftService.calculateOvertime(testShift, hoursWorked);

        // Assert
        assertEquals(0.0, overtime);
    }

    @Test
    public void testCalculateOvertime_JustOverThreshold_ReturnsSmallOvertime() {
        // Arrange
        double hoursWorked = 8.1;

        // Act
        double overtime = shiftService.calculateOvertime(testShift, hoursWorked);

        // Assert
        assertEquals(0.1, overtime, 0.01);
    }

    // ========== SHIFT GROUP TESTS ==========

    @Test
    public void testGetShiftsByGroup_ValidGroup_ReturnsShifts() {
        // Arrange
        List<Shift> shifts = Arrays.asList(testShift);
        when(shiftRepository.findByShiftGroup("A")).thenReturn(shifts);

        // Act
        List<Shift> result = shiftService.getShiftsByGroup("A");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getShiftGroup());
    }

    @Test
    public void testGetShiftsByGroup_NonExistentGroup_ReturnsEmptyList() {
        // Arrange
        when(shiftRepository.findByShiftGroup("Z")).thenReturn(Arrays.asList());

        // Act
        List<Shift> result = shiftService.getShiftsByGroup("Z");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetShiftsByGroup_NullGroup_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.getShiftsByGroup(null);
        });
    }

    // ========== SHIFT DURATION TESTS ==========

    @Test
    public void testCalculateShiftDuration_StandardShift_ReturnsEightHours() {
        // Act
        double duration = shiftService.calculateShiftDuration(testShift);

        // Assert
        assertEquals(8.0, duration);
    }

    @Test
    public void testCalculateShiftDuration_NightShift_ReturnsCorrectDuration() {
        // Arrange
        testShift.setStartTime(LocalTime.of(22, 0));
        testShift.setEndTime(LocalTime.of(6, 0));

        // Act
        double duration = shiftService.calculateShiftDuration(testShift);

        // Assert
        assertEquals(8.0, duration);
    }

    @Test
    public void testCalculateShiftDuration_NullShift_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.calculateShiftDuration(null);
        });
    }

    // ========== BLACKOUT DATE TESTS ==========

    @Test
    public void testIsBlackoutDate_NotBlackout_ReturnsFalse() {
        // Arrange
        LocalDate normalDate = LocalDate.now().plusDays(10);

        // Act
        boolean isBlackout = shiftService.isBlackoutDate(normalDate);

        // Assert
        assertFalse(isBlackout);
    }

    @Test
    public void testIsBlackoutDate_NullDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.isBlackoutDate(null);
        });
    }
}