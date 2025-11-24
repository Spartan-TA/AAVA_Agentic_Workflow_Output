package com.warehouse.employee.management.service;

import com.warehouse.employee.management.model.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService.
 * Tests cover normal operations, boundary conditions, and edge cases.
 * Follows AAA (Arrange-Act-Assert) pattern for clarity.
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;
    private Employee anotherEmployee;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test data
        validEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();

        anotherEmployee = Employee.builder()
                .id(2L)
                .badgeId("EMP002")
                .name("Jane Smith")
                .role("SUPERVISOR")
                .department("Warehouse")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2022, 6, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ========== Tests for getAllEmployees(Pageable) ==========

    @Test
    public void testGetAllEmployees_ValidPageable_ReturnsPageOfEmployees() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(validEmployee, anotherEmployee);
        Page<Employee> expectedPage = new PageImpl<>(employees, pageable, employees.size());
        when(employeeRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(validEmployee.getBadgeId(), result.getContent().get(0).getBadgeId());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testGetAllEmployees_EmptyResult_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testGetAllEmployees_NullPageable_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> employeeService.getAllEmployees(null));
    }

    // ========== Tests for getEmployeeById(Long) ==========

    @Test
    public void testGetEmployeeById_ValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act
        Optional<Employee> result = employeeService.getEmployeeById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("EMP001", result.get().getBadgeId());
        assertEquals("John Doe", result.get().getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployeeById_NonExistentId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetEmployeeById_NullId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeById(null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetEmployeeById_NegativeId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeById(-1L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(-1L);
    }

    @Test
    public void testGetEmployeeById_ZeroId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(0L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeById(0L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(0L);
    }

    // ========== Tests for getEmployeeByBadgeId(String) ==========

    @Test
    public void testGetEmployeeByBadgeId_ValidBadgeId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));

        // Act
        Optional<Employee> result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
    }

    @Test
    public void testGetEmployeeByBadgeId_NonExistentBadgeId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeByBadgeId("INVALID");

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findByBadgeId("INVALID");
    }

    @Test
    public void testGetEmployeeByBadgeId_NullBadgeId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findByBadgeId(null)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeByBadgeId(null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetEmployeeByBadgeId_EmptyBadgeId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findByBadgeId("")).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeByBadgeId("");

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findByBadgeId("");
    }

    @Test
    public void testGetEmployeeByBadgeId_WhitespaceBadgeId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findByBadgeId("   ")).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.getEmployeeByBadgeId("   ");

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findByBadgeId("   ");
    }

    // ========== Tests for createEmployee(Employee) ==========

    @Test
    public void testCreateEmployee_ValidEmployee_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.createEmployee(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> employeeService.createEmployee(validEmployee));
        assertEquals("Badge ID already exists", exception.getMessage());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullEmployee_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        Employee employeeWithNullBadge = Employee.builder()
                .badgeId(null)
                .name("Test User")
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, 
            () -> employeeService.createEmployee(employeeWithNullBadge));
    }

    @Test
    public void testCreateEmployee_EmptyBadgeId_Success() {
        // Arrange
        Employee employeeWithEmptyBadge = Employee.builder()
                .badgeId("")
                .name("Test User")
                .role("WORKER")
                .department("Test")
                .shiftGroup("Morning")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(false)
                .build();
        when(employeeRepository.findByBadgeId("")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeWithEmptyBadge);

        // Act
        Employee result = employeeService.createEmployee(employeeWithEmptyBadge);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getBadgeId());
    }

    // ========== Tests for updateEmployee(Long, Employee) ==========

    @Test
    public void testUpdateEmployee_ValidUpdate_Success() {
        // Arrange
        Employee updatedData = Employee.builder()
                .name("John Updated")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedData);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
        assertEquals("Logistics", result.getDepartment());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        Employee updatedData = Employee.builder().name("Test").build();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> employeeService.updateEmployee(999L, updatedData));
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_NullId_ThrowsException() {
        // Arrange
        Employee updatedData = Employee.builder().name("Test").build();

        // Act & Assert
        assertThrows(NullPointerException.class, 
            () -> employeeService.updateEmployee(null, updatedData));
    }

    @Test
    public void testUpdateEmployee_NullUpdatedData_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(NullPointerException.class, 
            () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    public void testUpdateEmployee_PartialUpdate_Success() {
        // Arrange
        Employee partialUpdate = Employee.builder()
                .name("Partial Update")
                .role(validEmployee.getRole())
                .department(validEmployee.getDepartment())
                .shiftGroup(validEmployee.getShiftGroup())
                .hireDate(validEmployee.getHireDate())
                .status(validEmployee.getStatus())
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Employee result = employeeService.updateEmployee(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("Partial Update", result.getName());
        assertEquals(validEmployee.getRole(), result.getRole());
    }

    // ========== Tests for softDeleteEmployee(Long) ==========

    @Test
    public void testSoftDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(validEmployee.getDeleted());
        assertEquals("TERMINATED", validEmployee.getStatus());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    @Test
    public void testSoftDeleteEmployee_NonExistentId_NoAction() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        employeeService.softDeleteEmployee(999L);

        // Assert
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testSoftDeleteEmployee_NullId_NoAction() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        employeeService.softDeleteEmployee(null);

        // Assert
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testSoftDeleteEmployee_AlreadyDeleted_UpdatesAgain() {
        // Arrange
        validEmployee.setDeleted(true);
        validEmployee.setStatus("TERMINATED");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(validEmployee.getDeleted());
        assertEquals("TERMINATED", validEmployee.getStatus());
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    // ========== Tests for getEmployeesByDepartment(String) ==========

    @Test
    public void testGetEmployeesByDepartment_ValidDepartment_ReturnsEmployees() {
        // Arrange
        List<Employee> warehouseEmployees = Arrays.asList(validEmployee, anotherEmployee);
        when(employeeRepository.findByDepartment("Warehouse")).thenReturn(warehouseEmployees);

        // Act
        List<Employee> result = employeeService.getEmployeesByDepartment("Warehouse");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Warehouse", result.get(0).getDepartment());
        verify(employeeRepository, times(1)).findByDepartment("Warehouse");
    }

    @Test
    public void testGetEmployeesByDepartment_NonExistentDepartment_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findByDepartment("NonExistent")).thenReturn(Arrays.asList());

        // Act
        List<Employee> result = employeeService.getEmployeesByDepartment("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findByDepartment("NonExistent");
    }

    @Test
    public void testGetEmployeesByDepartment_NullDepartment_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findByDepartment(null)).thenReturn(Arrays.asList());

        // Act
        List<Employee> result = employeeService.getEmployeesByDepartment(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEmployeesByDepartment_EmptyDepartment_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findByDepartment("")).thenReturn(Arrays.asList());

        // Act
        List<Employee> result = employeeService.getEmployeesByDepartment("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findByDepartment("");
    }

    @Test
    public void testGetEmployeesByDepartment_CaseSensitive_ReturnsCorrectResults() {
        // Arrange
        when(employeeRepository.findByDepartment("warehouse")).thenReturn(Arrays.asList());
        when(employeeRepository.findByDepartment("Warehouse")).thenReturn(Arrays.asList(validEmployee));

        // Act
        List<Employee> lowerCaseResult = employeeService.getEmployeesByDepartment("warehouse");
        List<Employee> properCaseResult = employeeService.getEmployeesByDepartment("Warehouse");

        // Assert
        assertTrue(lowerCaseResult.isEmpty());
        assertEquals(1, properCaseResult.size());
    }
}