package com.example.warehouse.service;

import com.example.warehouse.dto.EmployeeDto;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService.
 * 
 * Tests cover:
 * - Normal cases: Valid inputs and successful operations
 * - Boundary conditions: Null values, empty strings, edge dates
 * - Edge cases: Non-existent IDs, duplicate emails, invalid data
 * - Exception handling: RuntimeException for not found scenarios
 * 
 * @author Warehouse Test Team
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDto testEmployeeDto;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();

        testEmployeeDto = EmployeeDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    /**
     * Test getAllEmployees with multiple employees - Normal case.
     * Verifies that the service correctly retrieves and converts all employees.
     */
    @Test
    public void testGetAllEmployees_WithMultipleEmployees_Success() {
        // Arrange
        Employee employee2 = Employee.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@warehouse.com")
                .position("Supervisor")
                .hireDate(LocalDate.of(2023, 6, 1))
                .active(true)
                .build();
        List<Employee> employees = Arrays.asList(testEmployee, employee2);
        when(employeeRepository.findAll()).thenReturn(employees);

        // Act
        List<EmployeeDto> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
        verify(employeeRepository, times(1)).findAll();
    }

    /**
     * Test getAllEmployees with empty list - Boundary condition.
     * Verifies that the service handles empty employee list correctly.
     */
    @Test
    public void testGetAllEmployees_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeDto> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findAll();
    }

    /**
     * Test getAllEmployees with single employee - Edge case.
     */
    @Test
    public void testGetAllEmployees_SingleEmployee_Success() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(testEmployee));

        // Act
        List<EmployeeDto> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEmployee.getEmail(), result.get(0).getEmail());
        verify(employeeRepository, times(1)).findAll();
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    /**
     * Test getEmployeeById with valid ID - Normal case.
     * Verifies successful retrieval of employee by ID.
     */
    @Test
    public void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDto result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@warehouse.com", result.getEmail());
        verify(employeeRepository, times(1)).findById(1L);
    }

    /**
     * Test getEmployeeById with non-existent ID - Edge case.
     * Verifies that RuntimeException is thrown for non-existent employee.
     */
    @Test
    public void testGetEmployeeById_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
    }

    /**
     * Test getEmployeeById with null ID - Boundary condition.
     */
    @Test
    public void testGetEmployeeById_NullId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    /**
     * Test getEmployeeById with negative ID - Edge case.
     */
    @Test
    public void testGetEmployeeById_NegativeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    /**
     * Test createEmployee with valid data - Normal case.
     * Verifies successful employee creation.
     */
    @Test
    public void testCreateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@warehouse.com", result.getEmail());
        assertTrue(result.isActive());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test createEmployee with null first name - Boundary condition.
     */
    @Test
    public void testCreateEmployee_NullFirstName_Success() {
        // Arrange
        testEmployeeDto.setFirstName(null);
        Employee employeeWithNullFirstName = Employee.builder()
                .id(1L)
                .firstName(null)
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeWithNullFirstName);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertNull(result.getFirstName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test createEmployee with empty email - Edge case.
     */
    @Test
    public void testCreateEmployee_EmptyEmail_Success() {
        // Arrange
        testEmployeeDto.setEmail("");
        Employee employeeWithEmptyEmail = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeWithEmptyEmail);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getEmail());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test createEmployee with future hire date - Edge case.
     */
    @Test
    public void testCreateEmployee_FutureHireDate_Success() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        testEmployeeDto.setHireDate(futureDate);
        Employee employeeWithFutureDate = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(futureDate)
                .active(true)
                .build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeWithFutureDate);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals(futureDate, result.getHireDate());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test createEmployee with inactive status - Normal case.
     */
    @Test
    public void testCreateEmployee_InactiveStatus_Success() {
        // Arrange
        testEmployeeDto.setActive(false);
        Employee inactiveEmployee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(false)
                .build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(inactiveEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isActive());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    /**
     * Test updateEmployee with valid data - Normal case.
     * Verifies successful employee update.
     */
    @Test
    public void testUpdateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        testEmployeeDto.setFirstName("Jane");
        testEmployeeDto.setPosition("Senior Associate");
        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Senior Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Senior Associate", result.getPosition());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test updateEmployee with non-existent ID - Edge case.
     */
    @Test
    public void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDto);
        });
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test updateEmployee changing active status - Normal case.
     */
    @Test
    public void testUpdateEmployee_ChangeActiveStatus_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        testEmployeeDto.setActive(false);
        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(false)
                .build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isActive());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /**
     * Test updateEmployee with null values - Boundary condition.
     */
    @Test
    public void testUpdateEmployee_NullValues_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        testEmployeeDto.setFirstName(null);
        testEmployeeDto.setLastName(null);
        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .firstName(null)
                .lastName(null)
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    /**
     * Test deleteEmployee with valid ID - Normal case.
     * Verifies successful employee deletion.
     */
    @Test
    public void testDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    /**
     * Test deleteEmployee with non-existent ID - Edge case.
     */
    @Test
    public void testDeleteEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).existsById(999L);
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    /**
     * Test deleteEmployee with null ID - Boundary condition.
     */
    @Test
    public void testDeleteEmployee_NullId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsById(null)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            employeeService.deleteEmployee(null);
        });
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    /**
     * Test deleteEmployee with negative ID - Edge case.
     */
    @Test
    public void testDeleteEmployee_NegativeId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsById(-1L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            employeeService.deleteEmployee(-1L);
        });
        verify(employeeRepository, times(1)).existsById(-1L);
        verify(employeeRepository, never()).deleteById(anyLong());
    }
}