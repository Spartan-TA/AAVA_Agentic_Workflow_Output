package com.wms.ems.employee;

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
 * Comprehensive JUnit test suite for EmployeeService.
 * Tests cover CRUD operations, validation, edge cases, and exception handling.
 * 
 * @author Warehouse EMS Test Suite
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDto testEmployeeDto;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test employee entity
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Day Shift");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);

        // Arrange: Create test employee DTO
        testEmployeeDto = new EmployeeDto();
        testEmployeeDto.setBadgeId("EMP001");
        testEmployeeDto.setName("John Doe");
        testEmployeeDto.setRole("WORKER");
        testEmployeeDto.setDepartment("Warehouse");
        testEmployeeDto.setShiftGroup("Day Shift");
        testEmployeeDto.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDto.setStatus("ACTIVE");
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    public void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullInput_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDto.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDto.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    public void testCreateEmployee_NullName_ThrowsException() {
        // Arrange
        testEmployeeDto.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    public void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        testEmployeeDto.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    public void testCreateEmployee_InvalidRole_ThrowsException() {
        // Arrange
        testEmployeeDto.setRole("INVALID_ROLE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    @Test
    public void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        testEmployeeDto.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDto);
        });
    }

    // ==================== READ EMPLOYEE TESTS ====================

    @Test
    public void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDto result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testGetEmployeeById_InvalidId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    public void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    public void testGetEmployeeById_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    public void testGetEmployeeByBadgeId_ValidBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDto result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    public void testGetEmployeeByBadgeId_InvalidBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
    }

    @Test
    public void testListEmployees_WithPagination_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.listEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
    }

    @Test
    public void testListEmployees_EmptyResult_Success() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.listEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    public void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        testEmployeeDto.setName("Jane Doe");

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, testEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_InvalidId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDto);
        });
    }

    @Test
    public void testUpdateEmployee_NullDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    public void testUpdateEmployee_ChangeBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        testEmployeeDto.setBadgeId("EMP002");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.updateEmployee(1L, testEmployeeDto);
        });
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    public void testSoftDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testSoftDeleteEmployee_InvalidId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.softDeleteEmployee(999L);
        });
    }

    @Test
    public void testSoftDeleteEmployee_AlreadyDeleted_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.softDeleteEmployee(1L);
        });
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    public void testValidateEmployee_AllFieldsValid_Success() {
        // Act
        boolean result = employeeService.validateEmployee(testEmployeeDto);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testValidateEmployee_InvalidEmail_ThrowsException() {
        // Arrange
        testEmployeeDto.setEmail("invalid-email");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.validateEmployee(testEmployeeDto);
        });
    }

    @Test
    public void testValidateEmployee_InvalidPhoneNumber_ThrowsException() {
        // Arrange
        testEmployeeDto.setPhoneNumber("123");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.validateEmployee(testEmployeeDto);
        });
    }
}