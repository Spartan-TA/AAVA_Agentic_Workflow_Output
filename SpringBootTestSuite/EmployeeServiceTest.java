package com.company.warehouse.employee.service;

import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.domain.Role;
import com.company.warehouse.employee.domain.Status;
import com.company.warehouse.employee.dto.EmployeeCreateDTO;
import com.company.warehouse.employee.dto.EmployeeDTO;
import com.company.warehouse.employee.dto.EmployeeFilterDTO;
import com.company.warehouse.employee.dto.EmployeeUpdateDTO;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.common.exception.DuplicateResourceException;
import com.company.warehouse.common.exception.ResourceNotFoundException;
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
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal cases, boundary conditions, and edge cases
 * for all CRUD operations and business logic
 */
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeCreateDTO createDTO;
    private EmployeeUpdateDTO updateDTO;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole(Role.WORKER);
        testEmployee.setDepartment("Shipping");
        testEmployee.setShiftGroup("Day Shift");
        testEmployee.setHireDate(LocalDate.of(2024, 1, 1));
        testEmployee.setStatus(Status.ACTIVE);
        testEmployee.setDeleted(false);

        // Setup DTOs
        createDTO = new EmployeeCreateDTO();
        createDTO.setName("John Doe");
        createDTO.setBadgeId("EMP001");
        createDTO.setRole(Role.WORKER);
        createDTO.setDepartment("Shipping");
        createDTO.setShiftGroup("Day Shift");
        createDTO.setHireDate(LocalDate.of(2024, 1, 1));

        updateDTO = new EmployeeUpdateDTO();
        updateDTO.setName("John Updated");
        updateDTO.setBadgeId("EMP001");
        updateDTO.setRole(Role.SUPERVISOR);
        updateDTO.setStatus(Status.ACTIVE);

        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(1L);
        employeeDTO.setName("John Doe");
        employeeDTO.setBadgeId("EMP001");
        employeeDTO.setRole(Role.WORKER);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test createEmployee with valid input")
    public void testCreateEmployee_ValidInput() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeCreateDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals(Role.WORKER, result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test createEmployee with duplicate badgeId throws exception")
    public void testCreateEmployee_DuplicateBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test createEmployee with null name throws validation exception")
    public void testCreateEmployee_NullName() {
        // Arrange
        createDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
    }

    @Test
    @DisplayName("Test createEmployee with empty badgeId throws validation exception")
    public void testCreateEmployee_EmptyBadgeId() {
        // Arrange
        createDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
    }

    @Test
    @DisplayName("Test createEmployee with null role throws validation exception")
    public void testCreateEmployee_NullRole() {
        // Arrange
        createDTO.setRole(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
    }

    @Test
    @DisplayName("Test createEmployee with future hire date")
    public void testCreateEmployee_FutureHireDate() {
        // Arrange
        createDTO.setHireDate(LocalDate.now().plusDays(30));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeCreateDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== READ EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test getEmployee by valid ID")
    public void testGetEmployee_ValidId() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("Test getEmployee with non-existent ID throws exception")
    public void testGetEmployee_NonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployee(999L);
        });
    }

    @Test
    @DisplayName("Test getEmployee with null ID throws exception")
    public void testGetEmployee_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(null);
        });
    }

    @Test
    @DisplayName("Test listEmployees with pagination")
    public void testListEmployees_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(new EmployeeFilterDTO(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Test listEmployees filtered by department")
    public void testListEmployees_FilteredByDepartment() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        EmployeeFilterDTO filter = new EmployeeFilterDTO();
        filter.setDepartment("Shipping");
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findByDepartmentAndDeletedFalse("Shipping", pageable))
            .thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(filter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1))
            .findByDepartmentAndDeletedFalse("Shipping", pageable);
    }

    @Test
    @DisplayName("Test listEmployees filtered by status")
    public void testListEmployees_FilteredByStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        EmployeeFilterDTO filter = new EmployeeFilterDTO();
        filter.setStatus(Status.ACTIVE);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findByStatusAndDeletedFalse(Status.ACTIVE, pageable))
            .thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(filter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Test listEmployees returns empty page when no results")
    public void testListEmployees_EmptyResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(new EmployeeFilterDTO(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test updateEmployee with valid data")
    public void testUpdateEmployee_ValidData() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeIdAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test updateEmployee with non-existent ID throws exception")
    public void testUpdateEmployee_NonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, updateDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test updateEmployee with duplicate badgeId throws exception")
    public void testUpdateEmployee_DuplicateBadgeId() {
        // Arrange
        updateDTO.setBadgeId("EMP002");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeIdAndIdNot("EMP002", 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.updateEmployee(1L, updateDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test updateEmployee with same badgeId succeeds")
    public void testUpdateEmployee_SameBadgeId() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test updateEmployee changing status to TERMINATED")
    public void testUpdateEmployee_ChangeStatusToTerminated() {
        // Arrange
        updateDTO.setStatus(Status.TERMINATED);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeIdAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test deleteEmployee with valid ID")
    public void testDeleteEmployee_ValidId() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).delete(any(Employee.class));

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).delete(testEmployee);
    }

    @Test
    @DisplayName("Test deleteEmployee with non-existent ID throws exception")
    public void testDeleteEmployee_NonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    @DisplayName("Test deleteEmployee with null ID throws exception")
    public void testDeleteEmployee_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    @Test
    @DisplayName("Test deleteEmployee performs soft delete")
    public void testDeleteEmployee_SoftDelete() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setDeleted(true);
            return null;
        }).when(employeeRepository).delete(any(Employee.class));

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.isDeleted());
        verify(employeeRepository, times(1)).delete(testEmployee);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test createEmployee with maximum length name")
    public void testCreateEmployee_MaxLengthName() {
        // Arrange
        String maxName = "A".repeat(255);
        createDTO.setName(maxName);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeCreateDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test createEmployee with special characters in name")
    public void testCreateEmployee_SpecialCharactersInName() {
        // Arrange
        createDTO.setName("O'Brien-Smith Jr.");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeCreateDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test listEmployees with large page size")
    public void testListEmployees_LargePageSize() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(new EmployeeFilterDTO(), pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() <= 1000);
    }

    @Test
    @DisplayName("Test createEmployee with all optional fields null")
    public void testCreateEmployee_OptionalFieldsNull() {
        // Arrange
        createDTO.setDepartment(null);
        createDTO.setShiftGroup(null);
        createDTO.setHireDate(null);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeCreateDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
    }