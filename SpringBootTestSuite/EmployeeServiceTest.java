package com.warehousemgmt.service;

import com.warehousemgmt.domain.Employee;
import com.warehousemgmt.domain.EmployeeStatus;
import com.warehousemgmt.domain.Role;
import com.warehousemgmt.dto.EmployeeRequestDTO;
import com.warehousemgmt.dto.EmployeeResponseDTO;
import com.warehousemgmt.repository.EmployeeRepository;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Covers all CRUD operations, validation, boundary conditions, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;
    private EmployeeRequestDTO validRequestDTO;

    @BeforeEach
    public void setUp() {
        // Arrange: Set up valid test data
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setRole(Role.WORKER);
        validEmployee.setDepartment("Warehouse");
        validEmployee.setShiftGroup("Morning");
        validEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        validEmployee.setStatus(EmployeeStatus.ACTIVE);
        validEmployee.setDeleted(false);

        validRequestDTO = new EmployeeRequestDTO();
        validRequestDTO.setName("John Doe");
        validRequestDTO.setBadgeId("EMP001");
        validRequestDTO.setRole(Role.WORKER);
        validRequestDTO.setDepartment("Warehouse");
        validRequestDTO.setShiftGroup("Morning");
        validRequestDTO.setHireDate(LocalDate.of(2023, 1, 15));
        validRequestDTO.setStatus(EmployeeStatus.ACTIVE);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    public void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequestDTO);
        });
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullName_ThrowsException() {
        // Arrange
        validRequestDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequestDTO);
        });
    }

    @Test
    public void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        validRequestDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequestDTO);
        });
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        validRequestDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequestDTO);
        });
    }

    @Test
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        validRequestDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequestDTO);
        });
    }

    @Test
    public void testCreateEmployee_NullRole_ThrowsException() {
        // Arrange
        validRequestDTO.setRole(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequestDTO);
        });
    }

    @Test
    public void testCreateEmployee_NullDepartment_ThrowsException() {
        // Arrange
        validRequestDTO.setDepartment(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequestDTO);
        });
    }

    @Test
    public void testCreateEmployee_NullHireDate_ThrowsException() {
        // Arrange
        validRequestDTO.setHireDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequestDTO);
        });
    }

    @Test
    public void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        validRequestDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequestDTO);
        });
    }

    @Test
    public void testCreateEmployee_MaxLengthName_Success() {
        // Arrange
        String maxLengthName = "A".repeat(255);
        validRequestDTO.setName(maxLengthName);
        validEmployee.setName(maxLengthName);
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(maxLengthName, result.getName());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    public void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        validRequestDTO.setName("Jane Doe");

        // Act
        EmployeeResponseDTO result = employeeService.updateEmployee(1L, validRequestDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, validRequestDTO);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(null, validRequestDTO);
        });
    }

    @Test
    public void testUpdateEmployee_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(-1L, validRequestDTO);
        });
    }

    @Test
    public void testUpdateEmployee_DeletedEmployee_ThrowsException() {
        // Arrange
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.updateEmployee(1L, validRequestDTO);
        });
    }

    @Test
    public void testUpdateEmployee_PartialUpdate_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        EmployeeRequestDTO partialDTO = new EmployeeRequestDTO();
        partialDTO.setDepartment("Logistics");

        // Act
        EmployeeResponseDTO result = employeeService.updateEmployee(1L, partialDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // ========== SOFT DELETE TESTS ==========

    @Test
    public void testSoftDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testSoftDeleteEmployee_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.softDeleteEmployee(999L);
        });
    }

    @Test
    public void testSoftDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.softDeleteEmployee(null);
        });
    }

    @Test
    public void testSoftDeleteEmployee_AlreadyDeleted_ThrowsException() {
        // Arrange
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            employeeService.softDeleteEmployee(1L);
        });
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    public void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act
        EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployeeById_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
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
    public void testGetEmployeeById_DeletedEmployee_ThrowsException() {
        // Arrange
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployeeById(1L);
        });
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    public void testGetAllEmployees_NoFilters_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 20);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable, new HashMap<>());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    public void testGetAllEmployees_WithDepartmentFilter_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 20);
        Map<String, String> filters = new HashMap<>();
        filters.put("department", "Warehouse");
        when(employeeRepository.findByDepartmentAndDeletedFalse("Warehouse", pageable)).thenReturn(page);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllEmployees_EmptyResult_Success() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 20);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable, new HashMap<>());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testGetAllEmployees_NullPageable_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getAllEmployees(null, new HashMap<>());
        });
    }

    @Test
    public void testGetAllEmployees_LargePage_Success() {
        // Arrange
        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Employee emp = new Employee();
            emp.setId((long) i);
            emp.setName("Employee " + i);
            emp.setBadgeId("EMP" + String.format("%03d", i));
            employees.add(emp);
        }
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 100);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable, new HashMap<>());

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getTotalElements());
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    public void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        validRequestDTO.setName("O'Brien-Smith");
        validEmployee.setName("O'Brien-Smith");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("O'Brien-Smith", result.getName());
    }

    @Test
    public void testCreateEmployee_UnicodeCharactersInName_Success() {
        // Arrange
        validRequestDTO.setName("JosÃ© GarcÃ­a");
        validEmployee.setName("JosÃ© GarcÃ­a");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("JosÃ© GarcÃ­a", result.getName());
    }

    @Test
    public void testCreateEmployee_MinimumValidDate_Success() {
        // Arrange
        validRequestDTO.setHireDate(LocalDate.of(1900, 1, 1));
        validEmployee.setHireDate(LocalDate.of(1900, 1, 1));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.of(1900, 1, 1), result.getHireDate());
    }

    @Test
    public void testCreateEmployee_TodayHireDate_Success() {
        // Arrange
        validRequestDTO.setHireDate(LocalDate.now());
        validEmployee.setHireDate(LocalDate.now());
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(validRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getHireDate());
    }
}