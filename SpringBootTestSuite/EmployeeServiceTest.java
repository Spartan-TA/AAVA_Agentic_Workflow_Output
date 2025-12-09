package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Tests cover all CRUD operations, validation logic, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private Employee testEmployee2;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test employee objects
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);

        testEmployee2 = new Employee();
        testEmployee2.setId(2L);
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setName("Jane Smith");
        testEmployee2.setRole("SUPERVISOR");
        testEmployee2.setDepartment("Warehouse");
        testEmployee2.setShiftGroup("B");
        testEmployee2.setHireDate(LocalDate.of(2022, 6, 1));
        testEmployee2.setStatus("ACTIVE");
        testEmployee2.setDeleted(false);
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    public void testGetAllEmployees_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee, testEmployee2);
        when(employeeRepository.findAll()).thenReturn(employees);

        // Act
        List<Employee> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("EMP001", result.get(0).getBadgeId());
        assertEquals("EMP002", result.get(1).getBadgeId());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllEmployees_EmptyList() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Employee> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findAll();
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    public void testGetEmployeeById_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Optional<Employee> result = employeeService.getEmployeeById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("EMP001", result.get().getBadgeId());
        assertEquals("John Doe", result.get().getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployeeById_NotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetEmployeeById_NullId() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeById(null);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(null);
    }

    @Test
    public void testGetEmployeeById_NegativeId() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeById(-1L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(-1L);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    public void testCreateEmployee_Success() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, times(1)).findByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullBadgeId() {
        // Arrange
        Employee invalidEmployee = new Employee();
        invalidEmployee.setBadgeId(null);
        invalidEmployee.setName("Test User");
        invalidEmployee.setRole("WORKER");
        invalidEmployee.setHireDate(LocalDate.now());
        invalidEmployee.setStatus("ACTIVE");

        when(employeeRepository.findByBadgeIdAndDeletedFalse(null)).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(invalidEmployee);

        // Act
        Employee result = employeeService.createEmployee(invalidEmployee);

        // Assert
        assertNotNull(result);
        assertNull(result.getBadgeId());
    }

    @Test
    public void testCreateEmployee_EmptyBadgeId() {
        // Arrange
        Employee invalidEmployee = new Employee();
        invalidEmployee.setBadgeId("");
        invalidEmployee.setName("Test User");
        invalidEmployee.setRole("WORKER");
        invalidEmployee.setHireDate(LocalDate.now());
        invalidEmployee.setStatus("ACTIVE");

        when(employeeRepository.findByBadgeIdAndDeletedFalse("")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(invalidEmployee);

        // Act
        Employee result = employeeService.createEmployee(invalidEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getBadgeId());
    }

    @Test
    public void testCreateEmployee_NullName() {
        // Arrange
        Employee invalidEmployee = new Employee();
        invalidEmployee.setBadgeId("EMP999");
        invalidEmployee.setName(null);
        invalidEmployee.setRole("WORKER");
        invalidEmployee.setHireDate(LocalDate.now());
        invalidEmployee.setStatus("ACTIVE");

        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP999")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(invalidEmployee);

        // Act
        Employee result = employeeService.createEmployee(invalidEmployee);

        // Assert
        assertNotNull(result);
        assertNull(result.getName());
    }

    @Test
    public void testCreateEmployee_FutureHireDate() {
        // Arrange
        Employee futureEmployee = new Employee();
        futureEmployee.setBadgeId("EMP888");
        futureEmployee.setName("Future Employee");
        futureEmployee.setRole("WORKER");
        futureEmployee.setHireDate(LocalDate.now().plusDays(30));
        futureEmployee.setStatus("PENDING");

        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP888")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(futureEmployee);

        // Act
        Employee result = employeeService.createEmployee(futureEmployee);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHireDate().isAfter(LocalDate.now()));
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    public void testUpdateEmployee_Success() {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("John Updated");
        updatedEmployee.setRole("SUPERVISOR");
        updatedEmployee.setDepartment("Logistics");
        updatedEmployee.setShiftGroup("B");
        updatedEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        updatedEmployee.setStatus("ACTIVE");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
        assertEquals("Logistics", result.getDepartment());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    public void testUpdateEmployee_NotFound() {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("Updated Name");
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.updateEmployee(999L, updatedEmployee);
        });
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_NullId() {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("Updated Name");
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.updateEmployee(null, updatedEmployee);
        });
        assertTrue(exception.getMessage().contains("Employee not found"));
    }

    @Test
    public void testUpdateEmployee_PartialUpdate() {
        // Arrange
        Employee partialUpdate = new Employee();
        partialUpdate.setName("Partial Update");
        partialUpdate.setRole(testEmployee.getRole());
        partialUpdate.setDepartment(testEmployee.getDepartment());
        partialUpdate.setShiftGroup(testEmployee.getShiftGroup());
        partialUpdate.setHireDate(testEmployee.getHireDate());
        partialUpdate.setStatus(testEmployee.getStatus());

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.updateEmployee(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("Partial Update", result.getName());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    public void testDeleteEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.getDeleted());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    public void testDeleteEmployee_NotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testDeleteEmployee_NullId() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.deleteEmployee(null);
        });
        assertTrue(exception.getMessage().contains("Employee not found"));
    }

    @Test
    public void testDeleteEmployee_AlreadyDeleted() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.getDeleted());
        verify(employeeRepository, times(1)).save(testEmployee);
    }
}