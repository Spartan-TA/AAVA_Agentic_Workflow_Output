package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.EmployeeRole;
import com.warehouse.employee.domain.EmployeeStatus;
import com.warehouse.employee.dto.EmployeeDTO;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal operations, boundary conditions, and edge cases
 */
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

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
        testEmployee.setRole(EmployeeRole.WORKER);
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Day Shift");
        testEmployee.setHireDate(LocalDate.of(2024, 1, 1));
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDeleted(false);

        // Setup test employee DTO
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Day Shift");
        testEmployeeDTO.setHireDate(LocalDate.of(2024, 1, 1));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test create employee with valid data")
    public void testCreateEmployee_ValidData_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with duplicate badge ID")
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null badge ID")
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with empty badge ID")
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with null name")
    public void testCreateEmployee_NullName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with empty name")
    public void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with whitespace-only name")
    public void testCreateEmployee_WhitespaceName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setName("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with invalid role")
    public void testCreateEmployee_InvalidRole_ThrowsException() {
        // Arrange
        testEmployeeDTO.setRole("INVALID_ROLE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test create employee with future hire date")
    public void testCreateEmployee_FutureHireDate_Success() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        testEmployeeDTO.setHireDate(futureDate);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(futureDate, result.getHireDate());
    }

    @Test
    @DisplayName("Test create employee with past hire date")
    public void testCreateEmployee_PastHireDate_Success() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusYears(5);
        testEmployeeDTO.setHireDate(pastDate);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test get all employees - success")
    public void testGetAllEmployees_Success() {
        // Arrange
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setBadgeId("EMP002");
        employee2.setName("Jane Smith");
        employee2.setDeleted(false);

        List<Employee> employees = Arrays.asList(testEmployee, employee2);
        when(employeeRepository.findAllByDeletedFalse(any())).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(any());
    }

    @Test
    @DisplayName("Test get all employees - empty list")
    public void testGetAllEmployees_EmptyList() {
        // Arrange
        when(employeeRepository.findAllByDeletedFalse(any())).thenReturn(Arrays.asList());

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test get employee by ID - success")
    public void testGetEmployeeById_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("Test get employee by ID - not found")
    public void testGetEmployeeById_NotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    @DisplayName("Test get employee by ID - null ID")
    public void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    @DisplayName("Test get employee by ID - negative ID")
    public void testGetEmployeeById_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    @DisplayName("Test get employee by ID - zero ID")
    public void testGetEmployeeById_ZeroId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(0L);
        });
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test update employee - success")
    public void testUpdateEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        testEmployeeDTO.setName("John Doe Updated");
        testEmployeeDTO.setRole("SUPERVISOR");

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update employee - not found")
    public void testUpdateEmployee_NotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test update employee - null DTO")
    public void testUpdateEmployee_NullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    @DisplayName("Test update employee - change badge ID to duplicate")
    public void testUpdateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP002")).thenReturn(true);
        
        testEmployeeDTO.setBadgeId("EMP002");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, testEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Test update employee - change to inactive status")
    public void testUpdateEmployee_ChangeToInactive_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        testEmployeeDTO.setStatus("INACTIVE");

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test soft delete employee - success")
    public void testDeleteEmployee_Success() {
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
    @DisplayName("Test soft delete employee - not found")
    public void testDeleteEmployee_NotFound_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    @DisplayName("Test soft delete employee - null ID")
    public void testDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    @Test
    @DisplayName("Test soft delete employee - already deleted")
    public void testDeleteEmployee_AlreadyDeleted_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.deleteEmployee(1L);
        });
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test create employee with maximum length name")
    public void testCreateEmployee_MaxLengthName_Success() {
        // Arrange
        String maxLengthName = "A".repeat(255);
        testEmployeeDTO.setName(maxLengthName);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with special characters in name")
    public void testCreateEmployee_SpecialCharactersName_Success() {
        // Arrange
        testEmployeeDTO.setName("O'Brien-Smith Jr.");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with unicode characters in name")
    public void testCreateEmployee_UnicodeCharactersName_Success() {
        // Arrange
        testEmployeeDTO.setName("JosÃ© GarcÃ­a");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create employee with all roles")
    public void testCreateEmployee_AllRoles_Success() {
        // Test ADMIN role
        testEmployeeDTO.setRole("ADMIN");
        testEmployeeDTO.setBadgeId("EMP001");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        assertNotNull(employeeService.createEmployee(testEmployeeDTO));

        // Test HR role
        testEmployeeDTO.setRole("HR");
        testEmployeeDTO.setBadgeId("EMP002");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP002")).thenReturn(false);
        assertNotNull(employeeService.createEmployee(testEmployeeDTO));

        // Test SUPERVISOR role
        testEmployeeDTO.setRole("SUPERVISOR");
        testEmployeeDTO.setBadgeId("EMP003");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP003")).thenReturn(false);
        assertNotNull(employeeService.createEmployee(testEmployeeDTO));

        // Test WORKER role
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setBadgeId("EMP004");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP004")).thenReturn(false);
        assertNotNull(employeeService.createEmployee(testEmployeeDTO));
    }
}