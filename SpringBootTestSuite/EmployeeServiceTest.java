package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.dto.EmployeeDTO;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.mapper.EmployeeMapper;
import com.warehouse.ems.employee.repository.EmployeeRepository;
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

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal operations, boundary conditions, and edge cases
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
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee entity
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        
        // Setup test employee DTO
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Morning");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("Test create employee with valid data")
    public void testCreateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        assertEquals("ACTIVE", result.getStatus());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with duplicate badge ID")
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.create(testEmployeeDTO)
        );
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null badge ID")
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.create(testEmployeeDTO));
    }

    @Test
    @DisplayName("Test create employee with empty name")
    public void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setName("");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.create(testEmployeeDTO));
    }

    @Test
    @DisplayName("Test create employee with null hire date")
    public void testCreateEmployee_NullHireDate_ThrowsException() {
        // Arrange
        testEmployeeDTO.setHireDate(null);

        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.create(testEmployeeDTO));
    }

    @Test
    @DisplayName("Test create employee with future hire date")
    public void testCreateEmployee_FutureHireDate_Success() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        testEmployeeDTO.setHireDate(futureDate);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(futureDate, result.getHireDate());
    }

    // ========== READ TESTS ==========

    @Test
    @DisplayName("Test get employee by ID - valid ID")
    public void testGetById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    @DisplayName("Test get employee by ID - non-existent ID")
    public void testGetById_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> employeeService.getById(999L)
        );
        assertTrue(exception.getMessage().contains("Employee not found"));
    }

    @Test
    @DisplayName("Test get employee by ID - null ID")
    public void testGetById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.getById(null));
    }

    @Test
    @DisplayName("Test get employee by ID - negative ID")
    public void testGetById_NegativeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.getById(-1L));
    }

    @Test
    @DisplayName("Test list employees with pagination - no filters")
    public void testList_NoFilters_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAllActive(pageable)).thenReturn(employeePage);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.list(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
    }

    @Test
    @DisplayName("Test list employees with status filter")
    public void testList_WithStatusFilter_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAllByStatus("ACTIVE", pageable)).thenReturn(employeePage);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.list("ACTIVE", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Test list employees with department and status filters")
    public void testList_WithDepartmentAndStatusFilters_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAllByDepartmentAndStatus("Warehouse", "ACTIVE", pageable))
            .thenReturn(employeePage);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.list("ACTIVE", "Warehouse", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Warehouse", result.getContent().get(0).getDepartment());
    }

    @Test
    @DisplayName("Test list employees with empty result")
    public void testList_EmptyResult_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeRepository.findAllActive(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.list(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("Test update employee with valid data")
    public void testUpdate_ValidData_Success() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Jane Doe");
        updateDTO.setBadgeId("EMP001");
        updateDTO.setRole("SUPERVISOR");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(updateDTO);

        // Act
        EmployeeDTO result = employeeService.update(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee with non-existent ID")
    public void testUpdate_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            EntityNotFoundException.class,
            () -> employeeService.update(999L, testEmployeeDTO)
        );
    }

    @Test
    @DisplayName("Test update employee with duplicate badge ID")
    public void testUpdate_DuplicateBadgeId_ThrowsException() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setBadgeId("EMP002");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeId("EMP002")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.update(1L, updateDTO)
        );
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
    }

    @Test
    @DisplayName("Test update employee with null ID")
    public void testUpdate_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.update(null, testEmployeeDTO));
    }

    @Test
    @DisplayName("Test update employee with same badge ID")
    public void testUpdate_SameBadgeId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.update(1L, testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("Test soft delete employee with valid ID")
    public void testDelete_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.delete(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertEquals("DELETED", testEmployee.getStatus());
    }

    @Test
    @DisplayName("Test delete employee with non-existent ID")
    public void testDelete_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            EntityNotFoundException.class,
            () -> employeeService.delete(999L)
        );
    }

    @Test
    @DisplayName("Test delete employee with null ID")
    public void testDelete_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.delete(null));
    }

    @Test
    @DisplayName("Test delete already deleted employee")
    public void testDelete_AlreadyDeleted_Success() {
        // Arrange
        testEmployee.setStatus("DELETED");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.delete(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertEquals("DELETED", testEmployee.getStatus());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test create employee with maximum length name")
    public void testCreateEmployee_MaxLengthName_Success() {
        // Arrange
        String maxLengthName = "A".repeat(128);
        testEmployeeDTO.setName(maxLengthName);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with maximum length badge ID")
    public void testCreateEmployee_MaxLengthBadgeId_Success() {
        // Arrange
        String maxLengthBadgeId = "B".repeat(32);
        testEmployeeDTO.setBadgeId(maxLengthBadgeId);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test list employees with large page size")
    public void testList_LargePageSize_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAllActive(pageable)).thenReturn(employeePage);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.list(null, null, pageable);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with special characters in name")
    public void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testEmployeeDTO.setName("O'Brien-Smith Jr.");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }
}