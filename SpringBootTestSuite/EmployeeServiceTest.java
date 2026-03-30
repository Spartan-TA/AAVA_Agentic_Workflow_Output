package com.companyname.wems.employee.service;

import com.companyname.wems.employee.dto.EmployeeRequest;
import com.companyname.wems.employee.dto.EmployeeResponse;
import com.companyname.wems.employee.entity.Employee;
import com.companyname.wems.employee.repository.EmployeeRepository;
import com.companyname.wems.exception.DuplicateResourceException;
import com.companyname.wems.exception.ResourceNotFoundException;
import com.companyname.wems.exception.BusinessException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal operations, boundary conditions, edge cases, and exception scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;
    private EmployeeRequest validRequest;

    @BeforeEach
    void setUp() {
        // Arrange: Set up test data
        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP12345")
                .role(Employee.Role.WORKER)
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status(Employee.Status.ACTIVE)
                .build();

        validRequest = EmployeeRequest.builder()
                .name("John Doe")
                .badgeId("EMP12345")
                .role(Employee.Role.WORKER)
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .build();
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Should create employee with valid input")
    void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponse response = employeeService.createEmployee(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        assertEquals("EMP12345", response.getBadgeId());
        assertEquals(Employee.Role.WORKER, response.getRole());
        verify(employeeRepository, times(1)).existsByBadgeId("EMP12345");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when badge ID already exists")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP12345")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, times(1)).existsByBadgeId("EMP12345");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when name is null")
    void testCreateEmployee_NullName_ThrowsException() {
        // Arrange
        validRequest.setName(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when name is empty string")
    void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        validRequest.setName("");

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when name exceeds maximum length")
    void testCreateEmployee_NameTooLong_ThrowsException() {
        // Arrange
        String longName = "A".repeat(101); // Exceeds 100 character limit
        validRequest.setName(longName);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when badge ID is null")
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        validRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when badge ID has invalid format")
    void testCreateEmployee_InvalidBadgeIdFormat_ThrowsException() {
        // Arrange
        validRequest.setBadgeId("invalid-badge"); // Does not match pattern ^[A-Z0-9]{5,20}$

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when badge ID is too short")
    void testCreateEmployee_BadgeIdTooShort_ThrowsException() {
        // Arrange
        validRequest.setBadgeId("EMP1"); // Less than 5 characters

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when badge ID is too long")
    void testCreateEmployee_BadgeIdTooLong_ThrowsException() {
        // Arrange
        validRequest.setBadgeId("EMP" + "1".repeat(20)); // More than 20 characters

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when role is null")
    void testCreateEmployee_NullRole_ThrowsException() {
        // Arrange
        validRequest.setRole(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when hire date is in the future")
    void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        validRequest.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    @DisplayName("Should create employee with minimum valid badge ID length")
    void testCreateEmployee_MinimumBadgeIdLength_Success() {
        // Arrange
        validRequest.setBadgeId("EMP12"); // Exactly 5 characters
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponse response = employeeService.createEmployee(validRequest);

        // Assert
        assertNotNull(response);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should create employee with maximum valid badge ID length")
    void testCreateEmployee_MaximumBadgeIdLength_Success() {
        // Arrange
        validRequest.setBadgeId("EMP" + "1".repeat(17)); // Exactly 20 characters
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponse response = employeeService.createEmployee(validRequest);

        // Assert
        assertNotNull(response);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should create employee with hire date as today")
    void testCreateEmployee_HireDateToday_Success() {
        // Arrange
        validRequest.setHireDate(LocalDate.now());
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponse response = employeeService.createEmployee(validRequest);

        // Assert
        assertNotNull(response);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Should retrieve employee by valid ID")
    void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act
        EmployeeResponse response = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee ID does not exist")
    void testGetEmployeeById_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should throw BusinessException when employee ID is null")
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    @DisplayName("Should throw BusinessException when employee ID is negative")
    void testGetEmployeeById_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    @DisplayName("Should retrieve employee by valid badge ID")
    void testGetEmployeeByBadgeId_ValidBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP12345")).thenReturn(Optional.of(validEmployee));

        // Act
        EmployeeResponse response = employeeService.getEmployeeByBadgeId("EMP12345");

        // Assert
        assertNotNull(response);
        assertEquals("EMP12345", response.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("EMP12345");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when badge ID does not exist")
    void testGetEmployeeByBadgeId_NonExistentBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Should update employee with valid input")
    void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        EmployeeRequest updateRequest = EmployeeRequest.builder()
                .name("Jane Doe")
                .department("Receiving")
                .shiftGroup("Evening")
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponse response = employeeService.updateEmployee(1L, updateRequest);

        // Assert
        assertNotNull(response);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent employee")
    void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, validRequest);
        });
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating to existing badge ID")
    void testUpdateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        EmployeeRequest updateRequest = EmployeeRequest.builder()
                .badgeId("EMP99999")
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.existsByBadgeId("EMP99999")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.updateEmployee(1L, updateRequest);
        });
    }

    // ========== DELETE EMPLOYEE TESTS (SOFT DELETE) ==========

    @Test
    @DisplayName("Should soft delete employee successfully")
    void testDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(argThat(emp -> 
            emp.getStatus() == Employee.Status.DELETED
        ));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent employee")
    void testDeleteEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    @DisplayName("Should not physically delete employee from database")
    void testDeleteEmployee_SoftDeleteOnly_NoPhysicalDeletion() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, never()).delete(any(Employee.class));
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    // ========== LIST EMPLOYEES TESTS ==========

    @Test
    @DisplayName("Should list all employees with pagination")
    void testListEmployees_WithPagination_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeResponse> response = employeeService.listEmployees(pageable, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should filter employees by department")
    void testListEmployees_FilterByDepartment_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDepartment("Shipping", pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeResponse> response = employeeService.listEmployees(pageable, "Shipping", null);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(employeeRepository, times(1)).findByDepartment("Shipping", pageable);
    }

    @Test
    @DisplayName("Should filter employees by status")
    void testListEmployees_FilterByStatus_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByStatus(Employee.Status.ACTIVE, pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeResponse> response = employeeService.listEmployees(pageable, null, Employee.Status.ACTIVE);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(employeeRepository, times(1)).findByStatus(Employee.Status.ACTIVE, pageable);
    }

    @Test
    @DisplayName("Should filter employees by department and status")
    void testListEmployees_FilterByDepartmentAndStatus_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDepartmentAndStatus("Shipping", Employee.Status.ACTIVE, pageable))
                .thenReturn(employeePage);

        // Act
        Page<EmployeeResponse> response = employeeService.listEmployees(pageable, "Shipping", Employee.Status.ACTIVE);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(employeeRepository, times(1)).findByDepartmentAndStatus("Shipping", Employee.Status.ACTIVE, pageable);
    }

    @Test
    @DisplayName("Should return empty page when no employees match filter")
    void testListEmployees_NoMatches_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDepartment("NonExistent", pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponse> response = employeeService.listEmployees(pageable, "NonExistent", null);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalElements());
        assertTrue(response.isEmpty());
    }

    // ========== SEARCH EMPLOYEES TESTS ==========

    @Test
    @DisplayName("Should search employees by name")
    void testSearchEmployees_ByName_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByNameContainingIgnoreCase("John", pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeResponse> response = employeeService.searchEmployees("John", pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(employeeRepository, times(1)).findByNameContainingIgnoreCase("John", pageable);
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testSearchEmployees_EmptyQuery_ReturnsAllEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeResponse> response = employeeService.searchEmployees("", pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    // ========== COUNT EMPLOYEES TESTS ==========

    @Test
    @DisplayName("Should count employees by department")
    void testCountEmployeesByDepartment_Success() {
        // Arrange
        when(employeeRepository.countByDepartment("Shipping")).thenReturn(5L);

        // Act
        Long count = employeeService.countEmployeesByDepartment("Shipping");

        // Assert
        assertEquals(5L, count);
        verify(employeeRepository, times(1)).countByDepartment("Shipping");
    }

    @Test
    @DisplayName("Should count employees by status")
    void testCountEmployeesByStatus_Success() {
        // Arrange
        when(employeeRepository.countByStatus(Employee.Status.ACTIVE)).thenReturn(10L);

        // Act
        Long count = employeeService.countEmployeesByStatus(Employee.Status.ACTIVE);

        // Assert
        assertEquals(10L, count);
        verify(employeeRepository, times(1)).countByStatus(Employee.Status.ACTIVE);
    }

    @Test
    @DisplayName("Should return zero when no employees in department")
    void testCountEmployeesByDepartment_NoEmployees_ReturnsZero() {
        // Arrange
        when(employeeRepository.countByDepartment("NonExistent")).thenReturn(0L);

        // Act
        Long count = employeeService.countEmployeesByDepartment("NonExistent");

        // Assert
        assertEquals(0L, count);
    }

    // ========== ROLE VALIDATION TESTS ==========

    @Test
    @DisplayName("Should accept all valid roles")
    void testCreateEmployee_AllValidRoles_Success() {
        // Test ADMIN role
        validRequest.setRole(Employee.Role.ADMIN);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        assertDoesNotThrow(() -> employeeService.createEmployee(validRequest));

        // Test HR role
        validRequest.setRole(Employee.Role.HR);
        assertDoesNotThrow(() -> employeeService.createEmployee(validRequest));

        // Test SUPERVISOR role
        validRequest.setRole(Employee.Role.SUPERVISOR);
        assertDoesNotThrow(() -> employeeService.createEmployee(validRequest));

        // Test WORKER role
        validRequest.setRole(Employee.Role.WORKER);
        assertDoesNotThrow(() -> employeeService.createEmployee(validRequest));
    }

    // ========== STATUS VALIDATION TESTS ==========

    @Test
    @DisplayName("Should handle all valid status values")
    void testEmployeeStatus_AllValidStatuses_Success() {
        // Test ACTIVE status
        validEmployee.setStatus(Employee.Status.ACTIVE);
        assertEquals(Employee.Status.ACTIVE, validEmployee.getStatus());

        // Test INACTIVE status
        validEmployee.setStatus(Employee.Status.INACTIVE);
        assertEquals(Employee.Status.INACTIVE, validEmployee.getStatus());

        // Test DELETED status
        validEmployee.setStatus(Employee.Status.DELETED);
        assertEquals(Employee.Status.DELETED, validEmployee.getStatus());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle special characters in name")
    void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        validRequest.setName("O'Brien-Smith Jr.");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.createEmployee(validRequest));
    }

    @Test
    @DisplayName("Should handle Unicode characters in name")
    void testCreateEmployee_UnicodeCharactersInName_Success() {
        // Arrange
        validRequest.setName("JosÃ© GarcÃ­a");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.createEmployee(validRequest));
    }

    @Test
    @DisplayName("Should handle very old hire date")
    void testCreateEmployee_VeryOldHireDate_Success() {
        // Arrange
        validRequest.setHireDate(LocalDate.of(1980, 1, 1));
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.createEmployee(validRequest));
    }

    @Test
    @DisplayName("Should handle null optional fields")
    void testCreateEmployee_NullOptionalFields_Success() {
        // Arrange
        validRequest.setDepartment(null);
        validRequest.setShiftGroup(null);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.createEmployee(validRequest));
    }

    @Test
    @DisplayName("Should handle concurrent badge ID creation attempts")
    void testCreateEmployee_ConcurrentBadgeIdCreation_ThrowsException() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP12345"))
                .thenReturn(false)
                .thenReturn(true);

        // Act & Assert
        // First call should succeed
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        assertDoesNotThrow(() -> employeeService.createEmployee(validRequest));

        // Second call should fail due to duplicate
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }
}