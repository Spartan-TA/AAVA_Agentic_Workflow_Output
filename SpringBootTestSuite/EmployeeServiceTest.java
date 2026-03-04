package com.warehouse.ems.service.test;

import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.exception.ValidationException;
import com.warehouse.ems.model.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService.
 * Tests normal cases, edge cases, and exception scenarios for all public methods.
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private List<Employee> employeeList;

    @BeforeEach
    public void setUp() {
        // Arrange: Initialize test data
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");
        employee.setName("John Doe");
        employee.setActive(true);

        employeeList = new ArrayList<>();
        employeeList.add(employee);
    }

    /**
     * Test createEmployee with valid input.
     */
    @Test
    public void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        Employee result = employeeService.createEmployee(employee);

        // Assert
        assertNotNull(result);
        assertEquals("BADGE123", result.getBadgeId());
        verify(employeeRepository).existsByBadgeId("BADGE123");
        verify(employeeRepository).save(any(Employee.class));
    }

    /**
     * Test createEmployee with duplicate badge ID.
     */
    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsValidationException() {
        // Arrange
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(employee));
        verify(employeeRepository).existsByBadgeId("BADGE123");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test createEmployee with null badge ID.
     */
    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsValidationException() {
        // Arrange
        employee.setBadgeId(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(employee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test createEmployee with empty name.
     */
    @Test
    public void testCreateEmployee_EmptyName_ThrowsValidationException() {
        // Arrange
        employee.setName("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(employee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test getEmployeeById with existing ID.
     */
    @Test
    public void testGetEmployeeById_ExistingId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        Employee result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(employeeRepository).findById(1L);
    }

    /**
     * Test getEmployeeById with non-existing ID.
     */
    @Test
    public void testGetEmployeeById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(2L));
        verify(employeeRepository).findById(2L);
    }

    /**
     * Test getEmployeeById with null ID.
     */
    @Test
    public void testGetEmployeeById_NullId_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.getEmployeeById(null));
        verify(employeeRepository, never()).findById(any());
    }

    /**
     * Test getAllEmployees with pagination.
     */
    @Test
    public void testGetAllEmployees_WithPagination_ReturnsPagedList() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(employeeList, pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findAll(pageable);
    }

    /**
     * Test getAllEmployees with empty database.
     */
    @Test
    public void testGetAllEmployees_EmptyDatabase_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(employeeRepository).findAll(pageable);
    }

    /**
     * Test updateEmployee with valid input.
     */
    @Test
    public void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        Employee updated = new Employee();
        updated.setId(1L);
        updated.setBadgeId("BADGE123");
        updated.setName("Jane Doe");
        updated.setActive(true);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);

        // Act
        Employee result = employeeService.updateEmployee(1L, updated);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).existsByBadgeId("BADGE123");
        verify(employeeRepository).save(any(Employee.class));
    }

    /**
     * Test updateEmployee with non-existing ID.
     */
    @Test
    public void testUpdateEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        Employee updated = new Employee();
        updated.setId(2L);
        updated.setBadgeId("BADGE999");
        updated.setName("Jane Doe");

        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(2L, updated));
        verify(employeeRepository).findById(2L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test updateEmployee with duplicate badge ID.
     */
    @Test
    public void testUpdateEmployee_DuplicateBadgeId_ThrowsValidationException() {
        // Arrange
        Employee updated = new Employee();
        updated.setId(1L);
        updated.setBadgeId("BADGE999");
        updated.setName("Jane Doe");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByBadgeId("BADGE999")).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.updateEmployee(1L, updated));
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).existsByBadgeId("BADGE999");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test deleteEmployee with existing ID.
     */
    @Test
    public void testDeleteEmployee_ExistingId_SoftDeletesEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        assertFalse(employee.isActive());
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(employee);
    }

    /**
     * Test deleteEmployee with non-existing ID.
     */
    @Test
    public void testDeleteEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(2L));
        verify(employeeRepository).findById(2L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test findByBadgeId with existing badge ID.
     */
    @Test
    public void testFindByBadgeId_ExistingBadgeId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));

        // Act
        Employee result = employeeService.findByBadgeId("BADGE123");

        // Assert
        assertNotNull(result);
        assertEquals("BADGE123", result.getBadgeId());
        verify(employeeRepository).findByBadgeId("BADGE123");
    }

    /**
     * Test findByBadgeId with non-existing badge ID.
     */
    @Test
    public void testFindByBadgeId_NonExistingBadgeId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.findByBadgeId("BADGE999"));
        verify(employeeRepository).findByBadgeId("BADGE999");
    }
}
