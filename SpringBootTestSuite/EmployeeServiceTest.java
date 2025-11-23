package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.employee.service.impl.EmployeeServiceImpl;
import com.warehouse.ems.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService
 * Tests cover normal operations, boundary conditions, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee testEmployee;
    private EmployeeDto testEmployeeDto;

    @BeforeEach
    void setUp() {
        // Setup test employee entity
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setWarehouseId(1L);
        testEmployee.setDeleted(false);

        // Setup test employee DTO
        testEmployeeDto = new EmployeeDto();
        testEmployeeDto.setBadgeId("EMP001");
        testEmployeeDto.setName("John Doe");
        testEmployeeDto.setRole("WORKER");
        testEmployeeDto.setDepartment("Warehouse");
        testEmployeeDto.setShiftGroup("A");
        testEmployeeDto.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDto.setStatus("ACTIVE");
        testEmployeeDto.setWarehouseId(1L);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).existsByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.createEmployee(testEmployeeDto)
        );
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, times(1)).existsByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDto.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDto));
    }

    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDto.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDto));
    }

    @Test
    void testCreateEmployee_NullName_ThrowsException() {
        // Arrange
        testEmployeeDto.setName(null);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDto));
    }

    @Test
    void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        testEmployeeDto.setName("");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDto));
    }

    @Test
    void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        testEmployeeDto.setHireDate(LocalDate.now().plusDays(1));
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDto));
    }

    @Test
    void testCreateEmployee_InvalidRole_ThrowsException() {
        // Arrange
        testEmployeeDto.setRole("INVALID_ROLE");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDto));
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    void testGetAllEmployees_ValidPageable_ReturnsPage() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    void testGetAllEmployees_EmptyResult_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    void testGetAllEmployees_LargePageSize_ReturnsCorrectPage() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee, testEmployee, testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 100);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDto result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEmployeeById_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(999L));
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeeById(null));
    }

    @Test
    void testGetEmployeeById_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeeById(-1L));
    }

    @Test
    void testGetEmployeeById_ZeroId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeeById(0L));
    }

    @Test
    void testGetEmployeeById_DeletedEmployee_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(1L));
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        testEmployeeDto.setName("Jane Doe");

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, testEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(999L, testEmployeeDto));
    }

    @Test
    void testUpdateEmployee_ChangeBadgeIdToDuplicate_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP002")).thenReturn(true);
        testEmployeeDto.setBadgeId("EMP002");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, testEmployeeDto));
    }

    @Test
    void testUpdateEmployee_NullName_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        testEmployeeDto.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, testEmployeeDto));
    }

    @Test
    void testUpdateEmployee_EmptyDepartment_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        testEmployeeDto.setDepartment("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, testEmployeeDto));
    }

    // ========== SOFT DELETE EMPLOYEE TESTS ==========

    @Test
    void testSoftDeleteEmployee_ValidId_Success() {
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
    void testSoftDeleteEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.softDeleteEmployee(999L));
    }

    @Test
    void testSoftDeleteEmployee_AlreadyDeleted_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.softDeleteEmployee(1L));
    }

    @Test
    void testSoftDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.softDeleteEmployee(null));
    }

    @Test
    void testSoftDeleteEmployee_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.softDeleteEmployee(-1L));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testCreateEmployee_MaxLengthName_Success() {
        // Arrange
        String maxLengthName = "A".repeat(255);
        testEmployeeDto.setName(maxLengthName);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testCreateEmployee_MinimumValidData_Success() {
        // Arrange
        testEmployeeDto.setShiftGroup(null);
        testEmployeeDto.setWarehouseId(null);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllEmployees_FirstPage_ReturnsCorrectData() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
    }

    @Test
    void testGetAllEmployees_LastPage_ReturnsCorrectData() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(2, 10), 25);
        Pageable pageable = PageRequest.of(2, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isLast());
    }
}