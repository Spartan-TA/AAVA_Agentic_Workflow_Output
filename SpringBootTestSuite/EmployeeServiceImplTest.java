package com.company.ems.employee.service.impl;

import com.company.ems.employee.dto.EmployeeDto;
import com.company.ems.employee.model.Employee;
import com.company.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeServiceImpl.
 * 
 * Tests cover:
 * - Normal CRUD operations
 * - Boundary conditions (null values, empty strings, edge dates)
 * - Edge cases (duplicate badge IDs, non-existent employees)
 * - Pagination and filtering
 * - Soft-delete functionality
 * - Data validation
 * 
 * Epic E02: Employee Master Data (CRUD)
 * 
 * @author EMS Test Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeServiceImpl Unit Tests")
public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDto validEmployeeDto;
    private Employee validEmployee;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup valid test data
        validEmployeeDto = EmployeeDto.builder()
                .name("John Doe")
                .badgeId("EMP001")
                .role("Warehouse Worker")
                .department("Shipping")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .warehouseId(1L)
                .build();

        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("Warehouse Worker")
                .department("Shipping")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .warehouseId(1L)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        pageable = PageRequest.of(0, 10);
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Create Employee - Valid Input - Success")
    void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(validEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).existsByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Duplicate Badge ID - Throws Exception")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(validEmployeeDto)
        );
        assertTrue(exception.getMessage().contains("already exists"));
        verify(employeeRepository, times(1)).existsByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Null Name - Handled by Validation")
    void testCreateEmployee_NullName_ValidationHandled() {
        // Arrange
        EmployeeDto dtoWithNullName = EmployeeDto.builder()
                .name(null)
                .badgeId("EMP002")
                .role("Worker")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(dtoWithNullName);

        // Assert - Service layer doesn't validate, relies on controller validation
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Empty Badge ID - Handled")
    void testCreateEmployee_EmptyBadgeId_Handled() {
        // Arrange
        EmployeeDto dtoWithEmptyBadge = EmployeeDto.builder()
                .name("Jane Doe")
                .badgeId("")
                .role("Worker")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(dtoWithEmptyBadge);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Future Hire Date - Accepted")
    void testCreateEmployee_FutureHireDate_Accepted() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        validEmployeeDto.setHireDate(futureDate);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(validEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Past Hire Date - Accepted")
    void testCreateEmployee_PastHireDate_Accepted() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        validEmployeeDto.setHireDate(pastDate);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(validEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Null Warehouse ID - Accepted")
    void testCreateEmployee_NullWarehouseId_Accepted() {
        // Arrange
        validEmployeeDto.setWarehouseId(null);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(validEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Maximum Length Name - Accepted")
    void testCreateEmployee_MaxLengthName_Accepted() {
        // Arrange
        String maxLengthName = "A".repeat(100);
        validEmployeeDto.setName(maxLengthName);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeDto result = employeeService.createEmployee(validEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Update Employee - Valid Input - Success")
    void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        validEmployeeDto.setName("John Updated");

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, validEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Non-Existent ID - Throws Exception")
    void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NoSuchElementException.class,
                () -> employeeService.updateEmployee(999L, validEmployeeDto)
        );
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Null ID - Throws Exception")
    void testUpdateEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(
                Exception.class,
                () -> employeeService.updateEmployee(null, validEmployeeDto)
        );
    }

    @Test
    @DisplayName("Update Employee - Change Status to INACTIVE - Success")
    void testUpdateEmployee_ChangeStatusToInactive_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        validEmployeeDto.setStatus("INACTIVE");

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, validEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Change Department - Success")
    void testUpdateEmployee_ChangeDepartment_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        validEmployeeDto.setDepartment("Receiving");

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, validEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Update Employee - Null Department - Accepted")
    void testUpdateEmployee_NullDepartment_Accepted() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        validEmployeeDto.setDepartment(null);

        // Act
        EmployeeDto result = employeeService.updateEmployee(1L, validEmployeeDto);

        // Assert
        assertNotNull(result);
    }

    // ==================== GET EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Get Employee - Valid ID - Success")
    void testGetEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act
        EmployeeDto result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get Employee - Non-Existent ID - Throws Exception")
    void testGetEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NoSuchElementException.class,
                () -> employeeService.getEmployee(999L)
        );
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Get Employee - Null ID - Throws Exception")
    void testGetEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(
                Exception.class,
                () -> employeeService.getEmployee(null)
        );
    }

    @Test
    @DisplayName("Get Employee - Negative ID - Throws Exception")
    void testGetEmployee_NegativeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NoSuchElementException.class,
                () -> employeeService.getEmployee(-1L)
        );
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Get All Employees - With Data - Success")
    void testGetAllEmployees_WithData_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    @DisplayName("Get All Employees - Empty Database - Returns Empty Page")
    void testGetAllEmployees_EmptyDatabase_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Get All Employees - Large Page Size - Success")
    void testGetAllEmployees_LargePageSize_Success() {
        // Arrange
        Pageable largePageable = PageRequest.of(0, 1000);
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, largePageable, 1);
        when(employeeRepository.findAllByDeletedFalse(largePageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(largePageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Get All Employees - Page Beyond Data - Returns Empty")
    void testGetAllEmployees_PageBeyondData_ReturnsEmpty() {
        // Arrange
        Pageable highPageable = PageRequest.of(100, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), highPageable, 0);
        when(employeeRepository.findAllByDeletedFalse(highPageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.getAllEmployees(highPageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Delete Employee - Valid ID - Success (Soft Delete)")
    void testDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(argThat(emp -> 
            emp.getDeleted() && "INACTIVE".equals(emp.getStatus())
        ));
    }

    @Test
    @DisplayName("Delete Employee - Non-Existent ID - Throws Exception")
    void testDeleteEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NoSuchElementException.class,
                () -> employeeService.deleteEmployee(999L)
        );
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Delete Employee - Already Deleted - Still Processes")
    void testDeleteEmployee_AlreadyDeleted_StillProcesses() {
        // Arrange
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ==================== SEARCH EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Search Employees By Name - Found - Success")
    void testSearchEmployeesByName_Found_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.searchByName("John", pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.searchEmployeesByName("John", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Search Employees By Name - Not Found - Returns Empty")
    void testSearchEmployeesByName_NotFound_ReturnsEmpty() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.searchByName("NonExistent", pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.searchEmployeesByName("NonExistent", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Search Employees By Name - Empty String - Returns All")
    void testSearchEmployeesByName_EmptyString_ReturnsAll() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.searchByName("", pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.searchEmployeesByName("", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Search Employees By Name - Special Characters - Handled")
    void testSearchEmployeesByName_SpecialCharacters_Handled() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.searchByName("@#$%", pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.searchEmployeesByName("@#$%", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ==================== GET BY DEPARTMENT TESTS ====================

    @Test
    @DisplayName("Get Employees By Department - Found - Success")
    void testGetEmployeesByDepartment_Found_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findByDepartmentAndDeletedFalse("Shipping", pageable))
                .thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getEmployeesByDepartment("Shipping", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Shipping", result.getContent().get(0).getDepartment());
    }

    @Test
    @DisplayName("Get Employees By Department - Not Found - Returns Empty")
    void testGetEmployeesByDepartment_NotFound_ReturnsEmpty() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findByDepartmentAndDeletedFalse("NonExistent", pageable))
                .thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.getEmployeesByDepartment("NonExistent", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Get Employees By Department - Null Department - Handled")
    void testGetEmployeesByDepartment_NullDepartment_Handled() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findByDepartmentAndDeletedFalse(null, pageable))
                .thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.getEmployeesByDepartment(null, pageable);

        // Assert
        assertNotNull(result);
    }

    // ==================== GET BY STATUS TESTS ====================

    @Test
    @DisplayName("Get Employees By Status - ACTIVE - Success")
    void testGetEmployeesByStatus_Active_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findByStatusAndDeletedFalse("ACTIVE", pageable))
                .thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getEmployeesByStatus("ACTIVE", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ACTIVE", result.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("Get Employees By Status - INACTIVE - Success")
    void testGetEmployeesByStatus_Inactive_Success() {
        // Arrange
        validEmployee.setStatus("INACTIVE");
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findByStatusAndDeletedFalse("INACTIVE", pageable))
                .thenReturn(employeePage);

        // Act
        Page<EmployeeDto> result = employeeService.getEmployeesByStatus("INACTIVE", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Get Employees By Status - Invalid Status - Returns Empty")
    void testGetEmployeesByStatus_InvalidStatus_ReturnsEmpty() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findByStatusAndDeletedFalse("INVALID", pageable))
                .thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.getEmployeesByStatus("INVALID", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Get Employees By Status - Null Status - Handled")
    void testGetEmployeesByStatus_NullStatus_Handled() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findByStatusAndDeletedFalse(null, pageable))
                .thenReturn(emptyPage);

        // Act
        Page<EmployeeDto> result = employeeService.getEmployeesByStatus(null, pageable);

        // Assert
        assertNotNull(result);
    }
}