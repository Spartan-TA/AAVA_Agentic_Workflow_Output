package com.warehouse.management.scheduling;

import com.warehouse.management.common.exceptions.BusinessException;
import com.warehouse.management.common.exceptions.ResourceNotFoundException;
import com.warehouse.management.employee.Employee;
import com.warehouse.management.employee.EmployeeRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftSchedulingService
 * Tests cover shift templates, assignments, conflict detection, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class ShiftSchedulingServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ConflictDetector conflictDetector;

    @InjectMocks
    private ShiftSchedulingServiceImpl shiftSchedulingService;

    private ShiftTemplate testTemplate;
    private ShiftAssignment testAssignment;
    private Employee testEmployee;
    private ShiftTemplateRequest templateRequest;
    private ShiftAssignmentRequest assignmentRequest;
    private UUID templateId;
    private UUID assignmentId;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        templateId = UUID.randomUUID();
        assignmentId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(employeeId);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        
        // Setup shift template
        testTemplate = new ShiftTemplate();
        testTemplate.setId(templateId);
        testTemplate.setName("Morning Shift");
        testTemplate.setStartTime(LocalTime.of(8, 0));
        testTemplate.setEndTime(LocalTime.of(16, 0));
        testTemplate.setDaysOfWeek(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"));
        
        // Setup shift assignment
        testAssignment = new ShiftAssignment();
        testAssignment.setId(assignmentId);
        testAssignment.setEmployee(testEmployee);
        testAssignment.setShiftTemplate(testTemplate);
        testAssignment.setStartDate(LocalDate.now());
        testAssignment.setEndDate(LocalDate.now().plusDays(30));
        
        // Setup template request
        templateRequest = new ShiftTemplateRequest();
        templateRequest.setName("Morning Shift");
        templateRequest.setStartTime(LocalTime.of(8, 0));
        templateRequest.setEndTime(LocalTime.of(16, 0));
        templateRequest.setDaysOfWeek(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"));
        
        // Setup assignment request
        assignmentRequest = new ShiftAssignmentRequest();
        assignmentRequest.setEmployeeId(employeeId);
        assignmentRequest.setShiftTemplateId(templateId);
        assignmentRequest.setStartDate(LocalDate.now());
        assignmentRequest.setEndDate(LocalDate.now().plusDays(30));
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    void testCreateShiftTemplate_ValidInput_Success() {
        // Arrange
        when(shiftTemplateRepository.existsByName(templateRequest.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateResponse result = shiftSchedulingService.createShiftTemplate(templateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    void testCreateShiftTemplate_DuplicateName_ThrowsBusinessException() {
        // Arrange
        when(shiftTemplateRepository.existsByName(templateRequest.getName())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            shiftSchedulingService.createShiftTemplate(templateRequest);
        });
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    void testCreateShiftTemplate_InvalidTimeRange_ThrowsBusinessException() {
        // Arrange
        templateRequest.setStartTime(LocalTime.of(16, 0));
        templateRequest.setEndTime(LocalTime.of(8, 0));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftSchedulingService.createShiftTemplate(templateRequest);
        });
    }

    @Test
    void testCreateShiftTemplate_NullName_ThrowsBusinessException() {
        // Arrange
        templateRequest.setName(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftSchedulingService.createShiftTemplate(templateRequest);
        });
    }

    @Test
    void testCreateShiftTemplate_EmptyDaysOfWeek_ThrowsBusinessException() {
        // Arrange
        templateRequest.setDaysOfWeek(Arrays.asList());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftSchedulingService.createShiftTemplate(templateRequest);
        });
    }

    @Test
    void testCreateShiftTemplate_OvernightShift_Success() {
        // Arrange
        templateRequest.setName("Night Shift");
        templateRequest.setStartTime(LocalTime.of(22, 0));
        templateRequest.setEndTime(LocalTime.of(6, 0));
        
        when(shiftTemplateRepository.existsByName(templateRequest.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateResponse result = shiftSchedulingService.createShiftTemplate(templateRequest);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    // ========== ASSIGN SHIFT TESTS ==========

    @Test
    void testAssignShift_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(conflictDetector.hasConflict(any(), any(), any())).thenReturn(false);
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignmentResponse result = shiftSchedulingService.assignShift(assignmentRequest);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    void testAssignShift_EmployeeNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            shiftSchedulingService.assignShift(assignmentRequest);
        });
        
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    void testAssignShift_TemplateNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            shiftSchedulingService.assignShift(assignmentRequest);
        });
        
        assertTrue(exception.getMessage().contains("Shift template not found"));
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    void testAssignShift_ConflictDetected_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(conflictDetector.hasConflict(any(), any(), any())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            shiftSchedulingService.assignShift(assignmentRequest);
        });
        
        assertTrue(exception.getMessage().contains("conflict"));
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    void testAssignShift_InvalidDateRange_ThrowsBusinessException() {
        // Arrange
        assignmentRequest.setStartDate(LocalDate.now().plusDays(30));
        assignmentRequest.setEndDate(LocalDate.now());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftSchedulingService.assignShift(assignmentRequest);
        });
    }

    @Test
    void testAssignShift_PastStartDate_ThrowsBusinessException() {
        // Arrange
        assignmentRequest.setStartDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftSchedulingService.assignShift(assignmentRequest);
        });
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    void testDetectConflict_OverlappingShifts_ReturnsTrue() {
        // Arrange
        ShiftAssignment existingAssignment = new ShiftAssignment();
        existingAssignment.setEmployee(testEmployee);
        existingAssignment.setStartDate(LocalDate.now());
        existingAssignment.setEndDate(LocalDate.now().plusDays(15));
        
        when(shiftAssignmentRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(existingAssignment));

        // Act
        boolean hasConflict = shiftSchedulingService.detectConflict(employeeId, 
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(30));

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    void testDetectConflict_NoOverlap_ReturnsFalse() {
        // Arrange
        ShiftAssignment existingAssignment = new ShiftAssignment();
        existingAssignment.setEmployee(testEmployee);
        existingAssignment.setStartDate(LocalDate.now());
        existingAssignment.setEndDate(LocalDate.now().plusDays(15));
        
        when(shiftAssignmentRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList());

        // Act
        boolean hasConflict = shiftSchedulingService.detectConflict(employeeId, 
                LocalDate.now().plusDays(20), LocalDate.now().plusDays(30));

        // Assert
        assertFalse(hasConflict);
    }

    @Test
    void testDetectConflict_SameDayDifferentShifts_ReturnsTrue() {
        // Arrange
        ShiftAssignment morningShift = new ShiftAssignment();
        morningShift.setEmployee(testEmployee);
        morningShift.setStartDate(LocalDate.now());
        morningShift.setEndDate(LocalDate.now());
        
        ShiftTemplate morningTemplate = new ShiftTemplate();
        morningTemplate.setStartTime(LocalTime.of(8, 0));
        morningTemplate.setEndTime(LocalTime.of(16, 0));
        morningShift.setShiftTemplate(morningTemplate);
        
        when(shiftAssignmentRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(morningShift));

        // Act
        boolean hasConflict = shiftSchedulingService.detectConflict(employeeId, 
                LocalDate.now(), LocalDate.now());

        // Assert
        assertTrue(hasConflict);
    }

    // ========== GET EMPLOYEE SCHEDULE TESTS ==========

    @Test
    void testGetEmployeeSchedule_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        
        when(shiftAssignmentRepository.findByEmployeeAndDateRange(employeeId, startDate, endDate))
                .thenReturn(Arrays.asList(testAssignment));

        // Act
        List<ShiftAssignmentResponse> result = shiftSchedulingService.getEmployeeSchedule(employeeId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(shiftAssignmentRepository, times(1)).findByEmployeeAndDateRange(employeeId, startDate, endDate);
    }

    @Test
    void testGetEmployeeSchedule_EmptyResult_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        
        when(shiftAssignmentRepository.findByEmployeeAndDateRange(employeeId, startDate, endDate))
                .thenReturn(Arrays.asList());

        // Act
        List<ShiftAssignmentResponse> result = shiftSchedulingService.getEmployeeSchedule(employeeId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEmployeeSchedule_InvalidDateRange_ThrowsBusinessException() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(30);
        LocalDate endDate = LocalDate.now();

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftSchedulingService.getEmployeeSchedule(employeeId, startDate, endDate);
        });
    }

    // ========== BULK ASSIGNMENT TESTS ==========

    @Test
    void testBulkAssignShifts_ValidInput_Success() {
        // Arrange
        List<UUID> employeeIds = Arrays.asList(employeeId, UUID.randomUUID());
        BulkAssignmentRequest bulkRequest = new BulkAssignmentRequest();
        bulkRequest.setEmployeeIds(employeeIds);
        bulkRequest.setShiftTemplateId(templateId);
        bulkRequest.setStartDate(LocalDate.now());
        bulkRequest.setEndDate(LocalDate.now().plusDays(30));
        
        when(employeeRepository.findById(any())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(conflictDetector.hasConflict(any(), any(), any())).thenReturn(false);
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        List<ShiftAssignmentResponse> result = shiftSchedulingService.bulkAssignShifts(bulkRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(shiftAssignmentRepository, times(2)).save(any(ShiftAssignment.class));
    }

    @Test
    void testBulkAssignShifts_EmptyEmployeeList_ThrowsBusinessException() {
        // Arrange
        BulkAssignmentRequest bulkRequest = new BulkAssignmentRequest();
        bulkRequest.setEmployeeIds(Arrays.asList());
        bulkRequest.setShiftTemplateId(templateId);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftSchedulingService.bulkAssignShifts(bulkRequest);
        });
    }

    @Test
    void testBulkAssignShifts_PartialFailure_ReturnsSuccessful() {
        // Arrange
        UUID validEmployeeId = UUID.randomUUID();
        UUID invalidEmployeeId = UUID.randomUUID();
        List<UUID> employeeIds = Arrays.asList(validEmployeeId, invalidEmployeeId);
        
        BulkAssignmentRequest bulkRequest = new BulkAssignmentRequest();
        bulkRequest.setEmployeeIds(employeeIds);
        bulkRequest.setShiftTemplateId(templateId);
        bulkRequest.setStartDate(LocalDate.now());
        bulkRequest.setEndDate(LocalDate.now().plusDays(30));
        
        when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(invalidEmployeeId)).thenReturn(Optional.empty());
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(conflictDetector.hasConflict(any(), any(), any())).thenReturn(false);
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        List<ShiftAssignmentResponse> result = shiftSchedulingService.bulkAssignShifts(bulkRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    // ========== UPDATE SHIFT ASSIGNMENT TESTS ==========

    @Test
    void testUpdateShiftAssignment_ValidInput_Success() {
        // Arrange
        ShiftAssignmentRequest updateRequest = new ShiftAssignmentRequest();
        updateRequest.setEmployeeId(employeeId);
        updateRequest.setShiftTemplateId(templateId);
        updateRequest.setStartDate(LocalDate.now().plusDays(5));
        updateRequest.setEndDate(LocalDate.now().plusDays(35));
        
        when(shiftAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(testAssignment));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(conflictDetector.hasConflict(any(), any(), any())).thenReturn(false);
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignmentResponse result = shiftSchedulingService.updateShiftAssignment(assignmentId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    void testUpdateShiftAssignment_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(shiftAssignmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftSchedulingService.updateShiftAssignment(nonExistentId, assignmentRequest);
        });
    }

    // ========== DELETE SHIFT ASSIGNMENT TESTS ==========

    @Test
    void testDeleteShiftAssignment_ValidId_Success() {
        // Arrange
        when(shiftAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(testAssignment));
        doNothing().when(shiftAssignmentRepository).delete(testAssignment);

        // Act
        shiftSchedulingService.deleteShiftAssignment(assignmentId);

        // Assert
        verify(shiftAssignmentRepository, times(1)).delete(testAssignment);
    }

    @Test
    void testDeleteShiftAssignment_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(shiftAssignmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftSchedulingService.deleteShiftAssignment(nonExistentId);
        });
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testCreateShiftTemplate_24HourShift_Success() {
        // Arrange
        templateRequest.setName("24 Hour Shift");
        templateRequest.setStartTime(LocalTime.of(0, 0));
        templateRequest.setEndTime(LocalTime.of(23, 59));
        
        when(shiftTemplateRepository.existsByName(templateRequest.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateResponse result = shiftSchedulingService.createShiftTemplate(templateRequest);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    void testAssignShift_SingleDayAssignment_Success() {
        // Arrange
        assignmentRequest.setStartDate(LocalDate.now());
        assignmentRequest.setEndDate(LocalDate.now());
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(conflictDetector.hasConflict(any(), any(), any())).thenReturn(false);
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignmentResponse result = shiftSchedulingService.assignShift(assignmentRequest);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    void testCreateShiftTemplate_WeekendOnlyShift_Success() {
        // Arrange
        templateRequest.setName("Weekend Shift");
        templateRequest.setDaysOfWeek(Arrays.asList("SATURDAY", "SUNDAY"));
        
        when(shiftTemplateRepository.existsByName(templateRequest.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateResponse result = shiftSchedulingService.createShiftTemplate(templateRequest);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    void testAssignShift_LongTermAssignment_Success() {
        // Arrange
        assignmentRequest.setStartDate(LocalDate.now());
        assignmentRequest.setEndDate(LocalDate.now().plusYears(1));
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(conflictDetector.hasConflict(any(), any(), any())).thenReturn(false);
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignmentResponse result = shiftSchedulingService.assignShift(assignmentRequest);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }
}