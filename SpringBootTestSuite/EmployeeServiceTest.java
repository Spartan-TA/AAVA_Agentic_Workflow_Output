package com.company.warehouse.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService
 * Covers normal cases, boundary conditions, and edge cases
 */
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDto validEmployeeDto;
    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup valid test data
        validEmployeeDto = new EmployeeDto();
        validEmployeeDto.setName("John Doe");
        validEmployeeDto.setBadgeId("EMP001");
        validEmployeeDto.setRole(Role.WORKER);
        validEmployeeDto.setDepartment("Shipping");
        validEmployeeDto.setShiftGroup("Morning");
        validEmployeeDto.setHireDate(LocalDate.of(2024, 1, 15));
        validEmployeeDto.setStatus(Status.ACTIVE);

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setRole(Role.WORKER);
        validEmployee.setDepartment("Shipping");
        validEmployee.setShiftGroup("Morning");
        validEmployee.setHireDate(LocalDate.of(2024, 1, 15));
        validEmployee.setStatus(Status.ACTIVE);
        validEmployee.setCreatedAt(LocalDateTime.now());
        validEmployee.setUpdatedAt(LocalDateTime.now());
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test create employee with valid input")
    public void testCreateEmployeeWithValidInput() {
        // Arrange
        when(employeeRepository.findByBadgeId(validEmployeeDto.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals(Role.WORKER, result.getRole());
        verify(employeeRepository, times(1)).findByBadgeId(validEmployeeDto.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with duplicate badge ID")
    public void testCreateEmployeeWithDuplicateBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId(validEmployeeDto.getBadgeId()))
            .thenReturn(Optional.of(validEmployee));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.create(validEmployeeDto)
        );
        assertEquals("Badge ID must be unique", exception.getMessage());
        verify(employeeRepository, times(1)).findByBadgeId(validEmployeeDto.getBadgeId());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null name")
    public void testCreateEmployeeWithNullName() {
        // Arrange
        validEmployeeDto.setName(null);
        when(employeeRepository.findByBadgeId(validEmployeeDto.getBadgeId())).thenReturn(Optional.empty());

        // Act & Assert - Should handle null name
        assertDoesNotThrow(() -> {
            when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
            employeeService.create(validEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test create employee with empty badge ID")
    public void testCreateEmployeeWithEmptyBadgeId() {
        // Arrange
        validEmployeeDto.setBadgeId("");
        when(employeeRepository.findByBadgeId("")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with minimum valid name length")
    public void testCreateEmployeeWithMinimumNameLength() {
        // Arrange
        validEmployeeDto.setName("Jo"); // Minimum 2 characters
        when(employeeRepository.findByBadgeId(validEmployeeDto.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with maximum valid name length")
    public void testCreateEmployeeWithMaximumNameLength() {
        // Arrange
        String longName = "A".repeat(100); // Maximum 100 characters
        validEmployeeDto.setName(longName);
        when(employeeRepository.findByBadgeId(validEmployeeDto.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with past hire date")
    public void testCreateEmployeeWithPastHireDate() {
        // Arrange
        validEmployeeDto.setHireDate(LocalDate.of(2020, 1, 1));
        when(employeeRepository.findByBadgeId(validEmployeeDto.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with today's hire date")
    public void testCreateEmployeeWithTodayHireDate() {
        // Arrange
        validEmployeeDto.setHireDate(LocalDate.now());
        when(employeeRepository.findByBadgeId(validEmployeeDto.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== FIND EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test find employee by valid ID")
    public void testFindByIdWithValidId() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act
        Optional<Employee> result = employeeService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        assertEquals("EMP001", result.get().getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test find employee by non-existent ID")
    public void testFindByIdWithNonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Test find employee by null ID")
    public void testFindByIdWithNullId() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.findById(null);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("Test find employee by negative ID")
    public void testFindByIdWithNegativeId() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> result = employeeService.findById(-1L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(-1L);
    }

    // ========== FIND ALL EMPLOYEES TESTS ==========

    @Test
    @DisplayName("Test find all employees with pagination")
    public void testFindAllWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test find all employees with empty result")
    public void testFindAllWithEmptyResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test update employee with valid data")
    public void testUpdateEmployeeWithValidData() {
        // Arrange
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setName("Jane Doe");
        updateDto.setBadgeId("EMP001");
        updateDto.setRole(Role.SUPERVISOR);
        updateDto.setDepartment("Receiving");
        updateDto.setShiftGroup("Evening");
        updateDto.setHireDate(LocalDate.of(2024, 1, 15));
        updateDto.setStatus(Status.ACTIVE);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.update(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with non-existent ID")
    public void testUpdateEmployeeWithNonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.update(999L, validEmployeeDto)
        );
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with duplicate badge ID")
    public void testUpdateEmployeeWithDuplicateBadgeId() {
        // Arrange
        Employee anotherEmployee = new Employee();
        anotherEmployee.setId(2L);
        anotherEmployee.setBadgeId("EMP002");

        validEmployeeDto.setBadgeId("EMP002");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.findByBadgeId("EMP002")).thenReturn(Optional.of(anotherEmployee));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.update(1L, validEmployeeDto)
        );
        assertEquals("Badge ID must be unique", exception.getMessage());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test soft delete employee with valid ID")
    public void testDeleteEmployeeWithValidId() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.delete(1L);

        // Assert
        assertEquals(Status.INACTIVE, validEmployee.getStatus());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    @Test
    @DisplayName("Test delete employee with non-existent ID")
    public void testDeleteEmployeeWithNonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.delete(999L)
        );
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== FIND BY STATUS TESTS ==========

    @Test
    @DisplayName("Test find employees by active status")
    public void testFindByStatusActive() {
        // Arrange
        List<Employee> activeEmployees = Arrays.asList(validEmployee);
        when(employeeRepository.findByStatus(Status.ACTIVE)).thenReturn(activeEmployees);

        // Act
        List<Employee> result = employeeService.findByStatus(Status.ACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Status.ACTIVE, result.get(0).getStatus());
        verify(employeeRepository, times(1)).findByStatus(Status.ACTIVE);
    }

    @Test
    @DisplayName("Test find employees by inactive status")
    public void testFindByStatusInactive() {
        // Arrange
        when(employeeRepository.findByStatus(Status.INACTIVE)).thenReturn(Arrays.asList());

        // Act
        List<Employee> result = employeeService.findByStatus(Status.INACTIVE);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findByStatus(Status.INACTIVE);
    }

    // ========== FIND BY DEPARTMENT TESTS ==========

    @Test
    @DisplayName("Test find employees by department")
    public void testFindByDepartment() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findByDepartment("Shipping", pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.findByDepartment("Shipping", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Shipping", result.getContent().get(0).getDepartment());
        verify(employeeRepository, times(1)).findByDepartment("Shipping", pageable);
    }

    @Test
    @DisplayName("Test find employees by non-existent department")
    public void testFindByNonExistentDepartment() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findByDepartment("NonExistent", pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.findByDepartment("NonExistent", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(employeeRepository, times(1)).findByDepartment("NonExistent", pageable);
    }
}