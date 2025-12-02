package com.warehouse.management.scheduling.service;

import com.warehouse.management.scheduling.entity.Shift;
import com.warehouse.management.scheduling.repository.ShiftRepository;
import com.warehouse.management.employee.entity.Employee;
import com.warehouse.management.employee.repository.EmployeeRepository;
import com.warehouse.management.exception.BadRequestException;
import com.warehouse.management.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftServiceImpl
 * Tests cover shift CRUD, assignments, conflict detection, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class ShiftServiceImplTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftServiceImpl shiftService;

    private Shift validShift;
    private Shift anotherShift;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        // Arrange: Create valid test shift
        validShift = new Shift();
        validShift.setId(1L);
        validShift.setName("Morning Shift");
        validShift.setStartTime(LocalTime.of(8, 0));
        validShift.setEndTime(LocalTime.of(16, 0));
        validShift.setDaysOfWeek(new HashSet<>(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")));
        validShift.setDepartmentId(1L);
        validShift.setTenantId("TENANT001");

        // Arrange: Create another shift for conflict testing
        anotherShift = new Shift();
        anotherShift.setId(2L);
        anotherShift.setName("Evening Shift");
        anotherShift.setStartTime(LocalTime.of(16, 0));
        anotherShift.setEndTime(LocalTime.of(0, 0));
        anotherShift.setDaysOfWeek(new HashSet<>(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")));
        anotherShift.setDepartmentId(1L);
        anotherShift.setTenantId("TENANT001");

        // Arrange: Create valid test employee
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setStatus("ACTIVE");
        validEmployee.setDeleted(false);
        validEmployee.setTenantId("TENANT001");
    }

    // ========== CREATE SHIFT TESTS ==========

    @Test
    void testCreateShift_ValidInput_Success() {
        // Arrange
        when(shiftRepository.save(any(Shift.class))).thenReturn(validShift);

        // Act
        Shift result = shiftService.create(validShift);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        assertEquals(LocalTime.of(8, 0), result.getStartTime());
        assertEquals(LocalTime.of(16, 0), result.getEndTime());
        verify(shiftRepository, times(1)).save(any(Shift.class));
    }

    @Test
    void testCreateShift_NullName_ThrowsException() {
        // Arrange
        validShift.setName(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.create(validShift));
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    void testCreateShift_EmptyName_ThrowsException() {
        // Arrange
        validShift.setName("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.create(validShift));
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    void testCreateShift_NullStartTime_ThrowsException() {
        // Arrange
        validShift.setStartTime(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.create(validShift));
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    void testCreateShift_NullEndTime_ThrowsException() {
        // Arrange
        validShift.setEndTime(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.create(validShift));
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    void testCreateShift_NullDaysOfWeek_ThrowsException() {
        // Arrange
        validShift.setDaysOfWeek(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.create(validShift));
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    void testCreateShift_EmptyDaysOfWeek_ThrowsException() {
        // Arrange
        validShift.setDaysOfWeek(new HashSet<>());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.create(validShift));
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    void testCreateShift_NullTenantId_ThrowsException() {
        // Arrange
        validShift.setTenantId(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.create(validShift));
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    void testCreateShift_InvalidDayOfWeek_ThrowsException() {
        // Arrange
        validShift.setDaysOfWeek(new HashSet<>(Arrays.asList("INVALID_DAY")));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.create(validShift));
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    void testCreateShift_OvernightShift_Success() {
        // Arrange
        Shift overnightShift = new Shift();
        overnightShift.setName("Night Shift");
        overnightShift.setStartTime(LocalTime.of(22, 0));
        overnightShift.setEndTime(LocalTime.of(6, 0));
        overnightShift.setDaysOfWeek(new HashSet<>(Arrays.asList("MONDAY")));
        overnightShift.setDepartmentId(1L);
        overnightShift.setTenantId("TENANT001");

        when(shiftRepository.save(any(Shift.class))).thenReturn(overnightShift);

        // Act
        Shift result = shiftService.create(overnightShift);

        // Assert
        assertNotNull(result);
        assertEquals("Night Shift", result.getName());
        verify(shiftRepository, times(1)).save(any(Shift.class));
    }

    // ========== GET SHIFT TESTS ==========

    @Test
    void testGetShiftById_ExistingId_Success() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));

        // Act
        Shift result = shiftService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Morning Shift", result.getName());
        verify(shiftRepository, times(1)).findById(1L);
    }

    @Test
    void testGetShiftById_NonExistingId_ThrowsException() {
        // Arrange
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> shiftService.getById(999L));
        assertTrue(exception.getMessage().contains("Shift not found"));
        verify(shiftRepository, times(1)).findById(999L);
    }

    @Test
    void testGetShiftById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.getById(null));
        verify(shiftRepository, never()).findById(any());
    }

    @Test
    void testGetAllShifts_ValidTenant_Success() {
        // Arrange
        List<Shift> shifts = Arrays.asList(validShift, anotherShift);
        when(shiftRepository.findByTenantId("TENANT001")).thenReturn(shifts);

        // Act
        List<Shift> result = shiftService.getAll("TENANT001");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(shiftRepository, times(1)).findByTenantId("TENANT001");
    }

    @Test
    void testGetAllShifts_NullTenantId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.getAll(null));
        verify(shiftRepository, never()).findByTenantId(anyString());
    }

    @Test
    void testGetShiftsByDepartment_ValidDepartment_Success() {
        // Arrange
        List<Shift> shifts = Arrays.asList(validShift);
        when(shiftRepository.findByDepartmentIdAndTenantId(1L, "TENANT001")).thenReturn(shifts);

        // Act
        List<Shift> result = shiftService.getByDepartment(1L, "TENANT001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(shiftRepository, times(1)).findByDepartmentIdAndTenantId(1L, "TENANT001");
    }

    @Test
    void testGetShiftsByDepartment_NullDepartmentId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> shiftService.getByDepartment(null, "TENANT001"));
        verify(shiftRepository, never()).findByDepartmentIdAndTenantId(anyLong(), anyString());
    }

    // ========== UPDATE SHIFT TESTS ==========

    @Test
    void testUpdateShift_ValidInput_Success() {
        // Arrange
        Shift updatedShift = new Shift();
        updatedShift.setName("Updated Morning Shift");
        updatedShift.setStartTime(LocalTime.of(7, 0));
        updatedShift.setEndTime(LocalTime.of(15, 0));

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
        when(shiftRepository.save(any(Shift.class))).thenReturn(validShift);

        // Act
        Shift result = shiftService.update(1L, updatedShift);

        // Assert
        assertNotNull(result);
        verify(shiftRepository, times(1)).findById(1L);
        verify(shiftRepository, times(1)).save(any(Shift.class));
    }

    @Test
    void testUpdateShift_NonExistingId_ThrowsException() {
        // Arrange
        Shift updatedShift = new Shift();
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> shiftService.update(999L, updatedShift));
        verify(shiftRepository, times(1)).findById(999L);
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    void testUpdateShift_NullId_ThrowsException() {
        // Arrange
        Shift updatedShift = new Shift();

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.update(null, updatedShift));
        verify(shiftRepository, never()).findById(any());
    }

    @Test
    void testUpdateShift_ChangeTenantId_ThrowsException() {
        // Arrange
        Shift updatedShift = new Shift();
        updatedShift.setTenantId("DIFFERENT_TENANT");
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.update(1L, updatedShift));
    }

    // ========== DELETE SHIFT TESTS ==========

    @Test
    void testDeleteShift_ExistingId_Success() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
        doNothing().when(shiftRepository).delete(validShift);

        // Act
        shiftService.delete(1L);

        // Assert
        verify(shiftRepository, times(1)).findById(1L);
        verify(shiftRepository, times(1)).delete(validShift);
    }

    @Test
    void testDeleteShift_NonExistingId_ThrowsException() {
        // Arrange
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> shiftService.delete(999L));
        verify(shiftRepository, times(1)).findById(999L);
        verify(shiftRepository, never()).delete(any(Shift.class));
    }

    @Test
    void testDeleteShift_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> shiftService.delete(null));
        verify(shiftRepository, never()).findById(any());
    }

    // ========== SHIFT ASSIGNMENT TESTS ==========

    @Test
    void testAssignShift_ValidInput_Success() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act
        shiftService.assignShift(1L, employeeIds);

        // Assert
        verify(shiftRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void testAssignShift_NonExistingShift_ThrowsException() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L);
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> shiftService.assignShift(999L, employeeIds));
        verify(shiftRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    void testAssignShift_NonExistingEmployee_ThrowsException() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(999L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> shiftService.assignShift(1L, employeeIds));
        verify(shiftRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    void testAssignShift_NullShiftId_ThrowsException() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L);

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> shiftService.assignShift(null, employeeIds));
        verify(shiftRepository, never()).findById(any());
    }

    @Test
    void testAssignShift_NullEmployeeIds_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> shiftService.assignShift(1L, null));
        verify(shiftRepository, never()).findById(any());
    }

    @Test
    void testAssignShift_EmptyEmployeeIds_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> shiftService.assignShift(1L, Arrays.asList()));
        verify(shiftRepository, never()).findById(any());
    }

    @Test
    void testAssignShift_InactiveEmployee_ThrowsException() {
        // Arrange
        validEmployee.setStatus("INACTIVE");
        List<Long> employeeIds = Arrays.asList(1L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> shiftService.assignShift(1L, employeeIds));
    }

    @Test
    void testAssignShift_DeletedEmployee_ThrowsException() {
        // Arrange
        validEmployee.setDeleted(true);
        List<Long> employeeIds = Arrays.asList(1L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> shiftService.assignShift(1L, employeeIds));
    }

    @Test
    void testAssignShift_TenantMismatch_ThrowsException() {
        // Arrange
        validEmployee.setTenantId("DIFFERENT_TENANT");
        List<Long> employeeIds = Arrays.asList(1L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> shiftService.assignShift(1L, employeeIds));
    }

    // ========== BULK ASSIGNMENT TESTS ==========

    @Test
    void testBulkAssignShift_MultipleEmployees_Success() {
        // Arrange
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setStatus("ACTIVE");
        employee2.setDeleted(false);
        employee2.setTenantId("TENANT001");

        List<Long> employeeIds = Arrays.asList(1L, 2L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee2));

        // Act
        shiftService.assignShift(1L, employeeIds);

        // Assert
        verify(shiftRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).findById(2L);
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    void testDetectConflict_OverlappingShifts_ReturnsTrue() {
        // Arrange
        Shift conflictingShift = new Shift();
        conflictingShift.setStartTime(LocalTime.of(10, 0));
        conflictingShift.setEndTime(LocalTime.of(18, 0));
        conflictingShift.setDaysOfWeek(new HashSet<>(Arrays.asList("MONDAY")));

        // Act
        boolean hasConflict = shiftService.detectConflict(validShift, conflictingShift);

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    void testDetectConflict_NonOverlappingShifts_ReturnsFalse() {
        // Arrange
        Shift nonConflictingShift = new Shift();
        nonConflictingShift.setStartTime(LocalTime.of(16, 0));
        nonConflictingShift.setEndTime(LocalTime.of(0, 0));
        nonConflictingShift.setDaysOfWeek(new HashSet<>(Arrays.asList("MONDAY")));

        // Act
        boolean hasConflict = shiftService.detectConflict(validShift, nonConflictingShift);

        // Assert
        assertFalse(hasConflict);
    }

    @Test
    void testDetectConflict_DifferentDays_ReturnsFalse() {
        // Arrange
        Shift differentDayShift = new Shift();
        differentDayShift.setStartTime(LocalTime.of(8, 0));
        differentDayShift.setEndTime(LocalTime.of(16, 0));
        differentDayShift.setDaysOfWeek(new HashSet<>(Arrays.asList("SATURDAY", "SUNDAY")));

        // Act
        boolean hasConflict = shiftService.detectConflict(validShift, differentDayShift);

        // Assert
        assertFalse(hasConflict);
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testCreateShift_MidnightStart_Success() {
        // Arrange
        validShift.setStartTime(LocalTime.of(0, 0));
        validShift.setEndTime(LocalTime.of(8, 0));
        when(shiftRepository.save(any(Shift.class))).thenReturn(validShift);

        // Act
        Shift result = shiftService.create(validShift);

        // Assert
        assertNotNull(result);
        assertEquals(LocalTime.of(0, 0), result.getStartTime());
    }

    @Test
    void testCreateShift_MidnightEnd_Success() {
        // Arrange
        validShift.setStartTime(LocalTime.of(16, 0));
        validShift.setEndTime(LocalTime.of(0, 0));
        when(shiftRepository.save(any(Shift.class))).thenReturn(validShift);

        // Act
        Shift result = shiftService.create(validShift);

        // Assert
        assertNotNull(result);
        assertEquals(LocalTime.of(0, 0), result.getEndTime());
    }

    @Test
    void testCreateShift_AllDaysOfWeek_Success() {
        // Arrange
        validShift.setDaysOfWeek(new HashSet<>(Arrays.asList(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
        )));
        when(shiftRepository.save(any(Shift.class))).thenReturn(validShift);

        // Act
        Shift result = shiftService.create(validShift);

        // Assert
        assertNotNull(result);
        assertEquals(7, result.getDaysOfWeek().size());
    }

    @Test
    void testCreateShift_SingleDayOfWeek_Success() {
        // Arrange
        validShift.setDaysOfWeek(new HashSet<>(Arrays.asList("MONDAY")));
        when(shiftRepository.save(any(Shift.class))).thenReturn(validShift);

        // Act
        Shift result = shiftService.create(validShift);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getDaysOfWeek().size());
    }

    @Test
    void testCreateShift_MaxLengthName_Success() {
        // Arrange
        String maxName = "A".repeat(100); // Assuming max length is 100
        validShift.setName(maxName);
        when(shiftRepository.save(any(Shift.class))).thenReturn(validShift);

        // Act
        Shift result = shiftService.create(validShift);

        // Assert
        assertNotNull(result);
        assertEquals(maxName, result.getName());
    }
}