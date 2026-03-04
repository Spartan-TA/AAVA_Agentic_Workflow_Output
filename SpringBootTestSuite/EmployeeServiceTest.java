package com.wms.employee.service;

import com.wms.employee.dto.EmployeeDTO;
import com.wms.employee.model.Employee;
import com.wms.employee.repository.EmployeeRepository;
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

import javax.persistence.EntityNotFoundException;
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
 * Tests cover normal cases, boundary conditions, edge cases, validation, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDTO testEmployeeDTO;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test DTO
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Day Shift");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");

        // Arrange: Create test entity
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Day Shift");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
    }

    // ========== Tests for createEmployee() method ==========

    @Test
    @DisplayName("Test create employee with valid data")
    public void testCreateEmployee_ValidData_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        assertEquals("WORKER", result.getRole());
        assertEquals("Warehouse", result.getDepartment());
        assertEquals("Day Shift", result.getShiftGroup());
        assertEquals(LocalDate.of(2023, 1, 15), result.getHireDate());
        assertEquals("ACTIVE", result.getStatus());
        
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null DTO throws exception")
    public void testCreateEmployee_NullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.createEmployee(null);
        });
        
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null badgeId throws exception")
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
        
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with empty badgeId throws exception")
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
        
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with whitespace badgeId throws exception")
    public void testCreateEmployee_WhitespaceBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
        
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null name")
    public void testCreateEmployee_NullName_Success() {
        // Arrange
        testEmployeeDTO.setName(null);
        testEmployee.setName(null);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertNull(result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with empty name")
    public void testCreateEmployee_EmptyName_Success() {
        // Arrange
        testEmployeeDTO.setName("");
        testEmployee.setName("");
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with very long name (boundary)")
    public void testCreateEmployee_VeryLongName_Success() {
        // Arrange
        String longName = "A".repeat(255);
        testEmployeeDTO.setName(longName);
        testEmployee.setName(longName);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(longName, result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with special characters in name")
    public void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        String specialName = "JosÃ© MarÃ­a O'Brien-Smith";
        testEmployeeDTO.setName(specialName);
        testEmployee.setName(specialName);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(specialName, result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with future hire date")
    public void testCreateEmployee_FutureHireDate_Success() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        testEmployeeDTO.setHireDate(futureDate);
        testEmployee.setHireDate(futureDate);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(futureDate, result.getHireDate());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with past hire date")
    public void testCreateEmployee_PastHireDate_Success() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2000, 1, 1);
        testEmployeeDTO.setHireDate(pastDate);
        testEmployee.setHireDate(pastDate);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(pastDate, result.getHireDate());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== Tests for getEmployee() method ==========

    @Test
    @DisplayName("Test get employee by valid ID")
    public void testGetEmployee_ValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test get employee by non-existent ID throws exception")
    public void testGetEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployee(999L);
        });
        
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Test get employee by null ID throws exception")
    public void testGetEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(null);
        });
        
        verify(employeeRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Test get employee by negative ID throws exception")
    public void testGetEmployee_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(-1L);
        });
        
        verify(employeeRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Test get employee by zero ID throws exception")
    public void testGetEmployee_ZeroId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(0L);
        });
        
        verify(employeeRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Test get deleted employee returns employee")
    public void testGetEmployee_DeletedEmployee_ReturnsEmployee() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    // ========== Tests for listEmployees() method ==========

    @Test
    @DisplayName("Test list employees with pagination")
    public void testListEmployees_WithPagination_ReturnsPagedEmployees() {
        // Arrange
        Employee employee2 = createEmployee(2L, "EMP002", "Jane Smith");
        List<Employee> employees = Arrays.asList(testEmployee, employee2);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 2);
        
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(PageRequest.of(0, 10), null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
        assertEquals("EMP002", result.getContent().get(1).getBadgeId());
        
        verify(employeeRepository, times(1)).findAllByDeletedFalse(any(Pageable.class));
    }

    @Test
    @DisplayName("Test list employees with empty result")
    public void testListEmployees_EmptyResult_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(PageRequest.of(0, 10), null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        
        verify(employeeRepository, times(1)).findAllByDeletedFalse(any(Pageable.class));
    }

    @Test
    @DisplayName("Test list employees with null pageable throws exception")
    public void testListEmployees_NullPageable_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.listEmployees(null, null);
        });
        
        verify(employeeRepository, never()).findAllByDeletedFalse(any(Pageable.class));
    }

    @Test
    @DisplayName("Test list employees with page size 1")
    public void testListEmployees_PageSizeOne_ReturnsOneEmployee() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 1), 2);
        
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(PageRequest.of(0, 1), null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalPages());
        
        verify(employeeRepository, times(1)).findAllByDeletedFalse(any(Pageable.class));
    }

    @Test
    @DisplayName("Test list employees with large page size")
    public void testListEmployees_LargePageSize_ReturnsAllEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 100), 1);
        
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(PageRequest.of(0, 100), null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        verify(employeeRepository, times(1)).findAllByDeletedFalse(any(Pageable.class));
    }

    // ========== Tests for updateEmployee() method ==========

    @Test
    @DisplayName("Test update employee with valid data")
    public void testUpdateEmployee_ValidData_ReturnsUpdatedEmployee() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setRole("SUPERVISOR");
        updateDTO.setDepartment("Management");
        updateDTO.setStatus("ACTIVE");
        
        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(1L);
        updatedEmployee.setBadgeId("EMP001");
        updatedEmployee.setName("Updated Name");
        updatedEmployee.setRole("SUPERVISOR");
        updatedEmployee.setDepartment("Management");
        updatedEmployee.setStatus("ACTIVE");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
        assertEquals("Management", result.getDepartment());
        
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with non-existent ID throws exception")
    public void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Updated Name");
        
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, updateDTO);
        });
        
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with null ID throws exception")
    public void testUpdateEmployee_NullId_ThrowsException() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Updated Name");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(null, updateDTO);
        });
        
        verify(employeeRepository, never()).findById(anyLong());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with null DTO throws exception")
    public void testUpdateEmployee_NullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
        
        verify(employeeRepository, never()).findById(anyLong());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with partial data")
    public void testUpdateEmployee_PartialData_UpdatesOnlyProvidedFields() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Updated Name");
        // Other fields are null
        
        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(1L);
        updatedEmployee.setBadgeId("EMP001");
        updatedEmployee.setName("Updated Name");
        updatedEmployee.setRole("WORKER"); // Original value
        updatedEmployee.setDepartment("Warehouse"); // Original value
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("WORKER", result.getRole()); // Should retain original
        assertEquals("Warehouse", result.getDepartment()); // Should retain original
        
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== Tests for deleteEmployee() method ==========

    @Test
    @DisplayName("Test soft delete employee with valid ID")
    public void testDeleteEmployee_ValidId_SoftDeletesEmployee() {
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
    @DisplayName("Test delete employee with non-existent ID throws exception")
    public void testDeleteEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test delete employee with null ID throws exception")
    public void testDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
        
        verify(employeeRepository, never()).findById(anyLong());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test delete employee with negative ID throws exception")
    public void testDeleteEmployee_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(-1L);
        });
        
        verify(employeeRepository, never()).findById(anyLong());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test delete already deleted employee")
    public void testDeleteEmployee_AlreadyDeleted_Success() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== Helper Methods ==========

    private Employee createEmployee(Long id, String badgeId, String name) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setBadgeId(badgeId);
        employee.setName(name);
        employee.setRole("WORKER");
        employee.setDepartment("Warehouse");
        employee.setShiftGroup("Day Shift");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
        return employee;
    }
}