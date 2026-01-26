package com.wms.ems.schedule.service;

import com.wms.ems.schedule.dto.ShiftRequestDTO;
import com.wms.ems.schedule.dto.ShiftResponseDTO;
import com.wms.ems.schedule.entity.ShiftTemplate;
import com.wms.ems.schedule.entity.ShiftAssignment;
import com.wms.ems.schedule.repository.ShiftTemplateRepository;
import com.wms.ems.schedule.repository.ShiftAssignmentRepository;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftService
 * Covers: Shift templates, assignments, conflict detection, scheduling
 * Epic: E05 - Shift & Schedule Management
 */
@ExtendWith(MockitoExtension.class)
public class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    private Employee testEmployee;
    private ShiftTemplate testShiftTemplate;
    private ShiftAssignment testShiftAssignment;
    private ShiftRequestDTO shiftRequestDTO;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testShiftTemplate = new ShiftTemplate();
        testShiftTemplate.setId(1L);
        testShiftTemplate.setName("Morning Shift");
        testShiftTemplate.setStartTime(LocalTime.of(6, 0));
        testShiftTemplate.setEndTime(LocalTime.of(14, 0));
        testShiftTemplate.setDuration(8.0);

        testShiftAssignment = new ShiftAssignment();
        testShiftAssignment.setId(1L);
        testShiftAssignment.setEmployee(testEmployee);
        testShiftAssignment.setShiftTemplate(testShiftTemplate);
        testShiftAssignment.setDate(LocalDate.of(2024, 6, 1));

        shiftRequestDTO = new ShiftRequestDTO();
        shiftRequestDTO.setEmployeeId(1L);
        shiftRequestDTO.setShiftTemplateId(1L);
        shiftRequestDTO.setDate(LocalDate.of(2024, 6, 1));
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    public void testCreateShiftTemplate_ValidInput_CreatesTemplate() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplate result = shiftService.createShiftTemplate(testShiftTemplate);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    public void testCreateShiftTemplate_NullInput_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(null);
        });
    }

    @Test
    public void testCreateShiftTemplate_EndTimeBeforeStartTime_ThrowsException() {
        // Arrange
        testShiftTemplate.setStartTime(LocalTime.of(14, 0));
        testShiftTemplate.setEndTime(LocalTime.of(6, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(testShiftTemplate);
        });
    }

    @Test
    public void testCreateShiftTemplate_EmptyName_ThrowsException() {
        // Arrange
        testShiftTemplate.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(testShiftTemplate);
        });
    }

    // ========== ASSIGN SHIFT TESTS ==========

    @Test
    public void testAssignShift_ValidRequest_CreatesAssignment() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftResponseDTO result = shiftService.assignShift(shiftRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    public void testAssignShift_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(null);
        });
    }

    @Test
    public void testAssignShift_InvalidEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        shiftRequestDTO.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(shiftRequestDTO);
        });
    }

    @Test
    public void testAssignShift_InvalidShiftTemplateId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());
        shiftRequestDTO.setShiftTemplateId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(shiftRequestDTO);
        });
    }

    @Test
    public void testAssignShift_ConflictingShift_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testShiftAssignment));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            shiftService.assignShift(shiftRequestDTO);
        });
        assertTrue(exception.getMessage().contains("conflict"));
    }

    @Test
    public void testAssignShift_PastDate_ThrowsException() {
        // Arrange
        shiftRequestDTO.setDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShift(shiftRequestDTO);
        });
    }

    // ========== BULK ASSIGN SHIFTS TESTS ==========

    @Test
    public void testBulkAssignShifts_ValidRequests_CreatesMultipleAssignments() {
        // Arrange
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setBadgeId("EMP002");

        List<ShiftRequestDTO> requests = Arrays.asList(
                shiftRequestDTO,
                createShiftRequest(2L, 1L, LocalDate.of(2024, 6, 1))
        );

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        List<ShiftResponseDTO> results = shiftService.bulkAssignShifts(requests);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(shiftAssignmentRepository, times(2)).save(any(ShiftAssignment.class));
    }

    @Test
    public void testBulkAssignShifts_EmptyList_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(Arrays.asList());
        });
    }

    @Test
    public void testBulkAssignShifts_NullList_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.bulkAssignShifts(null);
        });
    }

    // ========== GET EMPLOYEE SHIFTS TESTS ==========

    @Test
    public void testGetEmployeeShifts_ValidEmployeeId_ReturnsShifts() {
        // Arrange
        List<ShiftAssignment> assignments = Arrays.asList(testShiftAssignment);
        when(shiftAssignmentRepository.findByEmployeeIdAndDateAfter(anyLong(), any(LocalDate.class)))
                .thenReturn(assignments);

        // Act
        List<ShiftResponseDTO> results = shiftService.getEmployeeUpcomingShifts(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    public void testGetEmployeeShifts_InvalidEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.getEmployeeUpcomingShifts(null);
        });
    }

    @Test
    public void testGetEmployeeShifts_NegativeEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.getEmployeeUpcomingShifts(-1L);
        });
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    public void testDetectConflict_NoConflict_ReturnsEmpty() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingShifts(1L, LocalDate.of(2024, 6, 1)))
                .thenReturn(Arrays.asList());

        // Act
        boolean hasConflict = shiftService.hasShiftConflict(1L, LocalDate.of(2024, 6, 1));

        // Assert
        assertFalse(hasConflict);
    }

    @Test
    public void testDetectConflict_WithConflict_ReturnsConflict() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingShifts(1L, LocalDate.of(2024, 6, 1)))
                .thenReturn(Arrays.asList(testShiftAssignment));

        // Act
        boolean hasConflict = shiftService.hasShiftConflict(1L, LocalDate.of(2024, 6, 1)))

        // Assert
        assertTrue(hasConflict);
    }

    // ========== DELETE SHIFT ASSIGNMENT TESTS ==========

    @Test
    public void testDeleteShiftAssignment_ValidId_DeletesAssignment() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(testShiftAssignment));
        doNothing().when(shiftAssignmentRepository).delete(any(ShiftAssignment.class));

        // Act
        shiftService.deleteShiftAssignment(1L);

        // Assert
        verify(shiftAssignmentRepository, times(1)).delete(any(ShiftAssignment.class));
    }

    @Test
    public void testDeleteShiftAssignment_InvalidId_ThrowsException() {
        // Arrange
        when(shiftAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.deleteShiftAssignment(999L);
        });
    }

    // ========== OVERNIGHT SHIFT TESTS ==========

    @Test
    public void testCreateShiftTemplate_OvernightShift_Success() {
        // Arrange
        testShiftTemplate.setStartTime(LocalTime.of(22, 0));
        testShiftTemplate.setEndTime(LocalTime.of(6, 0));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplate result = shiftService.createShiftTemplate(testShiftTemplate);

        // Assert
        assertNotNull(result);
        assertEquals(LocalTime.of(22, 0), result.getStartTime());
        assertEquals(LocalTime.of(6, 0), result.getEndTime());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testAssignShift_SameEmployeeMultipleDates_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftAssignmentRepository.findConflictingShifts(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftResponseDTO result1 = shiftService.assignShift(shiftRequestDTO);
        shiftRequestDTO.setDate(LocalDate.of(2024, 6, 2));
        ShiftResponseDTO result2 = shiftService.assignShift(shiftRequestDTO);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(shiftAssignmentRepository, times(2)).save(any(ShiftAssignment.class));
    }

    @Test
    public void testGetEmployeeShifts_NoUpcomingShifts_ReturnsEmpty() {
        // Arrange
        when(shiftAssignmentRepository.findByEmployeeIdAndDateAfter(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        List<ShiftResponseDTO> results = shiftService.getEmployeeUpcomingShifts(1L);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ========== HELPER METHODS ==========

    private ShiftRequestDTO createShiftRequest(Long employeeId, Long shiftTemplateId, LocalDate date) {
        ShiftRequestDTO dto = new ShiftRequestDTO();
        dto.setEmployeeId(employeeId);
        dto.setShiftTemplateId(shiftTemplateId);
        dto.setDate(date);
        return dto;
    }
}