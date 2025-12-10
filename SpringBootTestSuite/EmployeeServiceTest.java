package com.warehouse.employee;

import com.warehouse.dto.EmployeeDTO;
import com.warehouse.exception.DuplicateResourceException;
import com.warehouse.exception.ResourceNotFoundException;
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
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal cases, boundary conditions, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDTO validEmployeeDTO;
    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        // Arrange: Set up valid test data
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setBadgeId("EMP001");
        validEmployeeDTO.setFirstName("John");
        validEmployeeDTO.setLastName("Doe");
        validEmployeeDTO.setEmail("john.doe@warehouse.com");
        validEmployeeDTO.setRole(EmployeeRole.WORKER);
        validEmployeeDTO.setDepartment("Warehouse");
        validEmployeeDTO.setShiftGroup("Morning");
        validEmployeeDTO.setHireDate(LocalDate.now());
        validEmployeeDTO.setStatus(EmployeeStatus.ACTIVE);

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setEmail("john.doe@warehouse.com");
        validEmployee.setRole(EmployeeRole.WORKER);
        validEmployee.setDepartment("Warehouse");
        validEmployee.setShiftGroup("Morning");
        validEmployee.setHireDate(LocalDate.now());
        validEmployee.setStatus(EmployeeStatus.ACTIVE);
        validEmployee.setDeleted(false);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    public void testCreateEmployee_WithValidData_ShouldReturnCreatedEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(validEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ShouldThrowDuplicateResourceException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_WithNullBadgeId_ShouldThrowIllegalArgumentException() {
        // Arrange
        validEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_WithEmptyBadgeId_ShouldThrowIllegalArgumentException() {
        // Arrange
        validEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_WithNullFirstName_ShouldThrowIllegalArgumentException() {
        // Arrange
        validEmployeeDTO.setFirstName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_WithEmptyFirstName_ShouldThrowIllegalArgumentException() {
        // Arrange
        validEmployeeDTO.setFirstName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_WithInvalidEmailFormat_ShouldThrowIllegalArgumentException() {
        // Arrange
        validEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_WithNullEmail_ShouldThrowIllegalArgumentException() {
        // Arrange
        validEmployeeDTO.setEmail(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_WithAllRoles_ShouldSucceed() {
        // Test ADMIN role
        validEmployeeDTO.setRole(EmployeeRole.ADMIN);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        assertDoesNotThrow(() -> employeeService.createEmployee(validEmployeeDTO));

        // Test HR role
        validEmployeeDTO.setRole(EmployeeRole.HR);
        assertDoesNotThrow(() -> employeeService.createEmployee(validEmployeeDTO));

        // Test SUPERVISOR role
        validEmployeeDTO.setRole(EmployeeRole.SUPERVISOR);
        assertDoesNotThrow(() -> employeeService.createEmployee(validEmployeeDTO));

        // Test WORKER role
        validEmployeeDTO.setRole(EmployeeRole.WORKER);
        assertDoesNotThrow(() -> employeeService.createEmployee(validEmployeeDTO));
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    public void testGetEmployeeById_WithValidId_ShouldReturnEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployeeById_WithNonExistentId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetEmployeeById_WithNullId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    public void testGetEmployeeById_WithNegativeId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    public void testGetEmployeeById_WithZeroId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(0L);
        });
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    public void testGetAllEmployees_WithValidPageable_ShouldReturnPagedEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDeletedFalse(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
        verify(employeeRepository, times(1)).findByDeletedFalse(pageable);
    }

    @Test
    public void testGetAllEmployees_WithEmptyResult_ShouldReturnEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    public void testGetAllEmployees_WithNullPageable_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getAllEmployees(null);
        });
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    public void testUpdateEmployee_WithValidData_ShouldReturnUpdatedEmployee() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setFirstName("Jane");
        updateDTO.setLastName("Smith");
        updateDTO.setDepartment("Logistics");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_WithNonExistentId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, validEmployeeDTO);
        });
    }

    @Test
    public void testUpdateEmployee_WithNullId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(null, validEmployeeDTO);
        });
    }

    @Test
    public void testUpdateEmployee_WithNullDTO_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    // ========== DELETE EMPLOYEE (SOFT DELETE) TESTS ==========

    @Test
    public void testDeleteEmployee_WithValidId_ShouldSoftDeleteEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testDeleteEmployee_WithNonExistentId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    public void testDeleteEmployee_WithNullId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    @Test
    public void testDeleteEmployee_WithAlreadyDeletedEmployee_ShouldNotThrowException() {
        // Arrange
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
    }

    // ========== RESTORE EMPLOYEE TESTS ==========

    @Test
    public void testRestoreEmployee_WithValidDeletedEmployee_ShouldRestoreEmployee() {
        // Arrange
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDTO result = employeeService.restoreEmployee(1L);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testRestoreEmployee_WithNonExistentId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.restoreEmployee(999L);
        });
    }

    @Test
    public void testRestoreEmployee_WithNullId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.restoreEmployee(null);
        });
    }

    @Test
    public void testRestoreEmployee_WithActiveEmployee_ShouldNotThrowException() {
        // Arrange
        validEmployee.setDeleted(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.restoreEmployee(1L));
    }

    // ========== SEARCH EMPLOYEES TESTS ==========

    @Test
    public void testSearchEmployees_ByDepartment_ShouldReturnFilteredEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByDepartment("Warehouse")).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployeesByDepartment("Warehouse");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Warehouse", result.get(0).getDepartment());
    }

    @Test
    public void testSearchEmployees_ByRole_ShouldReturnFilteredEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByRole(EmployeeRole.WORKER)).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployeesByRole(EmployeeRole.WORKER);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EmployeeRole.WORKER, result.get(0).getRole());
    }

    @Test
    public void testSearchEmployees_ByStatus_ShouldReturnFilteredEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByStatus(EmployeeStatus.ACTIVE)).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployeesByStatus(EmployeeStatus.ACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EmployeeStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    public void testSearchEmployees_WithNullDepartment_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.searchEmployeesByDepartment(null);
        });
    }

    @Test
    public void testSearchEmployees_WithEmptyDepartment_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.searchEmployeesByDepartment("");
        });
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testCreateEmployee_WithMaxLengthFirstName_ShouldSucceed() {
        // Arrange
        String maxLengthName = "A".repeat(50); // Assuming max length is 50
        validEmployeeDTO.setFirstName(maxLengthName);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.createEmployee(validEmployeeDTO));
    }

    @Test
    public void testCreateEmployee_WithMinLengthFirstName_ShouldSucceed() {
        // Arrange
        validEmployeeDTO.setFirstName("A");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.createEmployee(validEmployeeDTO));
    }

    @Test
    public void testCreateEmployee_WithSpecialCharactersInName_ShouldSucceed() {
        // Arrange
        validEmployeeDTO.setFirstName("O'Brien");
        validEmployeeDTO.setLastName("Smith-Jones");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.createEmployee(validEmployeeDTO));
    }

    @Test
    public void testCreateEmployee_WithFutureHireDate_ShouldThrowIllegalArgumentException() {
        // Arrange
        validEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_WithPastHireDate_ShouldSucceed() {
        // Arrange
        validEmployeeDTO.setHireDate(LocalDate.now().minusYears(5));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        assertDoesNotThrow(() -> employeeService.createEmployee(validEmployeeDTO));
    }
}