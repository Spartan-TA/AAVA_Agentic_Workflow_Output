package com.company.wms.employee.service;

import com.company.wms.employee.model.Employee;
import com.company.wms.employee.model.EmployeeStatus;
import com.company.wms.employee.model.Role;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.employee.dto.EmployeeDto;
import com.company.wms.exception.ResourceNotFoundException;
import com.company.wms.exception.ValidationException;
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
 * Covers: CRUD operations, validation, edge cases, boundary conditions
 * Epic E02: Employee Master Data (CRUD)
 */
@DisplayName("Employee Service Test Suite")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDto testEmployeeDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole(Role.WORKER);
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDeleted(false);

        // Setup test DTO
        testEmployeeDto = new EmployeeDto();
        testEmployeeDto.setName("John Doe");
        testEmployeeDto.setBadgeId("EMP001");
        testEmployeeDto.setRole(Role.WORKER);
        testEmployeeDto.setDepartment("Warehouse");
        testEmployeeDto.setShiftGroup("A");
        testEmployeeDto.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDto.setStatus(EmployeeStatus.ACTIVE);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Create Employee - Valid Input")
    void testCreateEmployee_ValidInput() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals(Role.WORKER, result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Null Name")
    void testCreateEmployee_NullName() {
        // Arrange
        testEmployeeDto.setName(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Create Employee - Empty Name")
    void testCreateEmployee_EmptyName() {
        // Arrange
        testEmployeeDto.setName("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Create Employee - Null BadgeId")
    void testCreateEmployee_NullBadgeId() {
        // Arrange
        testEmployeeDto.setBadgeId(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Create Employee - Duplicate BadgeId")
    void testCreateEmployee_DuplicateBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Create Employee - Invalid BadgeId Format")
    void testCreateEmployee_InvalidBadgeIdFormat() {
        // Arrange
        testEmployeeDto.setBadgeId("INVALID@#$");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Create Employee - Future Hire Date")
    void testCreateEmployee_FutureHireDate() {
        // Arrange
        testEmployeeDto.setHireDate(LocalDate.now().plusDays(30));
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Very Old Hire Date")
    void testCreateEmployee_VeryOldHireDate() {
        // Arrange
        testEmployeeDto.setHireDate(LocalDate.of(1950, 1, 1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Create Employee - Null Role")
    void testCreateEmployee_NullRole() {
        // Arrange
        testEmployeeDto.setRole(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Create Employee - Maximum Name Length")
    void testCreateEmployee_MaximumNameLength() {
        // Arrange
        String longName = "A".repeat(255);
        testEmployeeDto.setName(longName);
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Exceeds Maximum Name Length")
    void testCreateEmployee_ExceedsMaximumNameLength() {
        // Arrange
        String tooLongName = "A".repeat(256);
        testEmployeeDto.setName(tooLongName);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    // ========== RETRIEVE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Get Employee By Id - Valid Id")
    void testGetEmployeeById_ValidId() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("Get Employee By Id - Non-Existent Id")
    void testGetEmployeeById_NonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    @DisplayName("Get Employee By Id - Null Id")
    void testGetEmployeeById_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    @DisplayName("Get Employee By Id - Negative Id")
    void testGetEmployeeById_NegativeId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    @DisplayName("Get Employee By BadgeId - Valid BadgeId")
    void testGetEmployeeByBadgeId_ValidBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    @DisplayName("Get Employee By BadgeId - Non-Existent BadgeId")
    void testGetEmployeeByBadgeId_NonExistentBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
    }

    @Test
    @DisplayName("Get All Employees - With Pagination")
    void testGetAllEmployees_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Get All Employees - Empty Result")
    void testGetAllEmployees_EmptyResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Update Employee - Valid Update")
    void testUpdateEmployee_ValidUpdate() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setName("Jane Doe");
        updateDto.setDepartment("Logistics");

        // Act
        Employee result = employeeService.updateEmployee(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Non-Existent Employee")
    void testUpdateEmployee_NonExistentEmployee() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Update Employee - Duplicate BadgeId")
    void testUpdateEmployee_DuplicateBadgeId() {
        // Arrange
        Employee anotherEmployee = new Employee();
        anotherEmployee.setId(2L);
        anotherEmployee.setBadgeId("EMP002");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP002")).thenReturn(Optional.of(anotherEmployee));
        
        testEmployeeDto.setBadgeId("EMP002");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.updateEmployee(1L, testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Update Employee - Null Update Data")
    void testUpdateEmployee_NullUpdateData() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Soft Delete Employee - Valid Id")
    void testSoftDeleteEmployee_ValidId() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Soft Delete Employee - Non-Existent Id")
    void testSoftDeleteEmployee_NonExistentId() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.softDeleteEmployee(999L);
        });
    }

    @Test
    @DisplayName("Soft Delete Employee - Already Deleted")
    void testSoftDeleteEmployee_AlreadyDeleted() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.softDeleteEmployee(1L);
        });
    }

    @Test
    @DisplayName("Soft Delete Employee - Null Id")
    void testSoftDeleteEmployee_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.softDeleteEmployee(null);
        });
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Create Employee - Special Characters in Name")
    void testCreateEmployee_SpecialCharactersInName() {
        // Arrange
        testEmployeeDto.setName("O'Brien-Smith");
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Unicode Characters in Name")
    void testCreateEmployee_UnicodeCharactersInName() {
        // Arrange
        testEmployeeDto.setName("JosÃ© GarcÃ­a");
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - All Roles")
    void testCreateEmployee_AllRoles() {
        // Test each role
        for (Role role : Role.values()) {
            testEmployeeDto.setRole(role);
            testEmployeeDto.setBadgeId("EMP" + role.name());
            when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            Employee result = employeeService.createEmployee(testEmployeeDto);
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Create Employee - All Statuses")
    void testCreateEmployee_AllStatuses() {
        // Test each status
        for (EmployeeStatus status : EmployeeStatus.values()) {
            testEmployeeDto.setStatus(status);
            testEmployeeDto.setBadgeId("EMP" + status.name());
            when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            Employee result = employeeService.createEmployee(testEmployeeDto);
            assertNotNull(result);
        }
    }
}