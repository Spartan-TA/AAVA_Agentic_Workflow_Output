package com.company.wms.scheduling.service;

import com.company.wms.employee.model.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.exception.BusinessException;
import com.company.wms.exception.ResourceNotFoundException;
import com.company.wms.scheduling.dto.BulkAssignmentDTO;
import com.company.wms.scheduling.dto.ShiftAssignmentDTO;
import com.company.wms.scheduling.dto.ShiftTemplateDTO;
import com.company.wms.scheduling.model.ShiftAssignment;
import com.company.wms.scheduling.model.ShiftStatus;
import com.company.wms.scheduling.model.ShiftTemplate;
import com.company.wms.scheduling.repository.ShiftAssignmentRepository;
import com.company.wms.scheduling.repository.ShiftTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftService
 * Covers shift templates, assignments, conflict detection, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate dayShiftTemplate;
    private ShiftTemplate nightShiftTemplate;
    private Employee testEmployee;
    private ShiftAssignment testAssignment;

    @BeforeEach
    void setUp() {
        // Setup day shift template
        dayShiftTemplate = new ShiftTemplate();
        dayShiftTemplate.setId(1L);
        dayShiftTemplate.setName("Day Shift");
        dayShiftTemplate.setStartTime(LocalTime.of(9, 0));
        dayShiftTemplate.setEndTime(LocalTime.of(17, 0));
        dayShiftTemplate.setRecurring(true);
        dayShiftTemplate.setRecurrencePattern("DAILY");
        dayShiftTemplate.setOvertimeEligible(true);
        dayShiftTemplate.setBreakMinutes(60);

        // Setup night shift template
        nightShiftTemplate = new ShiftTemplate();
        nightShiftTemplate.setId(2L);
        nightShiftTemplate.setName("Night Shift");
        nightShiftTemplate.setStartTime(LocalTime.of(22, 0));
        nightShiftTemplate.setEndTime(LocalTime.of(6, 0));
        nightShiftTemplate.setRecurring(true);
        nightShiftTemplate.setRecurrencePattern("DAILY");
        nightShiftTemplate.setOvertimeEligible(true);
        nightShiftTemplate.setBreakMinutes(60);

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setDepartment("Warehouse");

        // Setup test assignment
        testAssignment = new ShiftAssignment();
        testAssignment.setId(1L);
        testAssignment.setEmployee(testEmployee);
        testAssignment.setShiftTemplate(dayShiftTemplate);
        testAssignment.setShiftDate(LocalDate.now());
        testAssignment.setStartTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
        testAssignment.setEndTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)));
        testAssignment.setStatus(ShiftStatus.SCHEDULED);
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    void createShiftTemplate_ValidInput_ReturnsShiftTemplateDTO() {
        // Arrange
        ShiftTemplateDTO dto = new ShiftTemplateDTO();
        dto.setName("Day Shift");
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(17, 0));
        dto.setRecurring(true);
        dto.setRecurrencePattern("DAILY");

        when(shiftTemplateRepository.save(any(ShiftTemplate.class)))
            .thenReturn(dayShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Day Shift", result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    void createShiftTemplate_NullInput_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(null);
        });
    }

    @Test
    void createShiftTemplate_InvalidTimeRange_ThrowsBusinessException() {
        // Arrange
        ShiftTemplateDTO dto = new ShiftTemplateDTO();
        dto.setName("Invalid Shift");
        dto.setStartTime(LocalTime.of(17, 0));
        dto.setEndTime(LocalTime.of(9, 0)); // End before start

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.createShiftTemplate(dto);
        });
    }

    @Test
    void createShiftTemplate_EmptyName_ThrowsIllegalArgumentException() {
        // Arrange
        ShiftTemplateDTO dto = new ShiftTemplateDTO();
        dto.setName("");
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(17, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(dto);
        });
    }

    @Test
    void createShiftTemplate_NullStartTime_ThrowsIllegalArgumentException() {
        // Arrange
        ShiftTemplateDTO dto = new ShiftTemplateDTO();
        dto.setName("Day Shift");
        dto.setStartTime(null);
        dto.setEndTime(LocalTime.of(17, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(dto);
        });
    }

    @Test
    void createShiftTemplate_OvernightShift_Success() {
        // Arrange
        ShiftTemplateDTO dto = new ShiftTemplateDTO();
        dto.setName("Night Shift");
        dto.setStartTime(LocalTime.of(22, 0));
        dto.setEndTime(LocalTime.of(6, 0)); // Next day

        when(shiftTemplateRepository.save(any(ShiftTemplate.class)))
            .thenReturn(nightShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Night Shift", result.getName());
    }

    // ========== ASSIGN SHIFT TESTS ==========

    @Test
    void assignShift_ValidInput_ReturnsShiftAssignmentDTO() {
        // Arrange
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
        dto.setEmployeeId(1L);
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now().plusDays(1));

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L))
            .thenReturn(Optional.of(dayShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(anyLong(), any(), any()))
            .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(testAssignment);

        // Act
        ShiftAssignmentDTO result = shiftService.assignShift(dto);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    void assignShift_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
        dto.setEmployeeId(999L);
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now().plusDays(1));

        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(dto);
        });
    }

    @Test
    void assignShift_ShiftTemplateNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
        dto.setEmployeeId(1L);
        dto.setShiftTemplateId(999L);
        dto.setShiftDate(LocalDate.now().plusDays(1));

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(dto);
        });
    }

    @Test
    void assignShift_ConflictingShift_ThrowsBusinessException() {
        // Arrange
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
        dto.setEmployeeId(1L);
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now());

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L))
            .thenReturn(Optional.of(dayShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(testAssignment));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(dto);
        });
    }

    @Test
    void assignShift_PastDate_ThrowsBusinessException() {
        // Arrange
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
        dto.setEmployeeId(1L);
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(dto);
        });
    }

    @Test
    void assignShift_NullEmployeeId_ThrowsIllegalArgumentException() {
        // Arrange
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
        dto.setEmployeeId(null);
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(dto);
        });
    }

    // ========== BULK ASSIGNMENT TESTS ==========

    @Test
    void bulkAssign_ValidInput_ReturnsListOfAssignments() {
        // Arrange
        BulkAssignmentDTO dto = new BulkAssignmentDTO();
        dto.setEmployeeIds(Arrays.asList(1L, 2L));
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now().plusDays(1));
        dto.setStartTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(9, 0)));
        dto.setEndTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(17, 0)));

        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setName("Jane Doe");

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(2L))
            .thenReturn(Optional.of(employee2));
        when(shiftTemplateRepository.findById(1L))
            .thenReturn(Optional.of(dayShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(anyLong(), any(), any()))
            .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList(testAssignment));

        // Act
        List<ShiftAssignmentDTO> result = shiftService.bulkAssign(dto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(shiftAssignmentRepository, times(1)).saveAll(anyList());
    }

    @Test
    void bulkAssign_EmptyEmployeeList_ThrowsIllegalArgumentException() {
        // Arrange
        BulkAssignmentDTO dto = new BulkAssignmentDTO();
        dto.setEmployeeIds(Arrays.asList());
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssign(dto);
        });
    }

    @Test
    void bulkAssign_OneEmployeeHasConflict_ThrowsBusinessException() {
        // Arrange
        BulkAssignmentDTO dto = new BulkAssignmentDTO();
        dto.setEmployeeIds(Arrays.asList(1L, 2L));
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now().plusDays(1));
        dto.setStartTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(9, 0)));
        dto.setEndTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(17, 0)));

        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setName("Jane Doe");

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(2L))
            .thenReturn(Optional.of(employee2));
        when(shiftTemplateRepository.findById(1L))
            .thenReturn(Optional.of(dayShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(eq(1L), any(), any()))
            .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.findConflictingShifts(eq(2L), any(), any()))
            .thenReturn(Arrays.asList(testAssignment)); // Conflict for employee 2

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.bulkAssign(dto);
        });
    }

    // ========== GET UPCOMING SHIFTS TESTS ==========

    @Test
    void getUpcomingShifts_ValidEmployeeId_ReturnsShiftList() {
        // Arrange
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(shiftAssignmentRepository.findUpcomingShifts(eq(1L), any()))
            .thenReturn(Arrays.asList(testAssignment));

        // Act
        List<ShiftAssignmentDTO> result = shiftService.getUpcomingShifts(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getUpcomingShifts_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.getUpcomingShifts(999L);
        });
    }

    @Test
    void getUpcomingShifts_NoUpcomingShifts_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(shiftAssignmentRepository.findUpcomingShifts(eq(1L), any()))
            .thenReturn(Arrays.asList());

        // Act
        List<ShiftAssignmentDTO> result = shiftService.getUpcomingShifts(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== CANCEL SHIFT TESTS ==========

    @Test
    void cancelShift_ValidId_UpdatesStatusToCancelled() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L))
            .thenReturn(Optional.of(testAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(testAssignment);

        // Act
        shiftService.cancelShift(1L);

        // Assert
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
        assertEquals(ShiftStatus.CANCELLED, testAssignment.getStatus());
    }

    @Test
    void cancelShift_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(shiftAssignmentRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.cancelShift(999L);
        });
    }

    @Test
    void cancelShift_AlreadyCancelled_ThrowsBusinessException() {
        // Arrange
        testAssignment.setStatus(ShiftStatus.CANCELLED);
        when(shiftAssignmentRepository.findById(1L))
            .thenReturn(Optional.of(testAssignment));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.cancelShift(1L);
        });
    }

    @Test
    void cancelShift_CompletedShift_ThrowsBusinessException() {
        // Arrange
        testAssignment.setStatus(ShiftStatus.COMPLETED);
        when(shiftAssignmentRepository.findById(1L))
            .thenReturn(Optional.of(testAssignment));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.cancelShift(1L);
        });
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    void detectConflict_OverlappingShifts_ReturnsTrue() {
        // Arrange
        LocalDateTime start1 = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0));
        LocalDateTime end1 = LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0));
        LocalDateTime start2 = LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 0));
        LocalDateTime end2 = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));

        when(shiftAssignmentRepository.findConflictingShifts(1L, start2, end2))
            .thenReturn(Arrays.asList(testAssignment));

        // Act
        boolean hasConflict = shiftService.hasConflict(1L, start2, end2);

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    void detectConflict_NonOverlappingShifts_ReturnsFalse() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0));
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0));

        when(shiftAssignmentRepository.findConflictingShifts(1L, start, end))
            .thenReturn(Arrays.asList());

        // Act
        boolean hasConflict = shiftService.hasConflict(1L, start, end);

        // Assert
        assertFalse(hasConflict);
    }

    @Test
    void detectConflict_AdjacentShifts_ReturnsFalse() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0));
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0));

        when(shiftAssignmentRepository.findConflictingShifts(1L, start, end))
            .thenReturn(Arrays.asList());

        // Act
        boolean hasConflict = shiftService.hasConflict(1L, start, end);

        // Assert
        assertFalse(hasConflict);
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void createShiftTemplate_MidnightToMidnight_Success() {
        // Arrange
        ShiftTemplateDTO dto = new ShiftTemplateDTO();
        dto.setName("24 Hour Shift");
        dto.setStartTime(LocalTime.of(0, 0));
        dto.setEndTime(LocalTime.of(23, 59));

        when(shiftTemplateRepository.save(any(ShiftTemplate.class)))
            .thenReturn(dayShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void assignShift_MaxFutureDate_Success() {
        // Arrange
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
        dto.setEmployeeId(1L);
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now().plusYears(1));

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L))
            .thenReturn(Optional.of(dayShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(anyLong(), any(), any()))
            .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(testAssignment);

        // Act
        ShiftAssignmentDTO result = shiftService.assignShift(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void bulkAssign_LargeNumberOfEmployees_Success() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        BulkAssignmentDTO dto = new BulkAssignmentDTO();
        dto.setEmployeeIds(employeeIds);
        dto.setShiftTemplateId(1L);
        dto.setShiftDate(LocalDate.now().plusDays(1));
        dto.setStartTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(9, 0)));
        dto.setEndTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(17, 0)));

        when(employeeRepository.findById(anyLong()))
            .thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L))
            .thenReturn(Optional.of(dayShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(anyLong(), any(), any()))
            .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList(testAssignment));

        // Act
        List<ShiftAssignmentDTO> result = shiftService.bulkAssign(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void createShiftTemplate_ZeroBreakMinutes_Success() {
        // Arrange
        ShiftTemplateDTO dto = new ShiftTemplateDTO();
        dto.setName("No Break Shift");
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(17, 0));
        dto.setBreakMinutes(0);

        when(shiftTemplateRepository.save(any(ShiftTemplate.class)))
            .thenReturn(dayShiftTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(dto);

        // Assert
        assertNotNull(result);
    }
}