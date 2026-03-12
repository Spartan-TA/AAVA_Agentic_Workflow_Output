package com.wms.employee.service;

import com.wms.employee.domain.Employee;
import com.wms.employee.domain.Role;
import com.wms.employee.domain.Status;
import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.repository.EmployeeRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal operations, boundary conditions, and edge cases
 */
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee testEmployee;
    private EmployeeDto testEmployeeDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Arrange - Setup test data
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole(Role.WORKER);
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus(Status.ACTIVE);
        testEmployee.setDeleted(false);

        testEmployeeDto = new EmployeeDto();
        testEmployeeDto.setBadgeId("EMP001");
        testEmployeeDto.setName("John Doe");
        testEmployeeDto.setRole("WORKER");
        testEmployeeDto.setDepartment("Warehouse");
        testEmployeeDto.setShiftGroup("A");
        testEmployeeDto.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDto.setStatus("ACTIVE");
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test create employee with valid data")
    public void testCreateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null badge ID throws exception")
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDto.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with empty badge ID throws exception")
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDto.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test create employee with duplicate badge ID throws exception")
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null name throws exception")
    public void testCreateEmployee_NullName_ThrowsException() {
        // Arrange
        testEmployeeDto.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test create employee with invalid role throws exception")
    public void testCreateEmployee_InvalidRole_ThrowsException() {
        // Arrange
        testEmployeeDto.setRole("INVALID_ROLE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test create employee with future hire date throws exception")
    public void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        testEmployeeDto.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test get employee by valid ID")
    public void testGetEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDto result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("Test get employee with non-existent ID throws exception")
    public void testGetEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(999L);
        });
    }

    @Test
    @DisplayName("Test get employee with null ID throws exception")
    public void testGetEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(null);
        });
    }

    @Test
    @DisplayName("Test get employee with negative ID throws exception")
    public void testGetEmployee_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(-1L);
        });
    }

    @Test
    @DisplayName("Test get deleted employee returns null or throws exception")
    public void testGetEmployee_DeletedEmployee_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.getEmployee(1L);
        });
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test update employee with valid data")
    public void testUpdateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        testEmployeeDto.setName("Jane Doe");
        testEmployeeDto.setDepartment("Shipping");

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, testEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with null ID throws exception")
    public void testUpdateEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(null, testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test update employee with null DTO throws exception")
    public void testUpdateEmployee_NullDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    @DisplayName("Test update non-existent employee throws exception")
    public void testUpdateEmployee_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test update employee badge ID to duplicate throws exception")
    public void testUpdateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        Employee anotherEmployee = new Employee();
        anotherEmployee.setId(2L);
        anotherEmployee.setBadgeId("EMP002");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP002")).thenReturn(Optional.of(anotherEmployee));
        
        testEmployeeDto.setBadgeId("EMP002");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.updateEmployee(1L, testEmployeeDto);
        });
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test soft delete employee with valid ID")
    public void testDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertTrue(testEmployee.isDeleted());
    }

    @Test
    @DisplayName("Test delete employee with null ID throws exception")
    public void testDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    @Test
    @DisplayName("Test delete non-existent employee throws exception")
    public void testDeleteEmployee_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    @DisplayName("Test delete already deleted employee throws exception")
    public void testDeleteEmployee_AlreadyDeleted_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.deleteEmployee(1L);
        });
    }

    // ========== LIST EMPLOYEES TESTS ==========

    @Test
    @DisplayName("Test list all employees with pagination")
    public void testListEmployees_WithPagination_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.listEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(any(Pageable.class));
    }

    @Test
    @DisplayName("Test list employees with null pageable uses default")
    public void testListEmployees_NullPageable_UsesDefault() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.listEmployees(null);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findAllByDeletedFalse(any(Pageable.class));
    }

    @Test
    @DisplayName("Test list employees with filter by department")
    public void testListEmployees_FilterByDepartment_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(employeeRepository.findByDepartmentAndDeletedFalse(anyString(), any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.listEmployeesByDepartment("Warehouse", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Test list employees with filter by status")
    public void testListEmployees_FilterByStatus_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(employeeRepository.findByStatusAndDeletedFalse(any(Status.class), any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.listEmployeesByStatus(Status.ACTIVE, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Test list employees returns empty page when no results")
    public void testListEmployees_NoResults_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = Page.empty();
        Pageable pageable = PageRequest.of(0, 20);
        
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.listEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    @DisplayName("Test find employee by valid badge ID")
    public void testFindByBadgeId_ValidBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDto result = employeeService.findByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    @DisplayName("Test find employee by null badge ID throws exception")
    public void testFindByBadgeId_NullBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findByBadgeId(null);
        });
    }

    @Test
    @DisplayName("Test find employee by empty badge ID throws exception")
    public void testFindByBadgeId_EmptyBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findByBadgeId("");
        });
    }

    @Test
    @DisplayName("Test find employee by non-existent badge ID throws exception")
    public void testFindByBadgeId_NonExistentBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findByBadgeId("NONEXISTENT");
        });
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test create employee with maximum length name")
    public void testCreateEmployee_MaxLengthName_Success() {
        // Arrange
        String maxLengthName = "A".repeat(255);
        testEmployeeDto.setName(maxLengthName);
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with special characters in name")
    public void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testEmployeeDto.setName("O'Brien-Smith");
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with all valid roles")
    public void testCreateEmployee_AllValidRoles_Success() {
        // Test each valid role
        String[] validRoles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"};
        
        for (String role : validRoles) {
            // Arrange
            testEmployeeDto.setRole(role);
            testEmployeeDto.setBadgeId("EMP" + role);
            when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            // Act
            EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Test create employee with all valid statuses")
    public void testCreateEmployee_AllValidStatuses_Success() {
        // Test each valid status
        String[] validStatuses = {"ACTIVE", "INACTIVE", "TERMINATED"};
        
        for (String status : validStatuses) {
            // Arrange
            testEmployeeDto.setStatus(status);
            testEmployeeDto.setBadgeId("EMP" + status);
            when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            // Act
            EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Test create employee with hire date at boundary (today)")
    public void testCreateEmployee_HireDateToday_Success() {
        // Arrange
        testEmployeeDto.setHireDate(LocalDate.now());
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with very old hire date")
    public void testCreateEmployee_VeryOldHireDate_Success() {
        // Arrange
        testEmployeeDto.setHireDate(LocalDate.of(1980, 1, 1));
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }
}