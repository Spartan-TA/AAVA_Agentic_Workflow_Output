package com.wms.ems.schedule;

import com.wms.ems.employee.Employee;
import com.wms.ems.employee.EmployeeRepository;
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
 * Comprehensive JUnit test suite for ShiftService.
 * Tests cover shift template creation, assignments, conflict detection,
 * bulk operations, and edge cases.
 * 
 * @author Warehouse EMS Test Suite
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private WarehouseCalendarRepository warehouseCalendarRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate testShiftTemplate;
    private ShiftAssignment testShiftAssignment;
    private Employee testEmployee;
    private ShiftTemplateDto shiftTemplateDto;
    private ShiftAssignmentDto shiftAssignmentDto;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setStatus("ACTIVE");

        // Arrange: Create test shift template
        testShiftTemplate = new ShiftTemplate();
        testShiftTemplate.setId(1L);
        testShiftTemplate.setName("Day Shift");
        testShiftTemplate.setStartTime(LocalTime.of(8, 0));
        testShiftTemplate.setEndTime(LocalTime.of(16, 0));
        testShiftTemplate.setRecurring(true);
        testShiftTemplate.setWarehouseId(1L);

        // Arrange: Create test shift assignment
        testShiftAssignment = new ShiftAssignment();
        testShiftAssignment.setId(1L);
        testShiftAssignment.setEmployeeId(1L);
        testShiftAssignment.setShiftTemplateId(1L);
        testShiftAssignment.setDate(LocalDate.now());
        testShiftAssignment.setStatus("SCHEDULED");

        // Arrange: Create shift template DTO
        shiftTemplateDto = new ShiftTemplateDto();
        shiftTemplateDto.setName("Day Shift");
        shiftTemplateDto.setStartTime(LocalTime.of(8, 0));
        shiftTemplateDto.setEndTime(LocalTime.of(16, 0));
        shiftTemplateDto.setRecurring(true);
        shiftTemplateDto.setWarehouseId(1L);

        // Arrange: Create shift assignment DTO
        shiftAssignmentDto = new ShiftAssignmentDto();
        shiftAssignmentDto.setEmployeeId(1L);
        shiftAssignmentDto.setShiftTemplateId(1L);
        shiftAssignmentDto.setDate(LocalDate.now());
    }

    // ==================== SHIFT TEMPLATE CREATION TESTS ====================

    @Test
    public void testCreateShiftTemplate_ValidInput_Success() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDto result = shiftService.createShiftTemplate(shiftTemplateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Day Shift", result.getName());
        assertEquals(LocalTime.of(8, 0), result.getStartTime());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    public void testCreateShiftTemplate_NullDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(null);
        });
    }

    @Test
    public void testCreateShiftTemplate_NullName_ThrowsException() {
        // Arrange
        shiftTemplateDto.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    @Test
    public void testCreateShiftTemplate_EmptyName_ThrowsException() {
        // Arrange
        shiftTemplateDto.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    @Test
    public void testCreateShiftTemplate_NullStartTime_ThrowsException() {
        // Arrange
        shiftTemplateDto.setStartTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    @Test
    public void testCreateShiftTemplate_NullEndTime_ThrowsException() {
        // Arrange
        shiftTemplateDto.setEndTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    @Test
    public void testCreateShiftTemplate_EndTimeBeforeStartTime_ThrowsException() {
        // Arrange
        shiftTemplateDto.setStartTime(LocalTime.of(16, 0));
        shiftTemplateDto.setEndTime(LocalTime.of(8, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    @Test
    public void testCreateShiftTemplate_OvernightShift_Success() {
        // Arrange
        shiftTemplateDto.setStartTime(LocalTime.of(22, 0));
        shiftTemplateDto.setEndTime(LocalTime.of(6, 0));
        shiftTemplateDto.setOvernight(true);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDto result = shiftService.createShiftTemplate(shiftTemplateDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.isOvernight());
    }

    @Test
    public void testCreateShiftTemplate_InvalidWarehouseId_ThrowsException() {
        // Arrange
        shiftTemplateDto.setWarehouseId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(shiftTemplateDto);
        });
    }

    // ==================== SHIFT ASSIGNMENT TESTS ====================

    @Test
    public void testAssignShift_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(), any())).thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftAssignmentDto result = shiftService.assignShift(shiftAssignmentDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertEquals("SCHEDULED", result.getStatus());
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    public void testAssignShift_NullDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(null);
        });
    }

    @Test
    public void testAssignShift_InvalidEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        shiftAssignmentDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    @Test
    public void testAssignShift_InvalidShiftTemplateId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());
        shiftAssignmentDto.setShiftTemplateId(999L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    @Test
    public void testAssignShift_InactiveEmployee_ThrowsException() {
        // Arrange
        testEmployee.setStatus("INACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    @Test
    public void testAssignShift_PastDate_ThrowsException() {
        // Arrange
        shiftAssignmentDto.setDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    // ==================== CONFLICT DETECTION TESTS ====================

    @Test
    public void testDetectConflicts_NoConflicts_Success() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(), any())).thenReturn(Arrays.asList());

        // Act
        boolean hasConflicts = shiftService.detectConflicts(1L, LocalDate.now(), testShiftTemplate);

        // Assert
        assertFalse(hasConflicts);
    }

    @Test
    public void testDetectConflicts_WithConflicts_ReturnsTrue() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(testShiftAssignment));

        // Act
        boolean hasConflicts = shiftService.detectConflicts(1L, LocalDate.now(), testShiftTemplate);

        // Assert
        assertTrue(hasConflicts);
    }

    @Test
    public void testDetectConflicts_OverlappingShifts_ReturnsTrue() {
        // Arrange
        ShiftAssignment overlappingAssignment = new ShiftAssignment();
        overlappingAssignment.setEmployeeId(1L);
        overlappingAssignment.setDate(LocalDate.now());
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(overlappingAssignment));

        // Act
        boolean hasConflicts = shiftService.detectConflicts(1L, LocalDate.now(), testShiftTemplate);

        // Assert
        assertTrue(hasConflicts);
    }

    @Test
    public void testAssignShift_WithConflict_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(testShiftAssignment));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    // ==================== BULK ASSIGNMENT TESTS ====================

    @Test
    public void testBulkAssignShifts_ValidInput_Success() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(), any())).thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        List<ShiftAssignmentDto> results = shiftService.bulkAssignShifts(employeeIds, 1L, LocalDate.now());

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        verify(shiftAssignmentRepository, times(3)).save(any(ShiftAssignment.class));
    }

    @Test
    public void testBulkAssignShifts_EmptyEmployeeList_ThrowsException() {
        // Arrange
        List<Long> employeeIds = Arrays.asList();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(employeeIds, 1L, LocalDate.now());
        });
    }

    @Test
    public void testBulkAssignShifts_NullEmployeeList_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(null, 1L, LocalDate.now());
        });
    }

    @Test
    public void testBulkAssignShifts_PartialFailure_RollsBack() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 999L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(employeeIds, 1L, LocalDate.now());
        });
    }

    // ==================== BLACKOUT DATE TESTS ====================

    @Test
    public void testAssignShift_OnBlackoutDate_ThrowsException() {
        // Arrange
        LocalDate blackoutDate = LocalDate.now().plusDays(1);
        when(warehouseCalendarRepository.isBlackoutDate(anyLong(), any())).thenReturn(true);
        shiftAssignmentDto.setDate(blackoutDate);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            shiftService.assignShift(shiftAssignmentDto);
        });
    }

    @Test
    public void testIsBlackoutDate_ValidDate_ReturnsTrue() {
        // Arrange
        when(warehouseCalendarRepository.isBlackoutDate(anyLong(), any())).thenReturn(true);

        // Act
        boolean isBlackout = shiftService.isBlackoutDate(1L, LocalDate.now());

        // Assert
        assertTrue(isBlackout);
    }

    @Test
    public void testIsBlackoutDate_NonBlackoutDate_ReturnsFalse() {
        // Arrange
        when(warehouseCalendarRepository.isBlackoutDate(anyLong(), any())).thenReturn(false);

        // Act
        boolean isBlackout = shiftService.isBlackoutDate(1L, LocalDate.now());

        // Assert
        assertFalse(isBlackout);
    }

    // ==================== SHIFT RETRIEVAL TESTS ====================

    @Test
    public void testGetUpcomingShifts_ValidEmployeeId_Success() {
        // Arrange
        when(shiftAssignmentRepository.findUpcomingShiftsByEmployeeId(anyLong(), any()))
            .thenReturn(Arrays.asList(testShiftAssignment));

        // Act
        List<ShiftAssignmentDto> results = shiftService.getUpcomingShifts(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    public void testGetUpcomingShifts_NoShifts_ReturnsEmptyList() {
        // Arrange
        when(shiftAssignmentRepository.findUpcomingShiftsByEmployeeId(anyLong(), any()))
            .thenReturn(Arrays.asList());

        // Act
        List<ShiftAssignmentDto> results = shiftService.getUpcomingShifts(1L);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetUpcomingShifts_InvalidEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.getUpcomingShifts(null);
        });
    }

    // ==================== SHIFT CANCELLATION TESTS ====================

    @Test
    public void testCancelShift_ValidInput_Success() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(testShiftAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        shiftService.cancelShift(1L, "Employee requested cancellation");

        // Assert
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    public void testCancelShift_InvalidShiftId_ThrowsException() {
        // Arrange
        when(shiftAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.cancelShift(999L, "Test reason");
        });
    }

    @Test
    public void testCancelShift_NullReason_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.cancelShift(1L, null);
        });
    }

    @Test
    public void testCancelShift_EmptyReason_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.cancelShift(1L, "");
        });
    }
}