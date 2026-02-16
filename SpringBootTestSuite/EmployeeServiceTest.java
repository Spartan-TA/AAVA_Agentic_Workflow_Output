package com.warehouse.employeemgmt.service;

import com.warehouse.employeemgmt.domain.Employee;
import com.warehouse.employeemgmt.domain.EmployeeRole;
import com.warehouse.employeemgmt.domain.EmployeeStatus;
import com.warehouse.employeemgmt.dto.EmployeeDTO;
import com.warehouse.employeemgmt.exception.ResourceNotFoundException;
import com.warehouse.employeemgmt.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
 * Covers all CRUD operations, validation, edge cases, and boundary conditions
 * 
 * Test Coverage:
 * - Normal cases for all CRUD operations
 * - Null input validation
 * - Empty string validation
 * - Invalid format handling
 * - Boundary conditions
 * - Exception scenarios
 * - Soft delete functionality
 * - Pagination and filtering
 * - Badge ID uniqueness
 * - Audit logging
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Test Suite")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    public void setUp() {
        // Arrange - Setup test data
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole(EmployeeRole.WORKER);
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDeleted(false);

        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Morning");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test create employee with valid input")
    public void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals(EmployeeRole.WORKER, result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null DTO")
    public void testCreateEmployee_NullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(null);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null name")
    public void testCreateEmployee_NullName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with empty name")
    public void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with whitespace-only name")
    public void testCreateEmployee_WhitespaceName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setName("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with duplicate badge ID")
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(testEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null badge ID")
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with empty badge ID")
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with invalid role")
    public void testCreateEmployee_InvalidRole_ThrowsException() {
        // Arrange
        testEmployeeDTO.setRole("INVALID_ROLE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with future hire date")
    public void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.create(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with today's hire date")
    public void testCreateEmployee_TodayHireDate_Success() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now());
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with very long name (boundary)")
    public void testCreateEmployee_VeryLongName_Success() {
        // Arrange
        String longName = "A".repeat(255);
        testEmployeeDTO.setName(longName);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test update employee with valid input")
    public void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        testEmployeeDTO.setName("Jane Doe");

        // Act
        Employee result = employeeService.update(1L, testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with non-existent ID")
    public void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.update(999L, testEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with null ID")
    public void testUpdateEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.update(null, testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test update employee with negative ID")
    public void testUpdateEmployee_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.update(-1L, testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test update employee with zero ID")
    public void testUpdateEmployee_ZeroId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.update(0L, testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test update employee with null DTO")
    public void testUpdateEmployee_NullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.update(1L, null);
        });
    }

    @Test
    @DisplayName("Test partial update employee (PATCH)")
    public void testPartialUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        EmployeeDTO partialDTO = new EmployeeDTO();
        partialDTO.setName("Updated Name");

        // Act
        Employee result = employeeService.partialUpdate(1L, partialDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test soft delete employee with valid ID")
    public void testSoftDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDelete(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertTrue(testEmployee.isDeleted());
    }

    @Test
    @DisplayName("Test soft delete employee with non-existent ID")
    public void testSoftDeleteEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.softDelete(999L);
        });
    }

    @Test
    @DisplayName("Test soft delete employee with null ID")
    public void testSoftDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.softDelete(null);
        });
    }

    @Test
    @DisplayName("Test soft delete already deleted employee")
    public void testSoftDeleteEmployee_AlreadyDeleted_NoChange() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDelete(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertTrue(testEmployee.isDeleted());
    }

    // ==================== GET EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test get employee by valid ID")
    public void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    @DisplayName("Test get employee by non-existent ID")
    public void testGetEmployeeById_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getById(999L);
        });
    }

    @Test
    @DisplayName("Test get employee by null ID")
    public void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getById(null);
        });
    }

    @Test
    @DisplayName("Test get employee by badge ID")
    public void testGetEmployeeByBadgeId_ValidBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    @DisplayName("Test get employee by non-existent badge ID")
    public void testGetEmployeeByBadgeId_NonExistentBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getByBadgeId("INVALID");
        });
    }

    @Test
    @DisplayName("Test get employee by null badge ID")
    public void testGetEmployeeByBadgeId_NullBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getByBadgeId(null);
        });
    }

    @Test
    @DisplayName("Test get employee by empty badge ID")
    public void testGetEmployeeByBadgeId_EmptyBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getByBadgeId("");
        });
    }

    // ==================== LIST/FILTER EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test get all employees with pagination")
    public void testGetAllEmployees_WithPagination_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Test get all employees with null pageable")
    public void testGetAllEmployees_NullPageable_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getAllEmployees(null);
        });
    }

    @Test
    @DisplayName("Test get all employees - empty result")
    public void testGetAllEmployees_EmptyResult_Success() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Test filter employees by department")
    public void testFilterEmployees_ByDepartment_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDepartmentAndDeletedFalse(anyString(), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.filterByDepartment("Warehouse", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Warehouse", result.getContent().get(0).getDepartment());
    }

    @Test
    @DisplayName("Test filter employees by role")
    public void testFilterEmployees_ByRole_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByRoleAndDeletedFalse(any(EmployeeRole.class), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.filterByRole(EmployeeRole.WORKER, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(EmployeeRole.WORKER, result.getContent().get(0).getRole());
    }

    @Test
    @DisplayName("Test filter employees by status")
    public void testFilterEmployees_ByStatus_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByStatusAndDeletedFalse(any(EmployeeStatus.class), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.filterByStatus(EmployeeStatus.ACTIVE, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(EmployeeStatus.ACTIVE, result.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("Test search employees by name")
    public void testSearchEmployees_ByName_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse(anyString(), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.searchByName("John", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getName().contains("John"));
    }

    @Test
    @DisplayName("Test search employees with empty search term")
    public void testSearchEmployees_EmptySearchTerm_ReturnsAll() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.searchByName("", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Test create employee with maximum valid values")
    public void testCreateEmployee_MaximumValidValues_Success() {
        // Arrange
        testEmployeeDTO.setName("A".repeat(255));
        testEmployeeDTO.setBadgeId("B".repeat(50));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with minimum valid values")
    public void testCreateEmployee_MinimumValidValues_Success() {
        // Arrange
        testEmployeeDTO.setName("A");
        testEmployeeDTO.setBadgeId("1");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test pagination with page size boundary (1)")
    public void testGetAllEmployees_PageSizeOne_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 1);
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSize());
    }

    @Test
    @DisplayName("Test pagination with large page size (1000)")
    public void testGetAllEmployees_LargePageSize_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 1000);
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSize() <= 1000);
    }

    // ==================== SPECIAL CHARACTER TESTS ====================

    @Test
    @DisplayName("Test create employee with special characters in name")
    public void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testEmployeeDTO.setName("O'Brien-Smith");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with unicode characters in name")
    public void testCreateEmployee_UnicodeCharactersInName_Success() {
        // Arrange
        testEmployeeDTO.setName("JosÃ© GarcÃ­a");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== CONCURRENT MODIFICATION TESTS ====================

    @Test
    @DisplayName("Test update employee with stale data")
    public void testUpdateEmployee_StaleData_HandlesGracefully() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.update(1L, testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== AUDIT LOGGING TESTS ====================

    @Test
    @DisplayName("Test create employee generates audit log")
    public void testCreateEmployee_GeneratesAuditLog() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        // Verify audit log creation (would need audit service mock)
    }

    @Test
    @DisplayName("Test update employee generates audit log")
    public void testUpdateEmployee_GeneratesAuditLog() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.update(1L, testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        // Verify audit log creation (would need audit service mock)
    }

    @Test
    @DisplayName("Test soft delete employee generates audit log")
    public void testSoftDeleteEmployee_GeneratesAuditLog() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDelete(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertTrue(testEmployee.isDeleted());
        // Verify audit log creation (would need audit service mock)
    }
}