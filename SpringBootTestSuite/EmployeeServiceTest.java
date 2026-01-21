package com.wms.employee.service;

import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.dto.CreateEmployeeRequest;
import com.wms.employee.dto.UpdateEmployeeRequest;
import com.wms.common.Role;
import com.wms.exception.ResourceNotFoundException;
import com.wms.exception.ConflictException;
import com.wms.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Covers normal cases, boundary conditions, and edge cases
 */
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private CreateEmployeeRequest createRequest;
    private UpdateEmployeeRequest updateRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("BADGE001");
        testEmployee.setRole(Role.WORKER);
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setSoftDelete(false);
        
        // Setup create request
        createRequest = new CreateEmployeeRequest();
        createRequest.setName("Jane Smith");
        createRequest.setBadgeId("BADGE002");
        createRequest.setRole(Role.WORKER);
        createRequest.setDepartment("Warehouse");
        createRequest.setShiftGroup("B");
        createRequest.setHireDate(LocalDate.of(2023, 6, 1));
        
        // Setup update request
        updateRequest = new UpdateEmployeeRequest();
        updateRequest.setName("John Updated");
        updateRequest.setDepartment("Logistics");
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test create employee with valid data")
    public void testCreateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("BADGE001", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with duplicate badge ID")
    public void testCreateEmployee_DuplicateBadgeId_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null name")
    public void testCreateEmployee_NullName_ThrowsBadRequestException() {
        // Arrange
        createRequest.setName(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Test create employee with empty badge ID")
    public void testCreateEmployee_EmptyBadgeId_ThrowsBadRequestException() {
        // Arrange
        createRequest.setBadgeId("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Test create employee with future hire date")
    public void testCreateEmployee_FutureHireDate_Success() {
        // Arrange
        createRequest.setHireDate(LocalDate.now().plusDays(30));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with past hire date")
    public void testCreateEmployee_PastHireDate_Success() {
        // Arrange
        createRequest.setHireDate(LocalDate.of(2020, 1, 1));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== READ EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test get employee by valid ID")
    public void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDto result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test get employee by non-existent ID")
    public void testGetEmployeeById_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    @DisplayName("Test get employee by null ID")
    public void testGetEmployeeById_NullId_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    @DisplayName("Test get employee by negative ID")
    public void testGetEmployeeById_NegativeId_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    @DisplayName("Test get employee by badge ID")
    public void testGetEmployeeByBadgeId_ValidBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE001")).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDto result = employeeService.getEmployeeByBadgeId("BADGE001");

        // Assert
        assertNotNull(result);
        assertEquals("BADGE001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("BADGE001");
    }

    @Test
    @DisplayName("Test get all employees with pagination")
    public void testGetAllEmployees_WithPagination_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test get all employees with empty result")
    public void testGetAllEmployees_EmptyResult_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test update employee with valid data")
    public void testUpdateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update non-existent employee")
    public void testUpdateEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, updateRequest);
        });
    }

    @Test
    @DisplayName("Test update employee with null request")
    public void testUpdateEmployee_NullRequest_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    @DisplayName("Test partial update employee")
    public void testUpdateEmployee_PartialUpdate_Success() {
        // Arrange
        UpdateEmployeeRequest partialUpdate = new UpdateEmployeeRequest();
        partialUpdate.setName("Updated Name Only");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test soft delete employee")
    public void testDeleteEmployee_ValidId_SoftDeleteSuccess() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.isSoftDelete());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    @DisplayName("Test delete non-existent employee")
    public void testDeleteEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    @DisplayName("Test delete already deleted employee")
    public void testDeleteEmployee_AlreadyDeleted_ThrowsConflictException() {
        // Arrange
        testEmployee.setSoftDelete(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            employeeService.deleteEmployee(1L);
        });
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test create employee with maximum length name")
    public void testCreateEmployee_MaxLengthName_Success() {
        // Arrange
        String maxName = "A".repeat(255);
        createRequest.setName(maxName);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with special characters in name")
    public void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        createRequest.setName("O'Brien-Smith Jr.");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with whitespace-only name")
    public void testCreateEmployee_WhitespaceOnlyName_ThrowsBadRequestException() {
        // Arrange
        createRequest.setName("   ");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Test get employees with large page size")
    public void testGetAllEmployees_LargePageSize_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get employees with zero page size")
    public void testGetAllEmployees_ZeroPageSize_ThrowsBadRequestException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 0);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.getAllEmployees(pageable);
        });
    }

    @Test
    @DisplayName("Test create employee with all roles")
    public void testCreateEmployee_AllRoles_Success() {
        // Test each role
        for (Role role : Role.values()) {
            createRequest.setRole(role);
            createRequest.setBadgeId("BADGE_" + role.name());
            when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            EmployeeDto result = employeeService.createEmployee(createRequest);
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Test concurrent employee creation")
    public void testCreateEmployee_ConcurrentCreation_HandlesRaceCondition() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE001"))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(testEmployee));

        // Act & Assert
        // First call should succeed
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        EmployeeDto result1 = employeeService.createEmployee(createRequest);
        assertNotNull(result1);

        // Second call with same badge should fail
        assertThrows(ConflictException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }
}