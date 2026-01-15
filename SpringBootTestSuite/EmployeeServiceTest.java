package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.dto.EmployeeDTO;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.mapper.EmployeeMapper;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.exception.NotFoundException;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal cases, boundary conditions, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        // Arrange - Setup test data
        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        testEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeMapper.toEntity(any(EmployeeDTO.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullInput_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
    }

    @Test
    void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    void testCreateEmployee_InvalidRole_ThrowsException() {
        // Arrange
        testEmployeeDTO.setRole("INVALID_ROLE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEmployeeById_NonExistentId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    void testGetEmployeeById_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    void testGetEmployeeById_ZeroId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(0L);
        });
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    void testGetAllEmployees_ValidPageable_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    void testGetAllEmployees_WithDepartmentFilter_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable, "Warehouse", null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAllEmployees_WithStatusFilter_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable, null, "ACTIVE", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAllEmployees_EmptyResult_Success() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testGetAllEmployees_NullPageable_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getAllEmployees(null, null, null, null);
        });
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        EmployeeDTO updateDTO = EmployeeDTO.builder()
                .name("Jane Doe")
                .department("Logistics")
                .build();

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NonExistentId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDTO);
        });
    }

    @Test
    void testUpdateEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(null, testEmployeeDTO);
        });
    }

    @Test
    void testUpdateEmployee_NullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    void testUpdateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeIdAndIdNot(anyString(), anyLong())).thenReturn(true);

        EmployeeDTO updateDTO = EmployeeDTO.builder()
                .badgeId("EMP002")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, updateDTO);
        });
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    void testDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertEquals("INACTIVE", testEmployee.getStatus());
    }

    @Test
    void testDeleteEmployee_NonExistentId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    void testDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    @Test
    void testDeleteEmployee_AlreadyInactive_Success() {
        // Arrange
        testEmployee.setStatus("INACTIVE");
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertEquals("INACTIVE", testEmployee.getStatus());
    }

    // ========== GET EMPLOYEE BY BADGE ID TESTS ==========

    @Test
    void testGetEmployeeByBadgeId_ValidBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    void testGetEmployeeByBadgeId_NonExistentBadgeId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
    }

    @Test
    void testGetEmployeeByBadgeId_NullBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByBadgeId(null);
        });
    }

    @Test
    void testGetEmployeeByBadgeId_EmptyBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByBadgeId("");
        });
    }

    @Test
    void testGetEmployeeByBadgeId_WhitespaceBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByBadgeId("   ");
        });
    }
}