package com.company.wms.employee.service;

import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.model.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.exception.BusinessException;
import com.company.wms.exception.ResourceNotFoundException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Covers normal cases, boundary conditions, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        // Setup test employee entity
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Day Shift");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);

        // Setup test employee DTO
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Day Shift");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    void createEmployee_ValidInput_ReturnsEmployeeDTO() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString()))
            .thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("WORKER", result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void createEmployee_NullInput_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_DuplicateBadgeId_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
            .thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_EmptyName_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    void createEmployee_NullBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    void createEmployee_InvalidRole_ThrowsBusinessException() {
        // Arrange
        testEmployeeDTO.setRole("INVALID_ROLE");
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    void createEmployee_FutureHireDate_ThrowsBusinessException() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    void getEmployee_ValidId_ReturnsEmployeeDTO() {
        // Arrange
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployee(999L);
        });
    }

    @Test
    void getEmployee_NullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(null);
        });
    }

    @Test
    void getEmployee_NegativeId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(-1L);
        });
    }

    @Test
    void getEmployee_DeletedEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployee(1L);
        });
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    void updateEmployee_ValidInput_ReturnsUpdatedEmployeeDTO() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Jane Doe");
        updateDTO.setDepartment("Logistics");
        updateDTO.setRole("SUPERVISOR");

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDTO);
        });
    }

    @Test
    void updateEmployee_NullDTO_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    void updateEmployee_ChangeBadgeIdToDuplicate_ThrowsBusinessException() {
        // Arrange
        Employee anotherEmployee = new Employee();
        anotherEmployee.setId(2L);
        anotherEmployee.setBadgeId("EMP002");

        testEmployeeDTO.setBadgeId("EMP002");

        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP002"))
            .thenReturn(Optional.of(anotherEmployee));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.updateEmployee(1L, testEmployeeDTO);
        });
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    void softDeleteEmployee_ValidId_MarksAsDeleted() {
        // Arrange
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertTrue(testEmployee.isDeleted());
    }

    @Test
    void softDeleteEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.softDeleteEmployee(999L);
        });
    }

    @Test
    void softDeleteEmployee_AlreadyDeleted_ThrowsBusinessException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.softDeleteEmployee(1L);
        });
    }

    // ========== LIST EMPLOYEES TESTS ==========

    @Test
    void listEmployees_NoFilters_ReturnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        
        when(employeeRepository.findAllByDeletedFalse(pageable))
            .thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, new HashMap<>());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    void listEmployees_WithDepartmentFilter_ReturnsFilteredResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, String> filters = new HashMap<>();
        filters.put("department", "Warehouse");
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        
        when(employeeRepository.findByFilters("Warehouse", null, pageable))
            .thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listEmployees_WithStatusFilter_ReturnsFilteredResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, String> filters = new HashMap<>();
        filters.put("status", "ACTIVE");
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        
        when(employeeRepository.findByFilters(null, "ACTIVE", pageable))
            .thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listEmployees_EmptyResult_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        
        when(employeeRepository.findAllByDeletedFalse(pageable))
            .thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, new HashMap<>());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void listEmployees_NullPageable_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.listEmployees(null, new HashMap<>());
        });
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void createEmployee_MaxLengthName_Success() {
        // Arrange
        String maxLengthName = "A".repeat(255);
        testEmployeeDTO.setName(maxLengthName);
        
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString()))
            .thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void createEmployee_NameExceedsMaxLength_ThrowsBusinessException() {
        // Arrange
        String tooLongName = "A".repeat(256);
        testEmployeeDTO.setName(tooLongName);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    void createEmployee_MinimumValidData_Success() {
        // Arrange
        testEmployeeDTO.setShiftGroup(null); // Optional field
        
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString()))
            .thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void createEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testEmployeeDTO.setName("O'Brien-Smith");
        
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString()))
            .thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void listEmployees_LargePageSize_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        
        when(employeeRepository.findAllByDeletedFalse(pageable))
            .thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, new HashMap<>());

        // Assert
        assertNotNull(result);
    }
}