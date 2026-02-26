package com.warehouse.employee.service;

import com.warehouse.employee.entity.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityNotFoundException;

/**
 * Comprehensive JUnit test class for EmployeeService
 * Tests all service layer business logic with mocked dependencies
 */
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.of(2024, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
    }

    @Test
    void testGetEmployeeById_ExistingEmployee_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        
        // Act
        Employee result = employeeService.getEmployeeById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEmployeeById_NonExistingEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    void testCreateEmployee_ValidData_ReturnsCreatedEmployee() {
        // Arrange
        Employee newEmployee = new Employee();
        newEmployee.setName("Jane Smith");
        newEmployee.setBadgeId("EMP002");
        newEmployee.setRole("WORKER");
        
        when(employeeRepository.findByBadgeId("EMP002")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(newEmployee);
        
        // Act
        Employee result = employeeService.createEmployee(newEmployee);
        
        // Assert
        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        Employee newEmployee = new Employee();
        newEmployee.setBadgeId("EMP001");
        
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(newEmployee);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullEmployee_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
    }

    @Test
    void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        Employee newEmployee = new Employee();
        newEmployee.setName("");
        newEmployee.setBadgeId("EMP002");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(newEmployee);
        });
    }

    @Test
    void testUpdateEmployee_ValidData_ReturnsUpdatedEmployee() {
        // Arrange
        Employee updatedData = new Employee();
        updatedData.setName("John Updated");
        updatedData.setDepartment("Logistics");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);
        
        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NonExistingEmployee_ThrowsException() {
        // Arrange
        Employee updatedData = new Employee();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, updatedData);
        });
    }

    @Test
    void testSoftDeleteEmployee_ExistingEmployee_MarksAsDeleted() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        // Act
        employeeService.softDeleteEmployee(1L);
        
        // Assert
        assertTrue(testEmployee.isDeleted());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    void testSoftDeleteEmployee_NonExistingEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.softDeleteEmployee(999L);
        });
    }

    @Test
    void testGetAllEmployees_WithPagination_ReturnsPagedResults() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        
        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    void testGetEmployeeByBadgeId_ExistingBadge_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));
        
        // Act
        Employee result = employeeService.getEmployeeByBadgeId("EMP001");
        
        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetEmployeeByBadgeId_NonExistingBadge_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("NONEXISTENT")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("NONEXISTENT");
        });
    }

    @Test
    void testGetEmployeesByDepartment_ReturnsMatchingEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByDepartment("Warehouse")).thenReturn(employees);
        
        // Act
        List<Employee> result = employeeService.getEmployeesByDepartment("Warehouse");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Warehouse", result.get(0).getDepartment());
    }

    @Test
    void testGetEmployeesByRole_ReturnsMatchingEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByRole("WORKER")).thenReturn(employees);
        
        // Act
        List<Employee> result = employeeService.getEmployeesByRole("WORKER");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WORKER", result.get(0).getRole());
    }

    @Test
    void testValidateEmployee_ValidEmployee_NoException() {
        // Arrange
        Employee validEmployee = new Employee();
        validEmployee.setName("Valid Name");
        validEmployee.setBadgeId("VALID001");
        validEmployee.setRole("WORKER");
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            employeeService.validateEmployee(validEmployee);
        });
    }

    @Test
    void testValidateEmployee_NullName_ThrowsException() {
        // Arrange
        Employee invalidEmployee = new Employee();
        invalidEmployee.setName(null);
        invalidEmployee.setBadgeId("VALID001");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.validateEmployee(invalidEmployee);
        });
    }

    @Test
    void testValidateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        Employee invalidEmployee = new Employee();
        invalidEmployee.setName("Valid Name");
        invalidEmployee.setBadgeId("");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.validateEmployee(invalidEmployee);
        });
    }
}