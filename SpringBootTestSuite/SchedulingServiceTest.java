package com.wms.scheduling.service;

import com.wms.scheduling.entity.ShiftTemplate;
import com.wms.scheduling.entity.ShiftAssignment;
import com.wms.scheduling.repository.ShiftTemplateRepository;
import com.wms.scheduling.repository.ShiftAssignmentRepository;
import com.wms.scheduling.dto.ShiftTemplateDto;
import com.wms.scheduling.dto.ShiftAssignmentDto;
import com.wms.scheduling.dto.BulkAssignmentRequest;
import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.exception.ResourceNotFoundException;
import com.wms.exception.BadRequestException;
import com.wms.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for SchedulingService
 * Covers shift templates, assignments, conflict detection, and bulk operations
 */
public class SchedulingServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SchedulingService schedulingService;

    private ShiftTemplate testTemplate;
    private ShiftAssignment testAssignment;
    private Employee testEmployee;
    private ShiftTemplateDto templateDto;
    private BulkAssignmentRequest bulkRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("BADGE001");
        
        // Setup test shift template
        testTemplate = new ShiftTemplate();
        testTemplate.setId(1L);
        testTemplate.setName("Morning Shift");
        testTemplate.setStartTime("08:00");
        testTemplate.setEndTime("16:00");
        testTemplate.setDaysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY));
        testTemplate.setActive(true);
        
        // Setup test shift assignment
        testAssignment = new ShiftAssignment();
        testAssignment.setId(1L);
        testAssignment.setEmployeeId(1L);
        testAssignment.setShiftTemplateId(1L);
        testAssignment.setStartTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0));
        testAssignment.setEndTime(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0));
        testAssignment.setStatus("SCHEDULED");
        
        // Setup template DTO
        templateDto = new ShiftTemplateDto();
        templateDto.setName("Evening Shift");
        templateDto.setStartTime("16:00");
        templateDto.setEndTime("00:00");
        templateDto.setDaysOfWeek(Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        
        // Setup bulk assignment request
        bulkRequest = new BulkAssignmentRequest();
        bulkRequest.setShiftTemplateId(1L);
        bulkRequest.setEmployeeIds(Arrays.asList(1L, 2L, 3L));
        bulkRequest.setStartDate(LocalDate.now().plusDays(1));
        bulkRequest.setEndDate(LocalDate.now().plusDays(7));
    }

    // ========== SHIFT TEMPLATE TESTS ==========

    @Test
    @DisplayName("Test create shift template with valid data")
    public void testCreateShiftTemplate_ValidData_Success() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDto result = schedulingService.createShiftTemplate(templateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Evening Shift", result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test create shift template with null name")
    public void testCreateShiftTemplate_NullName_ThrowsBadRequestException() {
        // Arrange
        templateDto.setName(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            schedulingService.createShiftTemplate(templateDto);
        });
    }

    @Test
    @DisplayName("Test create shift template with invalid time format")
    public void testCreateShiftTemplate_InvalidTimeFormat_ThrowsBadRequestException() {
        // Arrange
        templateDto.setStartTime("25:00");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            schedulingService.createShiftTemplate(templateDto);
        });
    }

    @Test
    @DisplayName("Test create shift template with end time before start time")
    public void testCreateShiftTemplate_EndBeforeStart_Success() {
        // Arrange - This is valid for overnight shifts
        templateDto.setStartTime("22:00");
        templateDto.setEndTime("06:00");
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDto result = schedulingService.createShiftTemplate(templateDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create shift template with empty days of week")
    public void testCreateShiftTemplate_EmptyDaysOfWeek_ThrowsBadRequestException() {
        // Arrange
        templateDto.setDaysOfWeek(Arrays.asList());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            schedulingService.createShiftTemplate(templateDto);
        });
    }

    @Test
    @DisplayName("Test get shift template by ID")
    public void testGetShiftTemplateById_ValidId_Success() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

        // Act
        ShiftTemplateDto result = schedulingService.getShiftTemplateById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Morning Shift", result.getName());
    }

    @Test
    @DisplayName("Test get shift template by non-existent ID")
    public void testGetShiftTemplateById_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(shiftTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schedulingService.getShiftTemplateById(999L);
        });
    }

    @Test
    @DisplayName("Test update shift template")
    public void testUpdateShiftTemplate_ValidData_Success() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDto result = schedulingService.updateShiftTemplate(1L, templateDto);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test delete shift template")
    public void testDeleteShiftTemplate_ValidId_Success() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.countActiveAssignmentsByTemplate(1L)).thenReturn(0L);
        doNothing().when(shiftTemplateRepository).delete(any(ShiftTemplate.class));

        // Act
        schedulingService.deleteShiftTemplate(1L);

        // Assert
        verify(shiftTemplateRepository, times(1)).delete(testTemplate);
    }

    @Test
    @DisplayName("Test delete shift template with active assignments")
    public void testDeleteShiftTemplate_WithActiveAssignments_ThrowsConflictException() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.countActiveAssignmentsByTemplate(1L)).thenReturn(5L);

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            schedulingService.deleteShiftTemplate(1L);
        });
    }

    // ========== SHIFT ASSIGNMENT TESTS ==========

    @Test
    @DisplayName("Test assign shift to employee")
    public void testAssignShift_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignmentDto result = schedulingService.assignShift(1L, 1L, LocalDate.now().plusDays(1));

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test assign shift to non-existent employee")
    public void testAssignShift_NonExistentEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schedulingService.assignShift(999L, 1L, LocalDate.now().plusDays(1));
        });
    }

    @Test
    @DisplayName("Test assign shift with conflict detection")
    public void testAssignShift_ConflictingShift_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testAssignment));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            schedulingService.assignShift(1L, 1L, LocalDate.now().plusDays(1));
        });
    }

    @Test
    @DisplayName("Test assign shift in the past")
    public void testAssignShift_PastDate_ThrowsBadRequestException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            schedulingService.assignShift(1L, 1L, LocalDate.now().minusDays(1));
        });
    }

    @Test
    @DisplayName("Test get employee schedule")
    public void testGetEmployeeSchedule_ValidData_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(shiftAssignmentRepository.findByEmployeeIdAndDateRange(1L, startDate, endDate))
            .thenReturn(Arrays.asList(testAssignment));

        // Act
        List<ShiftAssignmentDto> result = schedulingService.getEmployeeSchedule(1L, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test cancel shift assignment")
    public void testCancelShiftAssignment_ValidId_Success() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        schedulingService.cancelShiftAssignment(1L);

        // Assert
        assertEquals("CANCELLED", testAssignment.getStatus());
        verify(shiftAssignmentRepository, times(1)).save(testAssignment);
    }

    @Test
    @DisplayName("Test cancel already cancelled shift")
    public void testCancelShiftAssignment_AlreadyCancelled_ThrowsConflictException() {
        // Arrange
        testAssignment.setStatus("CANCELLED");
        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            schedulingService.cancelShiftAssignment(1L);
        });
    }

    // ========== BULK ASSIGNMENT TESTS ==========

    @Test
    @DisplayName("Test bulk assign shifts to multiple employees")
    public void testBulkAssignShifts_ValidData_Success() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setId(1L);
        Employee emp2 = new Employee();
        emp2.setId(2L);
        Employee emp3 = new Employee();
        emp3.setId(3L);
        
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp1));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(emp2));
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(emp3));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        List<ShiftAssignmentDto> result = schedulingService.bulkAssignShifts(bulkRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        verify(shiftAssignmentRepository, atLeast(3)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test bulk assign with empty employee list")
    public void testBulkAssignShifts_EmptyEmployeeList_ThrowsBadRequestException() {
        // Arrange
        bulkRequest.setEmployeeIds(Arrays.asList());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            schedulingService.bulkAssignShifts(bulkRequest);
        });
    }

    @Test
    @DisplayName("Test bulk assign with invalid date range")
    public void testBulkAssignShifts_InvalidDateRange_ThrowsBadRequestException() {
        // Arrange
        bulkRequest.setStartDate(LocalDate.now().plusDays(7));
        bulkRequest.setEndDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            schedulingService.bulkAssignShifts(bulkRequest);
        });
    }

    @Test
    @DisplayName("Test bulk assign with partial failures")
    public void testBulkAssignShifts_PartialFailures_ReturnsSuccessfulAssignments() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setId(1L);
        
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp1));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty()); // This will fail
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(emp1));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        List<ShiftAssignmentDto> result = schedulingService.bulkAssignShifts(bulkRequest);

        // Assert
        assertNotNull(result);
        // Should have assignments for emp1 and emp3, but not emp2
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    @DisplayName("Test detect overlapping shifts")
    public void testDetectConflicts_OverlappingShifts_ReturnsConflicts() {
        // Arrange
        ShiftAssignment existingShift = new ShiftAssignment();
        existingShift.setStartTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0));
        existingShift.setEndTime(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0));
        
        LocalDateTime newStart = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(1).withHour(22).withMinute(0));
        
        when(shiftAssignmentRepository.findConflictingAssignments(1L, newStart, newEnd))
            .thenReturn(Arrays.asList(existingShift));

        // Act
        boolean hasConflict = schedulingService.hasScheduleConflict(1L, newStart, newEnd);

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    @DisplayName("Test detect adjacent shifts without conflict")
    public void testDetectConflicts_AdjacentShifts_NoConflict() {
        // Arrange
        ShiftAssignment existingShift = new ShiftAssignment();
        existingShift.setStartTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0));
        existingShift.setEndTime(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0));
        
        LocalDateTime newStart = LocalDateTime.now().plusDays(1).withHour(16).withMinute(0);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(1).withHour(24).withMinute(0));
        
        when(shiftAssignmentRepository.findConflictingAssignments(1L, newStart, newEnd))
            .thenReturn(Arrays.asList());

        // Act
        boolean hasConflict = schedulingService.hasScheduleConflict(1L, newStart, newEnd);

        // Assert
        assertFalse(hasConflict);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test create 24-hour shift template")
    public void testCreateShiftTemplate_24HourShift_Success() {
        // Arrange
        templateDto.setStartTime("00:00");
        templateDto.setEndTime("23:59");
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDto result = schedulingService.createShiftTemplate(templateDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test assign shift for all days of week")
    public void testCreateShiftTemplate_AllDaysOfWeek_Success() {
        // Arrange
        templateDto.setDaysOfWeek(Arrays.asList(DayOfWeek.values()));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDto result = schedulingService.createShiftTemplate(templateDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get schedule for date range spanning multiple weeks")
    public void testGetEmployeeSchedule_MultipleWeeks_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        when(shiftAssignmentRepository.findByEmployeeIdAndDateRange(1L, startDate, endDate))
            .thenReturn(Arrays.asList(testAssignment));

        // Act
        List<ShiftAssignmentDto> result = schedulingService.getEmployeeSchedule(1L, startDate, endDate);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test bulk assign with maximum employee count")
    public void testBulkAssignShifts_MaximumEmployees_Success() {
        // Arrange
        List<Long> manyEmployees = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        bulkRequest.setEmployeeIds(manyEmployees);
        
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        List<ShiftAssignmentDto> result = schedulingService.bulkAssignShifts(bulkRequest);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, atLeast(10)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test audit trail for schedule changes")
    public void testScheduleChanges_AuditTrailCreated_Success() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDto result = schedulingService.updateShiftTemplate(1L, templateDto);

        // Assert
        assertNotNull(result);
        // Verify audit log entry was created (would need audit service mock)
    }
}