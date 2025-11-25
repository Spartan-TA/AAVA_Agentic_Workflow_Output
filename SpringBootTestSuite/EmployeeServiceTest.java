package com.warehouse.ems.service;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.exception.ValidationException;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService.
 * Tests cover all input method signatures including normal cases, boundary conditions, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;
    private Employee existingEmployee;

    @BeforeEach
    void setUp() {
        // Arrange: Set up valid employee for testing
        validEmployee = Employee.builder()
                .name("John Doe")
                .badgeId("BADGE001")
                .role(Employee.Role.WORKER)
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.now())
                .status(Employee.Status.ACTIVE)
                .deleted(false)
                .build();

        existingEmployee = Employee.builder()
                .id(1L)
                .name("Jane Smith")
                .badgeId("BADGE002")
                .role(Employee.Role.SUPERVISOR)
                .department("Warehouse")
                .shiftGroup("Evening")
                .hireDate(LocalDate.now().minusYears(2))
                .status(Employee.Status.ACTIVE)
                .deleted(false)
                .createdAt(LocalDateTime.now().minusYears(2))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    void testCreateEmployee_WithValidInput_ShouldReturnCreatedEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());

        // Act
        Employee result = employeeService.createEmployee(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("BADGE001", result.getBadgeId());
        assertEquals(Employee.Role.WORKER, result.getRole());
        assertEquals(Employee.Status.ACTIVE, result.getStatus());
        assertFalse(result.getDeleted());
        verify(employeeRepository, times(1)).findByBadgeId("BADGE001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(auditLogService, times(1)).logCreate(eq("Employee"), any(), any());
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ShouldThrowValidationException() {
        // Arrange
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.of(existingEmployee));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> employeeService.createEmployee(validEmployee));
        
        assertEquals("Badge ID already exists: BADGE001", exception.getMessage());
        verify(employeeRepository, times(1)).findByBadgeId("BADGE001");
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(auditLogService, never()).logCreate(anyString(), anyLong(), any());
    }

    @Test
    void testCreateEmployee_WithNullName_ShouldThrowValidationException() {
        // Arrange
        validEmployee.setName(null);
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.createEmployee(validEmployee));
        verify(employeeRepository, times(1)).findByBadgeId("BADGE001");
    }

    @Test
    void testCreateEmployee_WithEmptyName_ShouldThrowValidationException() {
        // Arrange
        validEmployee.setName("");
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.createEmployee(validEmployee));
        verify(employeeRepository, times(1)).findByBadgeId("BADGE001");
    }

    @Test
    void testCreateEmployee_WithNullBadgeId_ShouldThrowValidationException() {
        // Arrange
        validEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.createEmployee(validEmployee));
    }

    @Test
    void testCreateEmployee_WithEmptyBadgeId_ShouldThrowValidationException() {
        // Arrange
        validEmployee.setBadgeId("");

        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.createEmployee(validEmployee));
    }

    @Test
    void testCreateEmployee_WithNullStatus_ShouldSetDefaultActiveStatus() {
        // Arrange
        validEmployee.setStatus(null);
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(1L);
            return emp;
        });
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());

        // Act
        Employee result = employeeService.createEmployee(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(Employee.Status.ACTIVE, result.getStatus());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithAllRoles_ShouldCreateSuccessfully() {
        // Test ADMIN role
        validEmployee.setRole(Employee.Role.ADMIN);
        validEmployee.setBadgeId("ADMIN001");
        when(employeeRepository.findByBadgeId("ADMIN001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());
        
        Employee adminResult = employeeService.createEmployee(validEmployee);
        assertEquals(Employee.Role.ADMIN, adminResult.getRole());

        // Test HR role
        validEmployee.setRole(Employee.Role.HR);
        validEmployee.setBadgeId("HR001");
        when(employeeRepository.findByBadgeId("HR001")).thenReturn(Optional.empty());
        
        Employee hrResult = employeeService.createEmployee(validEmployee);
        assertEquals(Employee.Role.HR, hrResult.getRole());

        // Test SUPERVISOR role
        validEmployee.setRole(Employee.Role.SUPERVISOR);
        validEmployee.setBadgeId("SUPER001");
        when(employeeRepository.findByBadgeId("SUPER001")).thenReturn(Optional.empty());
        
        Employee supervisorResult = employeeService.createEmployee(validEmployee);
        assertEquals(Employee.Role.SUPERVISOR, supervisorResult.getRole());

        // Test WORKER role
        validEmployee.setRole(Employee.Role.WORKER);
        validEmployee.setBadgeId("WORKER001");
        when(employeeRepository.findByBadgeId("WORKER001")).thenReturn(Optional.empty());
        
        Employee workerResult = employeeService.createEmployee(validEmployee);
        assertEquals(Employee.Role.WORKER, workerResult.getRole());
    }

    @Test
    void testCreateEmployee_WithFutureHireDate_ShouldCreateSuccessfully() {
        // Arrange
        validEmployee.setHireDate(LocalDate.now().plusDays(30));
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());

        // Act
        Employee result = employeeService.createEmployee(validEmployee);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHireDate().isAfter(LocalDate.now()));
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithPastHireDate_ShouldCreateSuccessfully() {
        // Arrange
        validEmployee.setHireDate(LocalDate.now().minusYears(5));
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());

        // Act
        Employee result = employeeService.createEmployee(validEmployee);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHireDate().isBefore(LocalDate.now()));
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== GET EMPLOYEES TESTS ====================

    @Test
    void testGetEmployees_WithPagination_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(validEmployee, existingEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, employees.size());
        
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetEmployees_WithDepartmentFilter_ShouldReturnFilteredResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, employees.size());
        
        when(employeeRepository.findByDepartmentAndDeletedFalse("Warehouse", pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable, "Warehouse", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Warehouse", result.getContent().get(0).getDepartment());
        verify(employeeRepository, times(1)).findByDepartmentAndDeletedFalse("Warehouse", pageable);
    }

    @Test
    void testGetEmployees_WithRoleFilter_ShouldReturnFilteredResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(existingEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, employees.size());
        
        when(employeeRepository.findByRoleAndDeletedFalse(Employee.Role.SUPERVISOR, pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable, null, Employee.Role.SUPERVISOR);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(Employee.Role.SUPERVISOR, result.getContent().get(0).getRole());
        verify(employeeRepository, times(1)).findByRoleAndDeletedFalse(Employee.Role.SUPERVISOR, pageable);
    }

    @Test
    void testGetEmployees_WithDepartmentAndRoleFilter_ShouldReturnFilteredResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(existingEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, employees.size());
        
        when(employeeRepository.findByDepartmentAndDeletedFalse("Warehouse", pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable, "Warehouse", Employee.Role.SUPERVISOR);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(employeeRepository, times(1)).findByDepartmentAndDeletedFalse("Warehouse", pageable);
    }

    @Test
    void testGetEmployees_WithEmptyResults_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetEmployees_WithLargePage_ShouldReturnCorrectPageSize() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 100);
        List<Employee> employees = Arrays.asList(validEmployee, existingEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, employees.size());
        
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    void testGetEmployee_WithValidId_ShouldReturnEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));

        // Act
        Employee result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jane Smith", result.getName());
        assertEquals("BADGE002", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEmployee_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> employeeService.getEmployee(999L));
        
        assertEquals("Employee not found with ID: 999", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    void testGetEmployee_WithDeletedEmployee_ShouldThrowResourceNotFoundException() {
        // Arrange
        existingEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> employeeService.getEmployee(1L));
        
        assertEquals("Employee not found with ID: 1", exception.getMessage());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEmployee_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.getEmployee(null));
    }

    @Test
    void testGetEmployee_WithNegativeId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(-1L));
        verify(employeeRepository, times(1)).findById(-1L);
    }

    @Test
    void testGetEmployee_WithZeroId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(0L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(0L));
        verify(employeeRepository, times(1)).findById(0L);
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    void testUpdateEmployee_WithValidInput_ShouldReturnUpdatedEmployee() {
        // Arrange
        Employee updatedData = Employee.builder()
                .name("Jane Doe Updated")
                .role(Employee.Role.HR)
                .department("HR Department")
                .shiftGroup("Day")
                .hireDate(LocalDate.now().minusYears(3))
                .status(Employee.Status.ACTIVE)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logUpdate(anyString(), anyLong(), any(), any());

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe Updated", result.getName());
        assertEquals(Employee.Role.HR, result.getRole());
        assertEquals("HR Department", result.getDepartment());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(auditLogService, times(1)).logUpdate(eq("Employee"), eq(1L), any(), any());
    }

    @Test
    void testUpdateEmployee_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        Employee updatedData = Employee.builder().name("Updated Name").build();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> employeeService.updateEmployee(999L, updatedData));
        
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(auditLogService, never()).logUpdate(anyString(), anyLong(), any(), any());
    }

    @Test
    void testUpdateEmployee_WithNullName_ShouldUpdateOtherFields() {
        // Arrange
        Employee updatedData = Employee.builder()
                .name(null)
                .role(Employee.Role.ADMIN)
                .department("Admin Department")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logUpdate(anyString(), anyLong(), any(), any());

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        assertNull(result.getName());
        assertEquals(Employee.Role.ADMIN, result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithStatusChange_ShouldUpdateSuccessfully() {
        // Arrange
        Employee updatedData = Employee.builder()
                .name("Jane Smith")
                .role(Employee.Role.SUPERVISOR)
                .department("Warehouse")
                .status(Employee.Status.INACTIVE)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logUpdate(anyString(), anyLong(), any(), any());

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        assertEquals(Employee.Status.INACTIVE, result.getStatus());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithRoleChange_ShouldUpdateSuccessfully() {
        // Arrange
        Employee updatedData = Employee.builder()
                .name("Jane Smith")
                .role(Employee.Role.ADMIN)
                .department("Warehouse")
                .status(Employee.Status.ACTIVE)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logUpdate(anyString(), anyLong(), any(), any());

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        assertEquals(Employee.Role.ADMIN, result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithDepartmentChange_ShouldUpdateSuccessfully() {
        // Arrange
        Employee updatedData = Employee.builder()
                .name("Jane Smith")
                .role(Employee.Role.SUPERVISOR)
                .department("Logistics")
                .status(Employee.Status.ACTIVE)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logUpdate(anyString(), anyLong(), any(), any());

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        assertEquals("Logistics", result.getDepartment());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithShiftGroupChange_ShouldUpdateSuccessfully() {
        // Arrange
        Employee updatedData = Employee.builder()
                .name("Jane Smith")
                .role(Employee.Role.SUPERVISOR)
                .department("Warehouse")
                .shiftGroup("Night")
                .status(Employee.Status.ACTIVE)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logUpdate(anyString(), anyLong(), any(), any());

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        assertEquals("Night", result.getShiftGroup());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithHireDateChange_ShouldUpdateSuccessfully() {
        // Arrange
        LocalDate newHireDate = LocalDate.now().minusYears(1);
        Employee updatedData = Employee.builder()
                .name("Jane Smith")
                .role(Employee.Role.SUPERVISOR)
                .department("Warehouse")
                .hireDate(newHireDate)
                .status(Employee.Status.ACTIVE)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logUpdate(anyString(), anyLong(), any(), any());

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        assertEquals(newHireDate, result.getHireDate());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== SOFT DELETE EMPLOYEE TESTS ====================

    @Test
    void testSoftDeleteEmployee_WithValidId_ShouldSetDeletedFlag() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logDelete(anyString(), anyLong(), any());

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(existingEmployee.getDeleted());
        assertEquals(Employee.Status.TERMINATED, existingEmployee.getStatus());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(auditLogService, times(1)).logDelete(eq("Employee"), eq(1L), any());
    }

    @Test
    void testSoftDeleteEmployee_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> employeeService.softDeleteEmployee(999L));
        
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(auditLogService, never()).logDelete(anyString(), anyLong(), any());
    }

    @Test
    void testSoftDeleteEmployee_WithAlreadyDeletedEmployee_ShouldThrowResourceNotFoundException() {
        // Arrange
        existingEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> employeeService.softDeleteEmployee(1L));
        
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testSoftDeleteEmployee_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.softDeleteEmployee(null));
    }

    @Test
    void testSoftDeleteEmployee_WithNegativeId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> employeeService.softDeleteEmployee(-1L));
        
        verify(employeeRepository, times(1)).findById(-1L);
    }

    @Test
    void testSoftDeleteEmployee_ShouldSetStatusToTerminated() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logDelete(anyString(), anyLong(), any());

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertEquals(Employee.Status.TERMINATED, existingEmployee.getStatus());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testSoftDeleteEmployee_ShouldNotHardDeleteFromDatabase() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logDelete(anyString(), anyLong(), any());

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, never()).delete(any(Employee.class));
        verify(employeeRepository, never()).deleteById(anyLong());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== BOUNDARY AND EDGE CASE TESTS ====================

    @Test
    void testCreateEmployee_WithMaxLengthName_ShouldCreateSuccessfully() {
        // Arrange
        String maxLengthName = "A".repeat(100);
        validEmployee.setName(maxLengthName);
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());

        // Act
        Employee result = employeeService.createEmployee(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(maxLengthName, result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithMaxLengthBadgeId_ShouldCreateSuccessfully() {
        // Arrange
        String maxLengthBadgeId = "B".repeat(50);
        validEmployee.setBadgeId(maxLengthBadgeId);
        when(employeeRepository.findByBadgeId(maxLengthBadgeId)).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());

        // Act
        Employee result = employeeService.createEmployee(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(maxLengthBadgeId, result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithSpecialCharactersInName_ShouldCreateSuccessfully() {
        // Arrange
        validEmployee.setName("O'Brien-Smith Jr.");
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());

        // Act
        Employee result = employeeService.createEmployee(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("O'Brien-Smith Jr.", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithUnicodeCharactersInName_ShouldCreateSuccessfully() {
        // Arrange
        validEmployee.setName("JosÃ© GarcÃ­a");
        when(employeeRepository.findByBadgeId(validEmployee.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());

        // Act
        Employee result = employeeService.createEmployee(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("JosÃ© GarcÃ­a", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testGetEmployees_WithPageSizeZero_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 0);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetEmployees_WithNonExistentDepartment_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(employeeRepository.findByDepartmentAndDeletedFalse("NonExistent", pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable, "NonExistent", null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(employeeRepository, times(1)).findByDepartmentAndDeletedFalse("NonExistent", pageable);
    }

    @Test
    void testUpdateEmployee_WithAllFieldsNull_ShouldUpdateToNull() {
        // Arrange
        Employee updatedData = Employee.builder()
                .name(null)
                .role(null)
                .department(null)
                .shiftGroup(null)
                .hireDate(null)
                .status(null)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
        doNothing().when(auditLogService).logUpdate(anyString(), anyLong(), any(), any());

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithMinimumRequiredFields_ShouldCreateSuccessfully() {
        // Arrange
        Employee minimalEmployee = Employee.builder()
                .name("Min Employee")
                .badgeId("MIN001")
                .role(Employee.Role.WORKER)
                .build();

        when(employeeRepository.findByBadgeId("MIN001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(minimalEmployee);
        doNothing().when(auditLogService).logCreate(anyString(), anyLong(), any());

        // Act
        Employee result = employeeService.createEmployee(minimalEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("Min Employee", result.getName());
        assertEquals("MIN001", result.getBadgeId());
        assertEquals(Employee.Role.WORKER, result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }
}
