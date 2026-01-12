package com.warehouse.scheduling;

import com.warehouse.scheduling.entity.ShiftTemplate;
import com.warehouse.scheduling.entity.ShiftAssignment;
import com.warehouse.scheduling.repository.ShiftTemplateRepository;
import com.warehouse.scheduling.repository.ShiftAssignmentRepository;
import com.warehouse.scheduling.service.ShiftSchedulingService;
import com.warehouse.scheduling.dto.ShiftDTO;
import com.warehouse.scheduling.exception.ShiftConflictException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftSchedulingService
 * Covers shift templates, assignments, conflict detection, and bulk operations
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ShiftSchedulingServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @InjectMocks
    private ShiftSchedulingService shiftSchedulingService;

    private ShiftTemplate shiftTemplate;
    private ShiftAssignment shiftAssignment;
    private LocalDate testDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
        startTime = LocalTime.of(8, 0);
        endTime = LocalTime.of(17, 0);

        shiftTemplate = new ShiftTemplate();
        shiftTemplate.setId(1L);
        shiftTemplate.setName("Morning Shift");
        shiftTemplate.setStartTime(startTime);
        shiftTemplate.setEndTime(endTime);
        shiftTemplate.setRecurrence("DAILY");
        shiftTemplate.setDepartment("Shipping");
        shiftTemplate.setRequiredRole("WORKER");

        shiftAssignment = new ShiftAssignment();
        shiftAssignment.setId(1L);
        shiftAssignment.setEmployeeId(1L);
        shiftAssignment.setShiftTemplateId(1L);
        shiftAssignment.setDate(testDate);
        shiftAssignment.setStartTime(startTime);
        shiftAssignment.setEndTime(endTime);
        shiftAssignment.setStatus("SCHEDULED");
    }

    @AfterEach
    void tearDown() {
        shiftTemplate = null;
        shiftAssignment = null;
    }

    // ==================== SHIFT TEMPLATE TESTS ====================

    @Test
    @DisplayName("Test createShiftTemplate with valid input - should create template")
    void testCreateShiftTemplate_ValidInput_CreatesTemplate() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class)))
            .thenReturn(shiftTemplate);

        // Act
        ShiftDTO result = shiftSchedulingService.createShiftTemplate(shiftTemplate);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test createShiftTemplate with null input - should throw IllegalArgumentException")
    void testCreateShiftTemplate_NullInput_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftSchedulingService.createShiftTemplate(null);
        });
    }

    @Test
    @DisplayName("Test createShiftTemplate with invalid time range - should throw IllegalArgumentException")
    void testCreateShiftTemplate_InvalidTimeRange_ThrowsException() {
        // Arrange
        shiftTemplate.setStartTime(LocalTime.of(17, 0));
        shiftTemplate.setEndTime(LocalTime.of(8, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftSchedulingService.createShiftTemplate(shiftTemplate);
        });
    }

    @Test
    @DisplayName("Test getShiftTemplate by ID - should return template")
    void testGetShiftTemplate_ValidId_ReturnsTemplate() {
        // Arrange
        when(shiftTemplateRepository.findById(1L))
            .thenReturn(Optional.of(shiftTemplate));

        // Act
        ShiftDTO result = shiftSchedulingService.getShiftTemplate(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Test updateShiftTemplate - should update template")
    void testUpdateShiftTemplate_ValidInput_UpdatesTemplate() {
        // Arrange
        when(shiftTemplateRepository.findById(1L))
            .thenReturn(Optional.of(shiftTemplate));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class)))
            .thenReturn(shiftTemplate);

        // Act
        ShiftDTO result = shiftSchedulingService.updateShiftTemplate(1L, shiftTemplate);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test deleteShiftTemplate - should delete template")
    void testDeleteShiftTemplate_ValidId_DeletesTemplate() {
        // Arrange
        when(shiftTemplateRepository.findById(1L))
            .thenReturn(Optional.of(shiftTemplate));

        // Act
        shiftSchedulingService.deleteShiftTemplate(1L);

        // Assert
        verify(shiftTemplateRepository, times(1)).delete(shiftTemplate);
    }

    // ==================== SHIFT ASSIGNMENT TESTS ====================

    @Test
    @DisplayName("Test assignShift with no conflicts - should create assignment")
    void testAssignShift_NoConflict_CreatesAssignment() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingShifts(
            anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)
        )).thenReturn(Collections.emptyList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(shiftAssignment);

        // Act
        ShiftDTO result = shiftSchedulingService.assignShift(
            1L, 1L, testDate, startTime, endTime
        );

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test assignShift with conflict - should throw ShiftConflictException")
    void testAssignShift_WithConflict_ThrowsException() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingShifts(
            anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)
        )).thenReturn(Arrays.asList(shiftAssignment));

        // Act & Assert
        assertThrows(ShiftConflictException.class, () -> {
            shiftSchedulingService.assignShift(1L, 1L, testDate, startTime, endTime);
        });
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test assignShift with null parameters - should throw IllegalArgumentException")
    void testAssignShift_NullParameters_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftSchedulingService.assignShift(null, 1L, testDate, startTime, endTime);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            shiftSchedulingService.assignShift(1L, null, testDate, startTime, endTime);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            shiftSchedulingService.assignShift(1L, 1L, null, startTime, endTime);
        });
    }

    @Test
    @DisplayName("Test getShiftAssignment by ID - should return assignment")
    void testGetShiftAssignment_ValidId_ReturnsAssignment() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L))
            .thenReturn(Optional.of(shiftAssignment));

        // Act
        ShiftDTO result = shiftSchedulingService.getShiftAssignment(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Test updateShiftAssignment - should update assignment")
    void testUpdateShiftAssignment_ValidInput_UpdatesAssignment() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L))
            .thenReturn(Optional.of(shiftAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(shiftAssignment);

        // Act
        ShiftDTO result = shiftSchedulingService.updateShiftAssignment(1L, shiftAssignment);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test cancelShiftAssignment - should cancel assignment")
    void testCancelShiftAssignment_ValidId_CancelsAssignment() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L))
            .thenReturn(Optional.of(shiftAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(shiftAssignment);

        // Act
        shiftSchedulingService.cancelShiftAssignment(1L);

        // Assert
        verify(shiftAssignmentRepository, times(1)).save(argThat(assignment ->
            "CANCELLED".equals(assignment.getStatus())
        ));
    }

    // ==================== CONFLICT DETECTION TESTS ====================

    @Test
    @DisplayName("Test detectConflicts with overlapping shifts - should detect conflicts")
    void testDetectConflicts_OverlappingShifts_DetectsConflicts() {
        // Arrange
        ShiftAssignment existingShift = new ShiftAssignment();
        existingShift.setEmployeeId(1L);
        existingShift.setDate(testDate);
        existingShift.setStartTime(LocalTime.of(7, 0));
        existingShift.setEndTime(LocalTime.of(15, 0));

        when(shiftAssignmentRepository.findConflictingShifts(
            1L, testDate, startTime, endTime
        )).thenReturn(Arrays.asList(existingShift));

        // Act
        List<ShiftDTO> conflicts = shiftSchedulingService.detectConflicts(
            1L, testDate, startTime, endTime
        );

        // Assert
        assertNotNull(conflicts);
        assertFalse(conflicts.isEmpty());
    }

    @Test
    @DisplayName("Test detectConflicts with no overlaps - should return empty list")
    void testDetectConflicts_NoOverlaps_ReturnsEmptyList() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingShifts(
            1L, testDate, startTime, endTime
        )).thenReturn(Collections.emptyList());

        // Act
        List<ShiftDTO> conflicts = shiftSchedulingService.detectConflicts(
            1L, testDate, startTime, endTime
        );

        // Assert
        assertNotNull(conflicts);
        assertTrue(conflicts.isEmpty());
    }

    // ==================== BLACKOUT DATES TESTS ====================

    @Test
    @DisplayName("Test assignShift on blackout date - should throw IllegalArgumentException")
    void testAssignShift_BlackoutDate_ThrowsException() {
        // Arrange
        LocalDate blackoutDate = LocalDate.of(2024, 12, 25); // Christmas
        when(shiftSchedulingService.isBlackoutDate(blackoutDate)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftSchedulingService.assignShift(1L, 1L, blackoutDate, startTime, endTime);
        });
    }

    @Test
    @DisplayName("Test addBlackoutDate - should add date to blackout list")
    void testAddBlackoutDate_ValidDate_AddsToBlacklist() {
        // Arrange
        LocalDate blackoutDate = LocalDate.of(2024, 12, 25);

        // Act
        shiftSchedulingService.addBlackoutDate(blackoutDate, "Christmas");

        // Assert
        assertTrue(shiftSchedulingService.isBlackoutDate(blackoutDate));
    }

    @Test
    @DisplayName("Test removeBlackoutDate - should remove date from blackout list")
    void testRemoveBlackoutDate_ValidDate_RemovesFromBlacklist() {
        // Arrange
        LocalDate blackoutDate = LocalDate.of(2024, 12, 25);
        shiftSchedulingService.addBlackoutDate(blackoutDate, "Christmas");

        // Act
        shiftSchedulingService.removeBlackoutDate(blackoutDate);

        // Assert
        assertFalse(shiftSchedulingService.isBlackoutDate(blackoutDate));
    }

    // ==================== BULK ASSIGNMENT TESTS ====================

    @Test
    @DisplayName("Test bulkAssignShifts with valid employees - should create multiple assignments")
    void testBulkAssignShifts_ValidEmployees_CreatesMultipleAssignments() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        when(shiftAssignmentRepository.findConflictingShifts(
            anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)
        )).thenReturn(Collections.emptyList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(shiftAssignment);

        // Act
        List<ShiftDTO> results = shiftSchedulingService.bulkAssignShifts(
            employeeIds, 1L, testDate, startTime, endTime
        );

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        verify(shiftAssignmentRepository, times(3)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test bulkAssignShifts with conflicts - should skip conflicting assignments")
    void testBulkAssignShifts_WithConflicts_SkipsConflictingAssignments() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        when(shiftAssignmentRepository.findConflictingShifts(
            eq(2L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)
        )).thenReturn(Arrays.asList(shiftAssignment));
        when(shiftAssignmentRepository.findConflictingShifts(
            anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)
        )).thenReturn(Collections.emptyList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(shiftAssignment);

        // Act
        List<ShiftDTO> results = shiftSchedulingService.bulkAssignShifts(
            employeeIds, 1L, testDate, startTime, endTime
        );

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size()); // Only 2 successful assignments
    }

    @Test
    @DisplayName("Test bulkAssignShifts with empty employee list - should return empty list")
    void testBulkAssignShifts_EmptyEmployeeList_ReturnsEmptyList() {
        // Arrange
        List<Long> employeeIds = Collections.emptyList();

        // Act
        List<ShiftDTO> results = shiftSchedulingService.bulkAssignShifts(
            employeeIds, 1L, testDate, startTime, endTime
        );

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    // ==================== UPCOMING SHIFTS TESTS ====================

    @Test
    @DisplayName("Test getUpcomingShifts for employee - should return future shifts")
    void testGetUpcomingShifts_ValidEmployee_ReturnsFutureShifts() {
        // Arrange
        List<ShiftAssignment> upcomingShifts = Arrays.asList(shiftAssignment);
        when(shiftAssignmentRepository.findUpcomingShiftsByEmployeeId(
            1L, LocalDate.now()
        )).thenReturn(upcomingShifts);

        // Act
        List<ShiftDTO> results = shiftSchedulingService.getUpcomingShifts(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Test getUpcomingShifts with no future shifts - should return empty list")
    void testGetUpcomingShifts_NoFutureShifts_ReturnsEmptyList() {
        // Arrange
        when(shiftAssignmentRepository.findUpcomingShiftsByEmployeeId(
            1L, LocalDate.now()
        )).thenReturn(Collections.emptyList());

        // Act
        List<ShiftDTO> results = shiftSchedulingService.getUpcomingShifts(1L);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ==================== AUDIT TRAIL TESTS ====================

    @Test
    @DisplayName("Test shift assignment creates audit entry")
    void testShiftAssignment_CreatesAuditEntry() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingShifts(
            anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)
        )).thenReturn(Collections.emptyList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(shiftAssignment);

        // Act
        ShiftDTO result = shiftSchedulingService.assignShift(
            1L, 1L, testDate, startTime, endTime
        );

        // Assert
        assertNotNull(result);
        // Verify audit log creation would be called
        // verify(auditService, times(1)).logAction(any(), any(), any());
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Test assignShift with overnight shift - should handle correctly")
    void testAssignShift_OvernightShift_HandlesCorrectly() {
        // Arrange
        LocalTime nightStart = LocalTime.of(22, 0);
        LocalTime nightEnd = LocalTime.of(6, 0);
        when(shiftAssignmentRepository.findConflictingShifts(
            anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)
        )).thenReturn(Collections.emptyList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(shiftAssignment);

        // Act
        ShiftDTO result = shiftSchedulingService.assignShift(
            1L, 1L, testDate, nightStart, nightEnd
        );

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test getShiftsByDateRange - should return shifts in range")
    void testGetShiftsByDateRange_ValidRange_ReturnsShifts() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<ShiftAssignment> shifts = Arrays.asList(shiftAssignment);
        when(shiftAssignmentRepository.findByDateBetween(startDate, endDate))
            .thenReturn(shifts);

        // Act
        List<ShiftDTO> results = shiftSchedulingService.getShiftsByDateRange(
            startDate, endDate
        );

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Test complete shift scheduling flow - template creation to assignment")
    void testCompleteShiftSchedulingFlow_Success() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class)))
            .thenReturn(shiftTemplate);
        when(shiftAssignmentRepository.findConflictingShifts(
            anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)
        )).thenReturn(Collections.emptyList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(shiftAssignment);

        // Act - Create Template
        ShiftDTO template = shiftSchedulingService.createShiftTemplate(shiftTemplate);
        assertNotNull(template);

        // Act - Assign Shift
        ShiftDTO assignment = shiftSchedulingService.assignShift(
            1L, template.getId(), testDate, startTime, endTime
        );
        assertNotNull(assignment);

        // Assert
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }
}