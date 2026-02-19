package com.company.wems.service;

import com.company.wems.dto.request.EmployeeRequestDTO;
import com.company.wems.dto.response.EmployeeResponseDTO;
import com.company.wems.entity.Employee;
import com.company.wems.exception.BadRequestException;
import com.company.wems.exception.ConflictException;
import com.company.wems.exception.ResourceNotFoundException;
import com.company.wems.repository.EmployeeRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests all public methods with normal cases, edge cases, and boundary conditions
 * 
 * @author WEMS Test Suite Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        // Setup test employee entity
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setPhone("+1234567890");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setTenantId(1L);
        testEmployee.setDeleted(false);

        // Setup valid request DTO
        validRequest = new EmployeeRequestDTO();
        validRequest.setBadgeId("EMP001");
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPhone("+1234567890");
        validRequest.setRole("WORKER");
        validRequest.setDepartment("Warehouse");
        validRequest.setHireDate(LocalDate.of(2023, 1, 15));
        validRequest.setTenantId(1L);
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    void testCreateEmployee_WithValidData_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithNullBadgeId_ThrowsBadRequestException() {
        // Arrange
        validRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithEmptyBadgeId_ThrowsBadRequestException() {
        // Arrange
        validRequest.setBadgeId("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithWhitespaceBadgeId_ThrowsBadRequestException() {
        // Arrange
        validRequest.setBadgeId("   ");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithNullEmail_ThrowsBadRequestException() {
        // Arrange
        validRequest.setEmail(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithInvalidEmailFormat_ThrowsBadRequestException() {
        // Arrange
        validRequest.setEmail("invalid-email");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithDuplicateEmail_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithNullFirstName_ThrowsBadRequestException() {
        // Arrange
        validRequest.setFirstName(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithNullLastName_ThrowsBadRequestException() {
        // Arrange
        validRequest.setLastName(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithInvalidRole_ThrowsBadRequestException() {
        // Arrange
        validRequest.setRole("INVALID_ROLE");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithFutureHireDate_ThrowsBadRequestException() {
        // Arrange
        validRequest.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ==================== GET EMPLOYEE TESTS ====================

    @Test
    void testGetEmployeeById_WithValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByIdAndDeletedFalse(1L);
    }

    @Test
    void testGetEmployeeById_WithNonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    void testGetEmployeeById_WithNullId_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    void testGetEmployeeById_WithNegativeId_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    void testGetEmployeeById_WithZeroId_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.getEmployeeById(0L);
        });
    }

    @Test
    void testGetEmployeeByBadgeId_WithValidBadgeId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeResponseDTO result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeIdAndDeletedFalse("EMP001");
    }

    @Test
    void testGetEmployeeByBadgeId_WithNonExistentBadgeId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
    }

    @Test
    void testGetEmployeeByBadgeId_WithNullBadgeId_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.getEmployeeByBadgeId(null);
        });
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    void testGetAllEmployees_WithValidPageable_ReturnsPageOfEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    void testGetAllEmployees_WithEmptyResult_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    void testUpdateEmployee_WithValidData_ReturnsUpdatedEmployee() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        EmployeeRequestDTO updateRequest = new EmployeeRequestDTO();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@example.com");
        updateRequest.setPhone("+9876543210");
        updateRequest.setDepartment("Logistics");

        // Act
        EmployeeResponseDTO result = employeeService.updateEmployee(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithNonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithInvalidEmail_ThrowsBadRequestException() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(testEmployee));
        validRequest.setEmail("invalid-email");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.updateEmployee(1L, validRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    void testDeleteEmployee_WithValidId_SoftDeletesEmployee() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertTrue(testEmployee.isDeleted());
    }

    @Test
    void testDeleteEmployee_WithNonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testDeleteEmployee_WithNullId_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    // ==================== SEARCH EMPLOYEES TESTS ====================

    @Test
    void testSearchEmployees_ByDepartment_ReturnsMatchingEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByDepartmentAndDeletedFalse(anyString())).thenReturn(employees);

        // Act
        List<EmployeeResponseDTO> result = employeeService.searchEmployeesByDepartment("Warehouse");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Warehouse", testEmployee.getDepartment());
    }

    @Test
    void testSearchEmployees_ByRole_ReturnsMatchingEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByRoleAndDeletedFalse(anyString())).thenReturn(employees);

        // Act
        List<EmployeeResponseDTO> result = employeeService.searchEmployeesByRole("WORKER");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WORKER", testEmployee.getRole());
    }

    @Test
    void testSearchEmployees_WithNullDepartment_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.searchEmployeesByDepartment(null);
        });
    }

    @Test
    void testSearchEmployees_WithEmptyDepartment_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            employeeService.searchEmployeesByDepartment("");
        });
    }