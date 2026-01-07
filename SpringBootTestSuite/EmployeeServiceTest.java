package com.company.warehouse.core.service;

import com.company.warehouse.core.domain.Employee;
import com.company.warehouse.core.domain.Employee.Role;
import com.company.warehouse.core.domain.Employee.Status;
import com.company.warehouse.core.repository.EmployeeRepository;
import com.company.warehouse.api.dto.EmployeeDTO;
import com.company.warehouse.api.exception.ResourceNotFoundException;
import com.company.warehouse.api.exception.BusinessException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService.
 * Tests cover normal cases, boundary conditions, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        // Arrange - Create test employee
        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role(Role.WORKER)
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status(Status.ACTIVE)
                .email("john.doe@company.com")
                .phone("+1234567890")
                .build();

        testEmployeeDTO = EmployeeDTO.builder()
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2023, 1, 15))
                .email("john.doe@company.com")
                .phone("+1234567890")
                .build();
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Should create employee with valid data")
    void testCreateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when creating employee with duplicate badgeId")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when creating employee with null name")
    void testCreateEmployee_NullName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Should throw exception when creating employee with empty badgeId")
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Should throw exception when creating employee with invalid email format")
    void testCreateEmployee_InvalidEmail_ThrowsException() {
        // Arrange
        testEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Should throw exception when creating employee with future hire date")
    void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Should retrieve employee by valid ID")
    void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when employee not found by ID")
    void testGetEmployeeById_NotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should throw exception when getting employee with null ID")
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    @DisplayName("Should retrieve employee by valid badgeId")
    void testGetEmployeeByBadgeId_ValidBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
    }

    // ========== LIST EMPLOYEES TESTS ==========

    @Test
    @DisplayName("Should list all employees with pagination")
    void testListEmployees_WithPagination_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should filter employees by department")
    void testListEmployees_FilterByDepartment_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findAllByDepartment("Shipping")).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.listEmployeesByDepartment("Shipping");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Shipping", result.get(0).getDepartment());
        verify(employeeRepository, times(1)).findAllByDepartment("Shipping");
    }

    @Test
    @DisplayName("Should return empty list when no employees in department")
    void testListEmployees_EmptyDepartment_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findAllByDepartment("NonExistent")).thenReturn(Arrays.asList());

        // Act
        List<EmployeeDTO> result = employeeService.listEmployeesByDepartment("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findAllByDepartment("NonExistent");
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Should update employee with valid data")
    void testUpdateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        testEmployeeDTO.setName("Jane Doe");

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent employee")
    void testUpdateEmployee_NotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDTO);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with null data")
    void testUpdateEmployee_NullData_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Should soft-delete employee successfully")
    void testDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent employee")
    void testDeleteEmployee_NotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle employee with minimum valid name length")
    void testCreateEmployee_MinimumNameLength_Success() {
        // Arrange
        testEmployeeDTO.setName("A");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle employee with maximum valid name length")
    void testCreateEmployee_MaximumNameLength_Success() {
        // Arrange
        String longName = "A".repeat(255);
        testEmployeeDTO.setName(longName);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle employee with special characters in name")
    void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testEmployeeDTO.setName("O'Brien-Smith");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle employee with hire date as today")
    void testCreateEmployee_HireDateToday_Success() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now());
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle employee with very old hire date")
    void testCreateEmployee_VeryOldHireDate_Success() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.of(1980, 1, 1));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle employee with all roles")
    void testCreateEmployee_AllRoles_Success() {
        // Test each role
        for (Role role : Role.values()) {
            // Arrange
            testEmployeeDTO.setRole(role.name());
            testEmployeeDTO.setBadgeId("EMP" + role.name());
            when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            // Act
            EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Should handle concurrent updates gracefully")
    void testUpdateEmployee_ConcurrentUpdate_HandlesGracefully() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenThrow(new RuntimeException("Optimistic locking failure"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            employeeService.updateEmployee(1L, testEmployeeDTO);
        });
    }
}