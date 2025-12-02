package com.warehouse.management.employee.service;

import com.warehouse.management.employee.entity.Employee;
import com.warehouse.management.employee.repository.EmployeeRepository;
import com.warehouse.management.exception.BadRequestException;
import com.warehouse.management.exception.ResourceNotFoundException;
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
 * Comprehensive JUnit test suite for EmployeeServiceImpl
 * Tests cover normal operations, boundary conditions, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee validEmployee;
    private Employee anotherEmployee;

    @BeforeEach
    void setUp() {
        // Arrange: Create valid test employee
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setEmail("john.doe@warehouse.com");
        validEmployee.setPhone("+1234567890");
        validEmployee.setRole("WORKER");
        validEmployee.setDepartment("SHIPPING");
        validEmployee.setShiftGroup("DAY");
        validEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        validEmployee.setStatus("ACTIVE");
        validEmployee.setTenantId("TENANT001");
        validEmployee.setDeleted(false);

        anotherEmployee = new Employee();
        anotherEmployee.setId(2L);
        anotherEmployee.setBadgeId("EMP002");
        anotherEmployee.setFirstName("Jane");
        anotherEmployee.setLastName("Smith");
        anotherEmployee.setEmail("jane.smith@warehouse.com");
        anotherEmployee.setPhone("+1987654321");
        anotherEmployee.setRole("SUPERVISOR");
        anotherEmployee.setDepartment("RECEIVING");
        anotherEmployee.setShiftGroup("NIGHT");
        anotherEmployee.setHireDate(LocalDate.of(2022, 6, 1));
        anotherEmployee.setStatus("ACTIVE");
        anotherEmployee.setTenantId("TENANT001");
        anotherEmployee.setDeleted(false);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@warehouse.com", result.getEmail());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, 
            () -> employeeService.create(validEmployee));
        assertTrue(exception.getMessage().contains("BadgeId already exists"));
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        validEmployee.setBadgeId(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.create(validEmployee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        validEmployee.setBadgeId("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.create(validEmployee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WhitespaceBadgeId_ThrowsException() {
        // Arrange
        validEmployee.setBadgeId("   ");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.create(validEmployee));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullFirstName_ThrowsException() {
        // Arrange
        validEmployee.setFirstName(null);
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.create(validEmployee));
    }

    @Test
    void testCreateEmployee_NullEmail_ThrowsException() {
        // Arrange
        validEmployee.setEmail(null);
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.create(validEmployee));
    }

    @Test
    void testCreateEmployee_InvalidEmailFormat_ThrowsException() {
        // Arrange
        validEmployee.setEmail("invalid-email");
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.create(validEmployee));
    }

    @Test
    void testCreateEmployee_NullTenantId_ThrowsException() {
        // Arrange
        validEmployee.setTenantId(null);
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.create(validEmployee));
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    void testGetEmployeeById_ExistingId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act
        Employee result = employeeService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEmployeeById_NonExistingId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> employeeService.getById(999L));
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.getById(null));
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    void testGetEmployeeById_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.getById(-1L));
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    void testGetEmployeeById_ZeroId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.getById(0L));
        verify(employeeRepository, never()).findById(any());
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    void testGetAllEmployees_WithPagination_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee, anotherEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByTenantIdAndDeletedFalse("TENANT001", pageable))
            .thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.getAll("TENANT001", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
        assertEquals("EMP002", result.getContent().get(1).getBadgeId());
        verify(employeeRepository, times(1)).findByTenantIdAndDeletedFalse("TENANT001", pageable);
    }

    @Test
    void testGetAllEmployees_EmptyResult_Success() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByTenantIdAndDeletedFalse("TENANT001", pageable))
            .thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getAll("TENANT001", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(employeeRepository, times(1)).findByTenantIdAndDeletedFalse("TENANT001", pageable);
    }

    @Test
    void testGetAllEmployees_NullTenantId_ThrowsException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.getAll(null, pageable));
        verify(employeeRepository, never()).findByTenantIdAndDeletedFalse(anyString(), any());
    }

    @Test
    void testGetAllEmployees_NullPageable_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.getAll("TENANT001", null));
        verify(employeeRepository, never()).findByTenantIdAndDeletedFalse(anyString(), any());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setFirstName("John Updated");
        updatedEmployee.setLastName("Doe Updated");
        updatedEmployee.setEmail("john.updated@warehouse.com");
        updatedEmployee.setPhone("+1111111111");
        updatedEmployee.setRole("SUPERVISOR");
        updatedEmployee.setDepartment("RECEIVING");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.update(1L, updatedEmployee);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NonExistingId_ThrowsException() {
        // Arrange
        Employee updatedEmployee = new Employee();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> employeeService.update(999L, updatedEmployee));
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NullId_ThrowsException() {
        // Arrange
        Employee updatedEmployee = new Employee();

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.update(null, updatedEmployee));
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    void testUpdateEmployee_NullEmployee_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.update(1L, null));
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    void testUpdateEmployee_InvalidEmail_ThrowsException() {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setEmail("invalid-email");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.update(1L, updatedEmployee));
    }

    @Test
    void testUpdateEmployee_ChangeTenantId_ThrowsException() {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setTenantId("DIFFERENT_TENANT");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.update(1L, updatedEmployee));
    }

    // ========== SOFT DELETE EMPLOYEE TESTS ==========

    @Test
    void testSoftDeleteEmployee_ExistingId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.softDelete(1L);

        // Assert
        assertTrue(validEmployee.isDeleted());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    @Test
    void testSoftDeleteEmployee_NonExistingId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.softDelete(999L));
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testSoftDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.softDelete(null));
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    void testSoftDeleteEmployee_AlreadyDeleted_Success() {
        // Arrange
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.softDelete(1L);

        // Assert
        assertTrue(validEmployee.isDeleted());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    // ========== FILTER BY DEPARTMENT TESTS ==========

    @Test
    void testGetEmployeesByDepartment_ValidDepartment_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByDepartmentAndTenantId("SHIPPING", "TENANT001"))
            .thenReturn(employees);

        // Act
        List<Employee> result = employeeService.getByDepartment("SHIPPING", "TENANT001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SHIPPING", result.get(0).getDepartment());
        verify(employeeRepository, times(1)).findByDepartmentAndTenantId("SHIPPING", "TENANT001");
    }

    @Test
    void testGetEmployeesByDepartment_NullDepartment_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> employeeService.getByDepartment(null, "TENANT001"));
        verify(employeeRepository, never()).findByDepartmentAndTenantId(anyString(), anyString());
    }

    @Test
    void testGetEmployeesByDepartment_EmptyDepartment_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> employeeService.getByDepartment("", "TENANT001"));
        verify(employeeRepository, never()).findByDepartmentAndTenantId(anyString(), anyString());
    }

    @Test
    void testGetEmployeesByDepartment_NullTenantId_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> employeeService.getByDepartment("SHIPPING", null));
        verify(employeeRepository, never()).findByDepartmentAndTenantId(anyString(), anyString());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testCreateEmployee_MaxLengthBadgeId_Success() {
        // Arrange
        String maxBadgeId = "A".repeat(50); // Assuming max length is 50
        validEmployee.setBadgeId(maxBadgeId);
        when(employeeRepository.findByBadgeId(maxBadgeId)).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(maxBadgeId, result.getBadgeId());
    }

    @Test
    void testCreateEmployee_MaxLengthFirstName_Success() {
        // Arrange
        String maxFirstName = "A".repeat(100); // Assuming max length is 100
        validEmployee.setFirstName(maxFirstName);
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(maxFirstName, result.getFirstName());
    }

    @Test
    void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        validEmployee.setHireDate(LocalDate.now().plusDays(1));
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> employeeService.create(validEmployee));
    }

    @Test
    void testCreateEmployee_VeryOldHireDate_Success() {
        // Arrange
        validEmployee.setHireDate(LocalDate.of(1990, 1, 1));
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        Employee result = employeeService.create(validEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.of(1990, 1, 1), result.getHireDate());
    }
}