package com.warehousemgmt.service;

import com.warehousemgmt.domain.ShiftTemplate;
import com.warehousemgmt.domain.ShiftAssignment;
import com.warehousemgmt.dto.ShiftTemplateDTO;
import com.warehousemgmt.dto.BulkAssignDTO;
import com.warehousemgmt.repository.ShiftTemplateRepository;
import com.warehousemgmt.repository.ShiftAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftService (Epic E05)
 * Covers shift templates, scheduling, conflict detection, and bulk assignments
 */
@ExtendWith(MockitoExtension.class)
public class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplateDTO validShiftTemplateDTO;
    private ShiftTemplate validShiftTemplate;

    @BeforeEach
    public void setUp() {
        validShiftTemplateDTO = new ShiftTemplateDTO();
        validShiftTemplateDTO.setName("Morning Shift");
        validShiftTemplateDTO.setStartTime(LocalTime.of(8, 0));
        validShiftTemplateDTO.setEndTime(LocalTime.of(17, 0));
        validShiftTemplateDTO.setRecurring(true);
        validShiftTemplateDTO.setDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

        validShiftTemplate = new ShiftTemplate();
        validShiftTemplate.setId(1L);
        validShiftTemplate.setName("Morning Shift");
        validShiftTemplate.setStartTime(LocalTime.of(8, 0));
        validShiftTemplate.setEndTime(LocalTime.of(17, 0));
        validShiftTemplate.setRecurring(true);
        validShiftTemplate.setDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    public void testCreateShiftTemplate_ValidInput_Success() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplate result = shiftService.createShiftTemplate(validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        assertEquals(LocalTime.of(8, 0), result.getStartTime());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    public void testCreateShiftTemplate_NullName_ThrowsException() {
        // Arrange
        validShiftTemplateDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    public void testCreateShiftTemplate_EmptyName_ThrowsException() {
        // Arrange
        validShiftTemplateDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    public void testCreateShiftTemplate_NullStartTime_ThrowsException() {
        // Arrange
        validShiftTemplateDTO.setStartTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    public void testCreateShiftTemplate_NullEndTime_ThrowsException() {
        // Arrange
        validShiftTemplateDTO.setEndTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    public void testCreateShiftTemplate_EndTimeBeforeStartTime_ThrowsException() {
        // Arrange
        validShiftTemplateDTO.setStartTime(LocalTime.of(17, 0));
        validShiftTemplateDTO.setEndTime(LocalTime.of(8, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    public void testCreateShiftTemplate_OvernightShift_Success() {
        // Arrange
        validShiftTemplateDTO.setName("Night Shift");
        validShiftTemplateDTO.setStartTime(LocalTime.of(22, 0));
        validShiftTemplateDTO.setEndTime(LocalTime.of(6, 0));
        validShiftTemplate.setName("Night Shift");
        validShiftTemplate.setStartTime(LocalTime.of(22, 0));
        validShiftTemplate.setEndTime(LocalTime.of(6, 0));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplate result = shiftService.createShiftTemplate(validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Night Shift", result.getName());
    }

    @Test
    public void testCreateShiftTemplate_EmptyDays_ThrowsException() {
        // Arrange
        validShiftTemplateDTO.setDays(Collections.emptySet());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    public void testCreateShiftTemplate_NullDays_ThrowsException() {
        // Arrange
        validShiftTemplateDTO.setDays(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    // ========== SHIFT ASSIGNMENT TESTS ==========

    @Test
    public void testAssignShift_ValidInput_Success() {
        // Arrange
        Long employeeId = 1L;
        Long shiftTemplateId = 1L;
        LocalDate date = LocalDate.now();

        when(shiftTemplateRepository.findById(shiftTemplateId)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(new ShiftAssignment());

        // Act
        ShiftAssignment result = shiftService.assignShift(employeeId, shiftTemplateId, date);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    public void testAssignShift_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(null, 1L, LocalDate.now());
        });
    }

    @Test
    public void testAssignShift_NullShiftTemplateId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, null, LocalDate.now());
        });
    }

    @Test
    public void testAssignShift_NullDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 1L, null);
        });
    }

    @Test
    public void testAssignShift_NonExistentShiftTemplate_ThrowsException() {
        // Arrange
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            shiftService.assignShift(1L, 999L, LocalDate.now());
        });
    }

    @Test
    public void testAssignShift_PastDate_ThrowsException() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(1L, 1L, pastDate);
        });
    }

    // ========== BULK ASSIGNMENT TESTS ==========

    @Test
    public void testBulkAssignShifts_ValidInput_Success() {
        // Arrange
        BulkAssignDTO bulkAssignDTO = new BulkAssignDTO();
        bulkAssignDTO.setEmployeeIds(Arrays.asList(1L, 2L, 3L));
        bulkAssignDTO.setShiftTemplateId(1L);
        bulkAssignDTO.setStartDate(LocalDate.now());
        bulkAssignDTO.setEndDate(LocalDate.now().plusDays(7));

        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(new ShiftAssignment());

        // Act
        List<ShiftAssignment> results = shiftService.bulkAssignShifts(bulkAssignDTO);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        verify(shiftAssignmentRepository, atLeastOnce()).save(any(ShiftAssignment.class));
    }

    @Test
    public void testBulkAssignShifts_EmptyEmployeeList_ThrowsException() {
        // Arrange
        BulkAssignDTO bulkAssignDTO = new BulkAssignDTO();
        bulkAssignDTO.setEmployeeIds(Collections.emptyList());
        bulkAssignDTO.setShiftTemplateId(1L);
        bulkAssignDTO.setStartDate(LocalDate.now());
        bulkAssignDTO.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(bulkAssignDTO);
        });
    }

    @Test
    public void testBulkAssignShifts_NullEmployeeList_ThrowsException() {
        // Arrange
        BulkAssignDTO bulkAssignDTO = new BulkAssignDTO();
        bulkAssignDTO.setEmployeeIds(null);
        bulkAssignDTO.setShiftTemplateId(1L);
        bulkAssignDTO.setStartDate(LocalDate.now());
        bulkAssignDTO.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(bulkAssignDTO);
        });
    }

    @Test
    public void testBulkAssignShifts_EndDateBeforeStartDate_ThrowsException() {
        // Arrange
        BulkAssignDTO bulkAssignDTO = new BulkAssignDTO();
        bulkAssignDTO.setEmployeeIds(Arrays.asList(1L, 2L));
        bulkAssignDTO.setShiftTemplateId(1L);
        bulkAssignDTO.setStartDate(LocalDate.now().plusDays(7));
        bulkAssignDTO.setEndDate(LocalDate.now());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(bulkAssignDTO);
        });
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    public void testDetectConflict_NoConflict_ReturnsFalse() {
        // Arrange
        Long employeeId = 1L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        when(shiftAssignmentRepository.findByEmployeeIdAndDate(employeeId, date)).thenReturn(Collections.emptyList());

        // Act
        boolean hasConflict = shiftService.detectConflict(employeeId, date, startTime, endTime);

        // Assert
        assertFalse(hasConflict);
    }

    @Test
    public void testDetectConflict_OverlappingShift_ReturnsTrue() {
        // Arrange
        Long employeeId = 1L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        ShiftAssignment existingAssignment = new ShiftAssignment();
        existingAssignment.setEmployeeId(employeeId);
        existingAssignment.setDate(date);
        existingAssignment.setStartTime(LocalTime.of(9, 0));
        existingAssignment.setEndTime(LocalTime.of(18, 0));

        when(shiftAssignmentRepository.findByEmployeeIdAndDate(employeeId, date)).thenReturn(Arrays.asList(existingAssignment));

        // Act
        boolean hasConflict = shiftService.detectConflict(employeeId, date, startTime, endTime);

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    public void testDetectConflict_AdjacentShifts_ReturnsFalse() {
        // Arrange
        Long employeeId = 1L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(17, 0);
        LocalTime endTime = LocalTime.of(22, 0);

        ShiftAssignment existingAssignment = new ShiftAssignment();
        existingAssignment.setEmployeeId(employeeId);
        existingAssignment.setDate(date);
        existingAssignment.setStartTime(LocalTime.of(8, 0));
        existingAssignment.setEndTime(LocalTime.of(17, 0));

        when(shiftAssignmentRepository.findByEmployeeIdAndDate(employeeId, date)).thenReturn(Arrays.asList(existingAssignment));

        // Act
        boolean hasConflict = shiftService.detectConflict(employeeId, date, startTime, endTime);

        // Assert
        assertFalse(hasConflict);
    }

    @Test
    public void testDetectConflict_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.detectConflict(null, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(17, 0));
        });
    }

    // ========== BLACKOUT DATE TESTS ==========

    @Test
    public void testAddBlackoutDate_ValidDate_Success() {
        // Arrange
        LocalDate blackoutDate = LocalDate.now().plusDays(30);
        String reason = "Holiday";

        // Act
        shiftService.addBlackoutDate(blackoutDate, reason);

        // Assert
        verify(shiftTemplateRepository, times(1)).save(any());
    }

    @Test
    public void testAddBlackoutDate_NullDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.addBlackoutDate(null, "Holiday");
        });
    }

    @Test
    public void testAddBlackoutDate_EmptyReason_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.addBlackoutDate(LocalDate.now(), "");
        });
    }

    @Test
    public void testIsBlackoutDate_ValidBlackoutDate_ReturnsTrue() {
        // Arrange
        LocalDate blackoutDate = LocalDate.now().plusDays(30);
        when(shiftTemplateRepository.isBlackoutDate(blackoutDate)).thenReturn(true);

        // Act
        boolean isBlackout = shiftService.isBlackoutDate(blackoutDate);

        // Assert
        assertTrue(isBlackout);
    }

    @Test
    public void testIsBlackoutDate_NonBlackoutDate_ReturnsFalse() {
        // Arrange
        LocalDate normalDate = LocalDate.now().plusDays(1);
        when(shiftTemplateRepository.isBlackoutDate(normalDate)).thenReturn(false);

        // Act
        boolean isBlackout = shiftService.isBlackoutDate(normalDate);

        // Assert
        assertFalse(isBlackout);
    }

    // ========== UPDATE SHIFT TEMPLATE TESTS ==========

    @Test
    public void testUpdateShiftTemplate_ValidInput_Success() {
        // Arrange
        Long templateId = 1L;
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        validShiftTemplateDTO.setName("Updated Shift");

        // Act
        ShiftTemplate result = shiftService.updateShiftTemplate(templateId, validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    public void testUpdateShiftTemplate_NonExistentId_ThrowsException() {
        // Arrange
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            shiftService.updateShiftTemplate(999L, validShiftTemplateDTO);
        });
    }

    // ========== DELETE SHIFT TEMPLATE TESTS ==========

    @Test
    public void testDeleteShiftTemplate_ValidId_Success() {
        // Arrange
        Long templateId = 1L;
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.of(validShiftTemplate));
        doNothing().when(shiftTemplateRepository).deleteById(templateId);

        // Act
        shiftService.deleteShiftTemplate(templateId);

        // Assert
        verify(shiftTemplateRepository, times(1)).deleteById(templateId);
    }

    @Test
    public void testDeleteShiftTemplate_NonExistentId_ThrowsException() {
        // Arrange
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            shiftService.deleteShiftTemplate(999L);
        });
    }

    @Test
    public void testDeleteShiftTemplate_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.deleteShiftTemplate(null);
        });
    }

    // ========== GET UPCOMING SHIFTS TESTS ==========

    @Test
    public void testGetUpcomingShifts_ValidEmployeeId_ReturnsShifts() {
        // Arrange
        Long employeeId = 1L;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        when(shiftAssignmentRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate))
                .thenReturn(Arrays.asList(new ShiftAssignment()));

        // Act
        List<ShiftAssignment> results = shiftService.getUpcomingShifts(employeeId, startDate, endDate);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    public void testGetUpcomingShifts_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.getUpcomingShifts(null, LocalDate.now(), LocalDate.now().plusDays(7));
        });
    }

    @Test
    public void testGetUpcomingShifts_EndDateBeforeStartDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.getUpcomingShifts(1L, LocalDate.now().plusDays(7), LocalDate.now());
        });
    }
}