package com.company.ems.employee;

import com.company.ems.common.exception.DuplicateBadgeIdException;
import com.company.ems.common.exception.NotFoundException;
import com.company.ems.employee.dto.EmployeeCreateRequest;
import com.company.ems.employee.dto.EmployeeUpdateRequest;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover: CRUD operations, validation, edge cases, boundary conditions
 */
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeCreateRequest createRequest;
    private EmployeeUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("B12345");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Receiving");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2024, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
        
        // Setup create request
        createRequest = new EmployeeCreateRequest();
        createRequest.setBadgeId("B12345");
        createRequest.setName("John Doe");
        createRequest.setRole("WORKER");
        createRequest.setDepartment("Receiving");
        createRequest.setShiftGroup("A");
        createRequest.setHireDate(LocalDate.of(2024, 1, 15));
        
        // Setup update request
        updateRequest = new EmployeeUpdateRequest();
        updateRequest.setName("John Updated");
        updateRequest.setDepartment("Shipping");
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Create Employee - Valid Input")
    void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("B12345", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        assertEquals("WORKER", result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Duplicate Badge ID")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString()))
            .thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(DuplicateBadgeIdException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Null Badge ID")
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        createRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Create Employee - Empty Name")
    void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        createRequest.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Create Employee - Invalid Role")
    void testCreateEmployee_InvalidRole_ThrowsException() {
        // Arrange
        createRequest.setRole("INVALID_ROLE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Create Employee - Future Hire Date")
    void testCreateEmployee_FutureHireDate_Success() {
        // Arrange
        createRequest.setHireDate(LocalDate.now().plusDays(30));
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHireDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("Create Employee - Very Long Name (Boundary)")
    void testCreateEmployee_VeryLongName_Success() {
        // Arrange
        String longName = "A".repeat(128); // Max length
        createRequest.setName(longName);
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    // ==================== GET EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Get Employee - Valid ID")
    void testGetEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("Get Employee - Non-existent ID")
    void testGetEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            employeeService.getEmployee(999L);
        });
    }

    @Test
    @DisplayName("Get Employee - Null ID")
    void testGetEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(null);
        });
    }

    @Test
    @DisplayName("Get Employee - Negative ID")
    void testGetEmployee_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(-1L);
        });
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Update Employee - Valid Input")
    void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.updateEmployee(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Non-existent ID")
    void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            employeeService.updateEmployee(999L, updateRequest);
        });
    }

    @Test
    @DisplayName("Update Employee - Partial Update")
    void testUpdateEmployee_PartialUpdate_Success() {
        // Arrange
        updateRequest.setName("Updated Name");
        updateRequest.setDepartment(null); // Don't update department
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.updateEmployee(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Receiving", result.getDepartment()); // Original department preserved
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Soft Delete Employee - Valid ID")
    void testSoftDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.getDeleted());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    @DisplayName("Soft Delete Employee - Non-existent ID")
    void testSoftDeleteEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            employeeService.softDeleteEmployee(999L);
        });
    }

    @Test
    @DisplayName("Soft Delete Employee - Already Deleted")
    void testSoftDeleteEmployee_AlreadyDeleted_Success() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.getDeleted());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    // ==================== LIST EMPLOYEES TESTS ====================

    @Test
    @DisplayName("List Employees - No Filter")
    void testListEmployees_NoFilter_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDepartment(null, pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.listEmployees(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("List Employees - With Department Filter")
    void testListEmployees_WithDepartmentFilter_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDepartment("Receiving", pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.listEmployees("Receiving", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Receiving", result.getContent().get(0).getDepartment());
    }

    @Test
    @DisplayName("List Employees - Empty Result")
    void testListEmployees_EmptyResult_Success() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDepartment("NonExistent", pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.listEmployees("NonExistent", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("List Employees - Large Page Size (Boundary)")
    void testListEmployees_LargePageSize_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 1000);
        when(employeeRepository.findAllByDepartment(null, pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.listEmployees(null, pageable);

        // Assert
        assertNotNull(result);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Edge Case - Special Characters in Name")
    void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        createRequest.setName("O'Brien-Smith Jr.");
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Edge Case - Unicode Characters in Name")
    void testCreateEmployee_UnicodeCharacters_Success() {
        // Arrange
        createRequest.setName("JosÃ© GarcÃ­a");
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Edge Case - Minimum Valid Badge ID Length")
    void testCreateEmployee_MinimumBadgeIdLength_Success() {
        // Arrange
        createRequest.setBadgeId("B1");
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Edge Case - Maximum Valid Badge ID Length")
    void testCreateEmployee_MaximumBadgeIdLength_Success() {
        // Arrange
        createRequest.setBadgeId("B" + "1".repeat(31)); // 32 chars total
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Edge Case - Hire Date on Leap Year")
    void testCreateEmployee_LeapYearHireDate_Success() {
        // Arrange
        createRequest.setHireDate(LocalDate.of(2024, 2, 29));
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }
}