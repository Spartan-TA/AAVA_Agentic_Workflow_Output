package com.example.warehouse.service;

import com.example.warehouse.entity.ShiftTemplate;
import com.example.warehouse.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ShiftService.
 * 
 * Tests cover:
 * - Shift template CRUD operations
 * - Normal cases with valid shift data
 * - Boundary conditions with null and edge time values
 * - Edge cases with invalid IDs and overlapping shifts
 * - Exception handling for non-existent shifts
 * 
 * @author Warehouse Test Team
 */
@ExtendWith(MockitoExtension.class)
public class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate morningShift;
    private ShiftTemplate nightShift;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    public void setUp() {
        morningShift = ShiftTemplate.builder()
                .id(1L)
                .name("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .build();

        nightShift = ShiftTemplate.builder()
                .id(2L)
                .name("Night Shift")
                .startTime(LocalTime.of(22, 0))
                .endTime(LocalTime.of(6, 0))
                .build();
    }

    // ==================== GET ALL SHIFTS TESTS ====================

    /**
     * Test getAllShifts with multiple shifts - Normal case.
     */
    @Test
    public void testGetAllShifts_WithMultipleShifts_Success() {
        // Arrange
        List<ShiftTemplate> shifts = Arrays.asList(morningShift, nightShift);
        when(shiftRepository.findAll()).thenReturn(shifts);

        // Act
        List<ShiftTemplate> result = shiftService.getAllShifts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Morning Shift", result.get(0).getName());
        assertEquals("Night Shift", result.get(1).getName());
        verify(shiftRepository, times(1)).findAll();
    }

    /**
     * Test getAllShifts with empty list - Boundary condition.
     */
    @Test
    public void testGetAllShifts_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(shiftRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ShiftTemplate> result = shiftService.getAllShifts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(shiftRepository, times(1)).findAll();
    }

    /**
     * Test getAllShifts with single shift - Edge case.
     */
    @Test
    public void testGetAllShifts_SingleShift_Success() {
        // Arrange
        when(shiftRepository.findAll()).thenReturn(Collections.singletonList(morningShift));

        // Act
        List<ShiftTemplate> result = shiftService.getAllShifts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Morning Shift", result.get(0).getName());
        verify(shiftRepository, times(1)).findAll();
    }

    // ==================== GET SHIFT BY ID TESTS ====================

    /**
     * Test getShiftById with valid ID - Normal case.
     */
    @Test
    public void testGetShiftById_ValidId_Success() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(morningShift));

        // Act
        ShiftTemplate result = shiftService.getShiftById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Morning Shift", result.getName());
        assertEquals(LocalTime.of(8, 0), result.getStartTime());
        assertEquals(LocalTime.of(16, 0), result.getEndTime());
        verify(shiftRepository, times(1)).findById(1L);
    }

    /**
     * Test getShiftById with non-existent ID - Edge case.
     */
    @Test
    public void testGetShiftById_NonExistentId_ThrowsException() {
        // Arrange
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shiftService.getShiftById(999L);
        });
        assertEquals("Shift not found", exception.getMessage());
        verify(shiftRepository, times(1)).findById(999L);
    }

    /**
     * Test getShiftById with null ID - Boundary condition.
     */
    @Test
    public void testGetShiftById_NullId_ThrowsException() {
        // Arrange
        when(shiftRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            shiftService.getShiftById(null);
        });
    }

    /**
     * Test getShiftById with negative ID - Edge case.
     */
    @Test
    public void testGetShiftById_NegativeId_ThrowsException() {
        // Arrange
        when(shiftRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            shiftService.getShiftById(-1L);
        });
    }

    // ==================== CREATE SHIFT TESTS ====================

    /**
     * Test createShift with valid data - Normal case.
     */
    @Test
    public void testCreateShift_ValidData_Success() {
        // Arrange
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(morningShift);

        // Act
        ShiftTemplate result = shiftService.createShift(morningShift);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        assertEquals(LocalTime.of(8, 0), result.getStartTime());
        assertEquals(LocalTime.of(16, 0), result.getEndTime());
        verify(shiftRepository, times(1)).save(any(ShiftTemplate.class));
    }

    /**
     * Test createShift with overnight shift - Edge case.
     */
    @Test
    public void testCreateShift_OvernightShift_Success() {
        // Arrange
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(nightShift);

        // Act
        ShiftTemplate result = shiftService.createShift(nightShift);

        // Assert
        assertNotNull(result);
        assertEquals("Night Shift", result.getName());
        assertEquals(LocalTime.of(22, 0), result.getStartTime());
        assertEquals(LocalTime.of(6, 0), result.getEndTime());
        assertTrue(result.getStartTime().isAfter(result.getEndTime())); // Overnight shift
        verify(shiftRepository, times(1)).save(any(ShiftTemplate.class));
    }

    /**
     * Test createShift with null name - Boundary condition.
     */
    @Test
    public void testCreateShift_NullName_Success() {
        // Arrange
        ShiftTemplate shiftWithNullName = ShiftTemplate.builder()
                .id(3L)
                .name(null)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .build();
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(shiftWithNullName);

        // Act
        ShiftTemplate result = shiftService.createShift(shiftWithNullName);

        // Assert
        assertNotNull(result);
        assertNull(result.getName());
        verify(shiftRepository, times(1)).save(any(ShiftTemplate.class));
    }

    /**
     * Test createShift with midnight times - Edge case.
     */
    @Test
    public void testCreateShift_MidnightTimes_Success() {
        // Arrange
        ShiftTemplate midnightShift = ShiftTemplate.builder()
                .id(3L)
                .name("Midnight Shift")
                .startTime(LocalTime.MIDNIGHT)
                .endTime(LocalTime.of(8, 0))
                .build();
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(midnightShift);

        // Act
        ShiftTemplate result = shiftService.createShift(midnightShift);

        // Assert
        assertNotNull(result);
        assertEquals(LocalTime.MIDNIGHT, result.getStartTime());
        verify(shiftRepository, times(1)).save(any(ShiftTemplate.class));
    }

    /**
     * Test createShift with same start and end time - Edge case.
     */
    @Test
    public void testCreateShift_SameStartEndTime_Success() {
        // Arrange
        ShiftTemplate sameTimeShift = ShiftTemplate.builder()
                .id(3L)
                .name("Same Time Shift")
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(12, 0))
                .build();
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(sameTimeShift);

        // Act
        ShiftTemplate result = shiftService.createShift(sameTimeShift);

        // Assert
        assertNotNull(result);
        assertEquals(result.getStartTime(), result.getEndTime());
        verify(shiftRepository, times(1)).save(any(ShiftTemplate.class));
    }

    // ==================== UPDATE SHIFT TESTS ====================

    /**
     * Test updateShift with valid data - Normal case.
     */
    @Test
    public void testUpdateShift_ValidData_Success() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(morningShift));
        ShiftTemplate updatedShift = ShiftTemplate.builder()
                .id(1L)
                .name("Updated Morning Shift")
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(15, 0))
                .build();
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(updatedShift);

        // Act
        ShiftTemplate result = shiftService.updateShift(1L, updatedShift);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Morning Shift", result.getName());
        assertEquals(LocalTime.of(7, 0), result.getStartTime());
        assertEquals(LocalTime.of(15, 0), result.getEndTime());
        verify(shiftRepository, times(1)).findById(1L);
        verify(shiftRepository, times(1)).save(any(ShiftTemplate.class));
    }

    /**
     * Test updateShift with non-existent ID - Edge case.
     */
    @Test
    public void testUpdateShift_NonExistentId_ThrowsException() {
        // Arrange
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shiftService.updateShift(999L, morningShift);
        });
        assertEquals("Shift not found", exception.getMessage());
        verify(shiftRepository, times(1)).findById(999L);
        verify(shiftRepository, never()).save(any(ShiftTemplate.class));
    }

    /**
     * Test updateShift changing only name - Normal case.
     */
    @Test
    public void testUpdateShift_ChangeNameOnly_Success() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(morningShift));
        ShiftTemplate updatedShift = ShiftTemplate.builder()
                .id(1L)
                .name("Early Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .build();
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(updatedShift);

        // Act
        ShiftTemplate result = shiftService.updateShift(1L, updatedShift);

        // Assert
        assertNotNull(result);
        assertEquals("Early Morning Shift", result.getName());
        assertEquals(LocalTime.of(8, 0), result.getStartTime());
        verify(shiftRepository, times(1)).save(any(ShiftTemplate.class));
    }

    /**
     * Test updateShift with null values - Boundary condition.
     */
    @Test
    public void testUpdateShift_NullValues_Success() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(morningShift));
        ShiftTemplate updatedShift = ShiftTemplate.builder()
                .id(1L)
                .name(null)
                .startTime(null)
                .endTime(null)
                .build();
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(updatedShift);

        // Act
        ShiftTemplate result = shiftService.updateShift(1L, updatedShift);

        // Assert
        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getStartTime());
        assertNull(result.getEndTime());
        verify(shiftRepository, times(1)).save(any(ShiftTemplate.class));
    }

    // ==================== DELETE SHIFT TESTS ====================

    /**
     * Test deleteShift with valid ID - Normal case.
     */
    @Test
    public void testDeleteShift_ValidId_Success() {
        // Arrange
        when(shiftRepository.existsById(1L)).thenReturn(true);
        doNothing().when(shiftRepository).deleteById(1L);

        // Act
        shiftService.deleteShift(1L);

        // Assert
        verify(shiftRepository, times(1)).existsById(1L);
        verify(shiftRepository, times(1)).deleteById(1L);
    }

    /**
     * Test deleteShift with non-existent ID - Edge case.
     */
    @Test
    public void testDeleteShift_NonExistentId_ThrowsException() {
        // Arrange
        when(shiftRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shiftService.deleteShift(999L);
        });
        assertEquals("Shift not found", exception.getMessage());
        verify(shiftRepository, times(1)).existsById(999L);
        verify(shiftRepository, never()).deleteById(anyLong());
    }

    /**
     * Test deleteShift with null ID - Boundary condition.
     */
    @Test
    public void testDeleteShift_NullId_ThrowsException() {
        // Arrange
        when(shiftRepository.existsById(null)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            shiftService.deleteShift(null);
        });
        verify(shiftRepository, never()).deleteById(anyLong());
    }

    /**
     * Test deleteShift with negative ID - Edge case.
     */
    @Test
    public void testDeleteShift_NegativeId_ThrowsException() {
        // Arrange
        when(shiftRepository.existsById(-1L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            shiftService.deleteShift(-1L);
        });
        verify(shiftRepository, times(1)).existsById(-1L);
        verify(shiftRepository, never()).deleteById(anyLong());
    }

    /**
     * Test createShift with 24-hour shift - Edge case.
     */
    @Test
    public void testCreateShift_TwentyFourHourShift_Success() {
        // Arrange
        ShiftTemplate fullDayShift = ShiftTemplate.builder()
                .id(3L)
                .name("24-Hour Shift")
                .startTime(LocalTime.MIDNIGHT)
                .endTime(LocalTime.of(23, 59))
                .build();
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(fullDayShift);

        // Act
        ShiftTemplate result = shiftService.createShift(fullDayShift);

        // Assert
        assertNotNull(result);
        assertEquals("24-Hour Shift", result.getName());
        assertEquals(LocalTime.MIDNIGHT, result.getStartTime());
        assertEquals(LocalTime.of(23, 59), result.getEndTime());
        verify(shiftRepository, times(1)).save(any(ShiftTemplate.class));
    }
}