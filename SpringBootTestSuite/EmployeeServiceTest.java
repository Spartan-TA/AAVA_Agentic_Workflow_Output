package com.warehouse.ems.employee;

import com.warehouse.ems.audit.AuditService;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal cases, boundary conditions, edge cases, and exception handling
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private Employee testEmployee2;

    @BeforeEach
    public void setUp() {
        // Setup test employee with valid data
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setPhone("+1234567890");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setRole("WORKER");
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setStatus("ACTIVE");
        testEmployee.setTenantId("TENANT001");

        // Setup second test employee
        testEmployee2 = new Employee();
        testEmployee2.setId(2L);
        testEmployee2.setBadgeId("EMP002");
        testEmployee2.setFirstName("Jane");
        testEmployee2.setLastName("Smith");
        testEmployee2.setEmail("jane.smith@warehouse.com");
        testEmployee2.setPhone("+1234567891");
        testEmployee2.setDepartment("Warehouse");
        testEmployee2.setRole("SUPERVISOR");
        testEmployee2.setHireDate(LocalDate.now());
        testEmployee2.setStatus("ACTIVE");
        testEmployee2.setTenantId("TENANT001");
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    public void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeId(testEmployee.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(employeeRepository, times(1)).existsByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(testEmployee);
        verify(auditService, times(1)).logCreate(anyString(), any(), anyString());
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsByBadgeId(testEmployee.getBadgeId())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });

        assertTrue(exception.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, times(1)).existsByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullEmployee_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testEmployee.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_InvalidEmail_ThrowsException() {
        // Arrange
        testEmployee.setEmail("invalid-email");
        when(employeeRepository.existsByBadgeId(testEmployee.getBadgeId())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
    }

    @Test
    public void testCreateEmployee_NullFirstName_ThrowsException() {
        // Arrange
        testEmployee.setFirstName(null);
        when(employeeRepository.existsByBadgeId(testEmployee.getBadgeId())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });
    }

    // ========== GET EMPLOYEES TESTS ==========

    @Test
    public void testGetEmployees_WithPagination_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee, testEmployee2);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
        assertEquals("EMP002", result.getContent().get(1).getBadgeId());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testGetEmployees_EmptyResult_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testGetEmployees_ByDepartment_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee, testEmployee2);
        when(employeeRepository.findByDepartment("Warehouse")).thenReturn(employees);

        // Act
        List<Employee> result = employeeService.getEmployeesByDepartment("Warehouse");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(employeeRepository, times(1)).findByDepartment("Warehouse");
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    public void testGetEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployee_InvalidId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            employeeService.getEmployee(999L);
        });

        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(null);
        });

        verify(employeeRepository, never()).findById(any());
    }

    @Test
    public void testGetEmployee_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(-1L);
        });

        verify(employeeRepository, never()).findById(any());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    public void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setFirstName("John Updated");
        updatedEmployee.setLastName("Doe Updated");
        updatedEmployee.setEmail("john.updated@warehouse.com");
        updatedEmployee.setPhone("+1234567899");
        updatedEmployee.setDepartment("Logistics");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.updateEmployee(1L, updatedEmployee);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(auditService, times(1)).logUpdate(anyString(), any(), anyString(), any(), any());
    }

    @Test
    public void testUpdateEmployee_InvalidId_ThrowsException() {
        // Arrange
        Employee updatedEmployee = new Employee();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            employeeService.updateEmployee(999L, updatedEmployee);
        });

        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_NullEmployee_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_PartialUpdate_Success() {
        // Arrange - Only update email
        Employee partialUpdate = new Employee();
        partialUpdate.setEmail("new.email@warehouse.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.updateEmployee(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    public void testDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).delete(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).delete(testEmployee);
        verify(auditService, times(1)).logDelete(anyString(), any(), anyString());
    }

    @Test
    public void testDeleteEmployee_InvalidId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            employeeService.deleteEmployee(999L);
        });

        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    public void testDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });

        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    // ========== SOFT DELETE TESTS ==========

    @Test
    public void testSoftDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(testEmployee);
        assertEquals("INACTIVE", testEmployee.getStatus());
    }

    // ========== BADGE ID VALIDATION TESTS ==========

    @Test
    public void testGetEmployeeByBadgeId_ValidBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
    }

    @Test
    public void testGetEmployeeByBadgeId_InvalidBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
    }

    @Test
    public void testGetEmployeeByBadgeId_NullBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByBadgeId(null);
        });
    }

    @Test
    public void testGetEmployeeByBadgeId_EmptyBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByBadgeId("");
        });
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testCreateEmployee_MaxLengthFields_Success() {
        // Arrange - Test with maximum length strings
        String maxLengthString = "A".repeat(255);
        testEmployee.setFirstName(maxLengthString);
        testEmployee.setLastName(maxLengthString);
        when(employeeRepository.existsByBadgeId(testEmployee.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(maxLengthString, result.getFirstName());
    }

    @Test
    public void testCreateEmployee_MinimumValidData_Success() {
        // Arrange - Test with minimum required fields
        Employee minEmployee = new Employee();
        minEmployee.setBadgeId("MIN001");
        minEmployee.setFirstName("A");
        minEmployee.setLastName("B");
        minEmployee.setEmail("a@b.c");
        when(employeeRepository.existsByBadgeId("MIN001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(minEmployee);

        // Act
        Employee result = employeeService.createEmployee(minEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("MIN001", result.getBadgeId());
    }

    // ========== MULTI-TENANT TESTS ==========

    @Test
    public void testGetEmployeesByTenant_ValidTenantId_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee, testEmployee2);
        when(employeeRepository.findByTenantId("TENANT001")).thenReturn(employees);

        // Act
        List<Employee> result = employeeService.getEmployeesByTenant("TENANT001");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> "TENANT001".equals(e.getTenantId())));
    }

    @Test
    public void testGetEmployeesByTenant_InvalidTenantId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findByTenantId("INVALID")).thenReturn(Arrays.asList());

        // Act
        List<Employee> result = employeeService.getEmployeesByTenant("INVALID");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}