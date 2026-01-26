package com.wms.ems.employee.service;

import com.wms.ems.employee.dto.EmployeeRequestDTO;
import com.wms.ems.employee.dto.EmployeeResponseDTO;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Covers: CRUD operations, validation, edge cases, boundary conditions
 * Epic: E02 - Employee Master Data (CRUD)
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeRequestDTO validRequest;

    @BeforeEach
    public void setUp() {
        // Arrange: Setup test data
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Day Shift");
        testEmployee.setHireDate(LocalDate.of(2024, 1, 1));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);

        validRequest = new EmployeeRequestDTO();
        validRequest.setBadgeId("EMP001");
        validRequest.setName("John Doe");
        validRequest.setRole("WORKER");
        validRequest.setDepartment("Warehouse");
        validRequest.setShiftGroup("Day Shift");
        validRequest.setHireDate(LocalDate.of(2024, 1, 1));
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    public void testCreateEmployee_ValidInput_ReturnsEmployeeResponseDTO() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        assertEquals("WORKER", result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullInput_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
    }

    @Test
    public void testCreateEmployee_EmptyName_ThrowsIllegalArgumentException() {
        // Arrange
        validRequest.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        validRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    public void testCreateEmployee_InvalidRole_ThrowsIllegalArgumentException() {
        // Arrange
        validRequest.setRole("INVALID_ROLE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    @Test
    public void testCreateEmployee_FutureHireDate_ThrowsIllegalArgumentException() {
        // Arrange
        validRequest.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequest);
        });
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    public void testGetEmployeeById_ValidId_ReturnsEmployeeResponseDTO() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployeeById_InvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    public void testGetEmployeeById_NullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    public void testGetEmployeeById_NegativeId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    public void testGetEmployeeById_DeletedEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(1L);
        });
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    public void testGetAllEmployees_ValidPageable_ReturnsPageOfEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
    }

    @Test
    public void testGetAllEmployees_EmptyResult_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testGetAllEmployees_NullPageable_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getAllEmployees(null);
        });
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    public void testUpdateEmployee_ValidInput_ReturnsUpdatedEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        validRequest.setName("Jane Doe");

        // Act
        EmployeeResponseDTO result = employeeService.updateEmployee(1L, validRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_InvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, validRequest);
        });
    }

    @Test
    public void testUpdateEmployee_NullRequest_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    public void testUpdateEmployee_DuplicateBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeIdAndIdNot("EMP002", 1L)).thenReturn(true);
        validRequest.setBadgeId("EMP002");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, validRequest);
        });
    }

    // ========== DELETE EMPLOYEE TESTS (SOFT DELETE) ==========

    @Test
    public void testDeleteEmployee_ValidId_SoftDeletesEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertTrue(testEmployee.isDeleted());
    }

    @Test
    public void testDeleteEmployee_InvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    public void testDeleteEmployee_AlreadyDeleted_ThrowsIllegalStateException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.deleteEmployee(1L);
        });
    }

    // ========== SEARCH EMPLOYEES TESTS ==========

    @Test
    public void testSearchEmployees_ByDepartment_ReturnsFilteredEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByDepartmentAndDeletedFalse("Warehouse")).thenReturn(employees);

        // Act
        List<EmployeeResponseDTO> result = employeeService.searchEmployeesByDepartment("Warehouse");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Warehouse", result.get(0).getDepartment());
    }

    @Test
    public void testSearchEmployees_ByRole_ReturnsFilteredEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByRoleAndDeletedFalse("WORKER")).thenReturn(employees);

        // Act
        List<EmployeeResponseDTO> result = employeeService.searchEmployeesByRole("WORKER");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WORKER", result.get(0).getRole());
    }

    @Test
    public void testSearchEmployees_ByStatus_ReturnsFilteredEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByStatusAndDeletedFalse("ACTIVE")).thenReturn(employees);

        // Act
        List<EmployeeResponseDTO> result = employeeService.searchEmployeesByStatus("ACTIVE");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    public void testSearchEmployees_EmptyDepartment_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.searchEmployeesByDepartment("");
        });
    }

    @Test
    public void testSearchEmployees_NullDepartment_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.searchEmployeesByDepartment(null);
        });
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testCreateEmployee_MaxLengthName_Success() {
        // Arrange
        String maxName = "A".repeat(255);
        validRequest.setName(maxName);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testCreateEmployee_MinLengthName_Success() {
        // Arrange
        validRequest.setName("A");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        validRequest.setName("O'Brien-Smith");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testGetAllEmployees_LargePageSize_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testGetAllEmployees_PageBeyondResults_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(100, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }
}