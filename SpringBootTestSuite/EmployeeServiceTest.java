package com.company.wms.employee;

import com.company.wms.exception.DuplicateResourceException;
import com.company.wms.exception.ResourceNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService
 * 
 * Tests cover:
 * - Normal operations
 * - Boundary conditions
 * - Edge cases
 * - Exception handling
 * - Null input validation
 * - Duplicate resource handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setPhone("+1234567890");
        testEmployee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setStatus("ACTIVE");
        testEmployee.setActive(true);
        testEmployee.setPassword("password123");

        pageable = PageRequest.of(0, 20);
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Should return all employees with pagination - Normal case")
    void testGetAllEmployees_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testEmployee.getBadgeId(), result.getContent().get(0).getBadgeId());
        verify(employeeRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when no employees exist")
    void testGetAllEmployees_EmptyResult() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should handle null pageable gracefully")
    void testGetAllEmployees_NullPageable() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.getAllEmployees(null);
        });
    }

    // ==================== GET ACTIVE EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Should return only active employees")
    void testGetActiveEmployees_Success() {
        // Arrange
        List<Employee> activeEmployees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(activeEmployees, pageable, 1);
        when(employeeRepository.findAllByActiveTrue(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.getActiveEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isActive());
        verify(employeeRepository, times(1)).findAllByActiveTrue(any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when no active employees")
    void testGetActiveEmployees_NoActiveEmployees() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findAllByActiveTrue(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getActiveEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    @DisplayName("Should return employee when valid ID provided")
    void testGetEmployeeById_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee.getId(), result.getId());
        assertEquals(testEmployee.getBadgeId(), result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee not found")
    void testGetEmployeeById_NotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should throw exception when null ID provided")
    void testGetEmployeeById_NullId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    @DisplayName("Should handle negative ID")
    void testGetEmployeeById_NegativeId() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    @DisplayName("Should handle zero ID")
    void testGetEmployeeById_ZeroId() {
        // Arrange
        when(employeeRepository.findById(0L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(0L);
        });
    }

    // ==================== GET EMPLOYEE BY BADGE ID TESTS ====================

    @Test
    @DisplayName("Should return employee when valid badge ID provided")
    void testGetEmployeeByBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when badge ID not found")
    void testGetEmployeeByBadgeId_NotFound() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
    }

    @Test
    @DisplayName("Should handle null badge ID")
    void testGetEmployeeByBadgeId_NullBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId(null);
        });
    }

    @Test
    @DisplayName("Should handle empty badge ID")
    void testGetEmployeeByBadgeId_EmptyBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId("")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("");
        });
    }

    @Test
    @DisplayName("Should handle whitespace-only badge ID")
    void testGetEmployeeByBadgeId_WhitespaceBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId("   ")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("   ");
        });
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Should create employee successfully with valid data")
    void testCreateEmployee_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee.getBadgeId(), result.getBadgeId());
        verify(employeeRepository, times(1)).existsByBadgeId(anyString());
        verify(employeeRepository, times(1)).existsByEmail(anyString());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when badge ID exists")
    void testCreateEmployee_DuplicateBadgeId() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        verify(employeeRepository, times(1)).existsByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email exists")
    void testCreateEmployee_DuplicateEmail() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        verify(employeeRepository, times(1)).existsByEmail("john.doe@example.com");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle null employee object")
    void testCreateEmployee_NullEmployee() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.createEmployee(null);
        });
    }

    @Test
    @DisplayName("Should set default status when not provided")
    void testCreateEmployee_DefaultStatus() {
        // Arrange
        testEmployee.setStatus(null);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            assertEquals("ACTIVE", emp.getStatus());
            return emp;
        });

        // Act
        employeeService.createEmployee(testEmployee);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should encode password when provided")
    void testCreateEmployee_PasswordEncoding() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            assertEquals("encodedPassword123", emp.getPassword());
            return emp;
        });

        // Act
        employeeService.createEmployee(testEmployee);

        // Assert
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("Should not encode null password")
    void testCreateEmployee_NullPassword() {
        // Arrange
        testEmployee.setPassword(null);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.createEmployee(testEmployee);

        // Assert
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should not encode empty password")
    void testCreateEmployee_EmptyPassword() {
        // Arrange
        testEmployee.setPassword("");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.createEmployee(testEmployee);

        // Assert
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Should update employee successfully with valid data")
    void testUpdateEmployee_Success() {
        // Arrange
        Employee updatedDetails = new Employee();
        updatedDetails.setBadgeId("EMP001");
        updatedDetails.setFirstName("Jane");
        updatedDetails.setLastName("Smith");
        updatedDetails.setEmail("jane.smith@example.com");
        updatedDetails.setPhone("+9876543210");
        updatedDetails.setDateOfBirth(LocalDate.of(1992, 5, 15));
        updatedDetails.setRole("SUPERVISOR");
        updatedDetails.setDepartment("Logistics");
        updatedDetails.setShiftGroup("B");
        updatedDetails.setStatus("ACTIVE");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByEmail("jane.smith@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent employee")
    void testUpdateEmployee_NotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, testEmployee);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating to existing badge ID")
    void testUpdateEmployee_DuplicateBadgeId() {
        // Arrange
        Employee updatedDetails = new Employee();
        updatedDetails.setBadgeId("EMP002");
        updatedDetails.setEmail("john.doe@example.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeId("EMP002")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.updateEmployee(1L, updatedDetails);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating to existing email")
    void testUpdateEmployee_DuplicateEmail() {
        // Arrange
        Employee updatedDetails = new Employee();
        updatedDetails.setBadgeId("EMP001");
        updatedDetails.setEmail("existing@example.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.updateEmployee(1L, updatedDetails);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle null ID in update")
    void testUpdateEmployee_NullId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.updateEmployee(null, testEmployee);
        });
    }

    @Test
    @DisplayName("Should handle null employee details in update")
    void testUpdateEmployee_NullDetails() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Should soft delete employee successfully")
    void testDeleteEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            assertFalse(emp.isActive());
            assertEquals("INACTIVE", emp.getStatus());
            assertNotNull(emp.getTerminationDate());
            return emp;
        });

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent employee")
    void testDeleteEmployee_NotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should handle null ID in delete")
    void testDeleteEmployee_NullId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    // ==================== SEARCH EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Should search employees successfully")
    void testSearchEmployees_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.searchEmployees(anyString(), any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.searchEmployees("John", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).searchEmployees("John", pageable);
    }

    @Test
    @DisplayName("Should return empty result when search term not found")
    void testSearchEmployees_NoResults() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.searchEmployees(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.searchEmployees("NonExistent", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Should handle null search term")
    void testSearchEmployees_NullSearchTerm() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.searchEmployees(isNull(), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.searchEmployees(null, pageable);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle empty search term")
    void testSearchEmployees_EmptySearchTerm() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.searchEmployees(eq(""), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.searchEmployees("", pageable);

        // Assert
        assertNotNull(result);
    }

    // ==================== GET EMPLOYEES BY DEPARTMENT TESTS ====================

    @Test
    @DisplayName("Should return employees by department")
    void testGetEmployeesByDepartment_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAllByDepartment("Warehouse", pageable)).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.getEmployeesByDepartment("Warehouse", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Warehouse", result.getContent().get(0).getDepartment());
    }

    @Test
    @DisplayName("Should return empty result when department has no employees")
    void testGetEmployeesByDepartment_NoEmployees() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findAllByDepartment("NonExistent", pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getEmployeesByDepartment("NonExistent", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ==================== GET EMPLOYEES BY ROLE TESTS ====================

    @Test
    @DisplayName("Should return employees by role")
    void testGetEmployeesByRole_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAllByRole("WORKER", pageable)).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.getEmployeesByRole("WORKER", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("WORKER", result.getContent().get(0).getRole());
    }

    // ==================== GET EMPLOYEES BY SHIFT GROUP TESTS ====================

    @Test
    @DisplayName("Should return employees by shift group")
    void testGetEmployeesByShiftGroup_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findAllByShiftGroup("A")).thenReturn(employees);

        // Act
        List<Employee> result = employeeService.getEmployeesByShiftGroup("A");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getShiftGroup());
    }

    @Test
    @DisplayName("Should return empty list when shift group has no employees")
    void testGetEmployeesByShiftGroup_NoEmployees() {
        // Arrange
        when(employeeRepository.findAllByShiftGroup("Z")).thenReturn(Arrays.asList());

        // Act
        List<Employee> result = employeeService.getEmployeesByShiftGroup("Z");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== COUNT TESTS ====================

    @Test
    @DisplayName("Should return total employee count")
    void testGetEmployeeCount_Success() {
        // Arrange
        when(employeeRepository.count()).thenReturn(100L);

        // Act
        long result = employeeService.getEmployeeCount();

        // Assert
        assertEquals(100L, result);
        verify(employeeRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should return zero when no employees exist")
    void testGetEmployeeCount_Zero() {
        // Arrange
        when(employeeRepository.count()).thenReturn(0L);

        // Act
        long result = employeeService.getEmployeeCount();

        // Assert
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Should return active employee count")
    void testGetActiveEmployeeCount_Success() {
        // Arrange
        when(employeeRepository.countByActiveTrue()).thenReturn(75L);

        // Act
        long result = employeeService.getActiveEmployeeCount();

        // Assert
        assertEquals(75L, result);
        verify(employeeRepository, times(1)).countByActiveTrue();
    }

    @Test
    @DisplayName("Should return zero when no active employees")
    void testGetActiveEmployeeCount_Zero() {
        // Arrange
        when(employeeRepository.countByActiveTrue()).thenReturn(0L);

        // Act
        long result = employeeService.getActiveEmployeeCount();

        // Assert
        assertEquals(0L, result);
    }
}