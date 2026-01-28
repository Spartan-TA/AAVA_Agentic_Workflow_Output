package com.wms.ems.employee;

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
 * Comprehensive service tests for Employee business logic
 * Tests cover:
 * - CRUD operations with business rules
 * - Caching behavior
 * - Soft delete logic
 * - Validation and error handling
 * - Edge cases and boundary conditions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);

        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Morning");
        testEmployeeDTO.setHireDate(LocalDate.now());
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== CREATE OPERATIONS ==========

    @Test
    @DisplayName("Should create employee successfully")
    public void testCreateEmployee() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when creating employee with duplicate badge ID")
    public void testCreateEmployeeWithDuplicateBadgeId() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when creating employee with null data")
    public void testCreateEmployeeWithNullData() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when creating employee with invalid badge ID")
    public void testCreateEmployeeWithInvalidBadgeId() {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== READ OPERATIONS ==========

    @Test
    @DisplayName("Should get employee by ID successfully")
    public void testGetEmployeeById() {
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
    public void testGetEmployeeByIdNotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    @DisplayName("Should get employee by badge ID successfully")
    public void testGetEmployeeByBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
    }

    @Test
    @DisplayName("Should get all employees successfully")
    public void testGetAllEmployees() {
        // Arrange
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setName("Jane Smith");
        employee2.setBadgeId("EMP002");
        employee2.setRole("SUPERVISOR");
        employee2.setDepartment("Logistics");
        employee2.setHireDate(LocalDate.now());
        employee2.setStatus("ACTIVE");

        List<Employee> employees = Arrays.asList(testEmployee, employee2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get employees by department")
    public void testGetEmployeesByDepartment() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByDepartment("Warehouse")).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.getEmployeesByDepartment("Warehouse");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Warehouse", result.get(0).getDepartment());
        verify(employeeRepository, times(1)).findByDepartment("Warehouse");
    }

    @Test
    @DisplayName("Should get employees by role")
    public void testGetEmployeesByRole() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByRole("WORKER")).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.getEmployeesByRole("WORKER");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WORKER", result.get(0).getRole());
        verify(employeeRepository, times(1)).findByRole("WORKER");
    }

    // ========== UPDATE OPERATIONS ==========

    @Test
    @DisplayName("Should update employee successfully")
    public void testUpdateEmployee() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("John Updated");
        updateDTO.setDepartment("Logistics");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent employee")
    public void testUpdateNonExistentEmployee() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should update employee status")
    public void testUpdateEmployeeStatus() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.updateEmployeeStatus(1L, "INACTIVE");

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== DELETE OPERATIONS ==========

    @Test
    @DisplayName("Should soft delete employee successfully")
    public void testSoftDeleteEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when soft deleting non-existent employee")
    public void testSoftDeleteNonExistentEmployee() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.softDeleteEmployee(999L);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should hard delete employee successfully")
    public void testHardDeleteEmployee() {
        // Arrange
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        // Act
        employeeService.hardDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    // ========== VALIDATION TESTS ==========

    @Test
    @DisplayName("Should validate badge ID format")
    public void testValidateBadgeIdFormat() {
        // Arrange
        testEmployeeDTO.setBadgeId("INVALID@ID");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Should validate hire date not in future")
    public void testValidateHireDateNotInFuture() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    // ========== CACHING TESTS ==========

    @Test
    @DisplayName("Should cache employee lookup by ID")
    public void testCacheEmployeeLookup() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        employeeService.getEmployeeById(1L);
        employeeService.getEmployeeById(1L);

        // Assert
        // With caching, repository should only be called once
        verify(employeeRepository, times(1)).findById(1L);
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Should handle null ID in get operation")
    public void testGetEmployeeWithNullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    @DisplayName("Should handle empty department filter")
    public void testGetEmployeesByEmptyDepartment() {
        // Arrange
        when(employeeRepository.findByDepartment("")).thenReturn(Arrays.asList());

        // Act
        List<EmployeeDTO> result = employeeService.getEmployeesByDepartment("");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle pagination with zero size")
    public void testGetAllEmployeesWithZeroPageSize() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getAllEmployees(pageable);
        });
    }

    @Test
    @DisplayName("Should count active employees")
    public void testCountActiveEmployees() {
        // Arrange
        when(employeeRepository.countByStatusAndDeletedFalse("ACTIVE")).thenReturn(5L);

        // Act
        long count = employeeService.countActiveEmployees();

        // Assert
        assertEquals(5L, count);
        verify(employeeRepository, times(1)).countByStatusAndDeletedFalse("ACTIVE");
    }