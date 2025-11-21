package com.warehouse.ems.service;

import com.warehouse.ems.dto.EmployeeDTO;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.entity.Role;
import com.warehouse.ems.exception.DuplicateBadgeIdException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.repository.RoleRepository;
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
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal operations, boundary conditions, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;
    private Role testRole;

    @BeforeEach
    public void setUp() {
        // Arrange - Set up test data
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("WORKER");

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setRole(testRole);
        testEmployee.setDepartment("Logistics");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);

        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setFirstName("John");
        testEmployeeDTO.setLastName("Doe");
        testEmployeeDTO.setEmail("john.doe@warehouse.com");
        testEmployeeDTO.setRoleId(1L);
        testEmployeeDTO.setDepartment("Logistics");
        testEmployeeDTO.setShiftGroup("A");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    public void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullEmployeeDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(DuplicateBadgeIdException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_InvalidEmail_ThrowsException() {
        // Arrange
        testEmployeeDTO.setEmail("invalid-email");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_EmptyFirstName_ThrowsException() {
        // Arrange
        testEmployeeDTO.setFirstName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    @Test
    public void testCreateEmployee_InvalidRoleId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.createEmployee(testEmployeeDTO);
        });
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    public void testGetEmployeeById_ExistingId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployeeById_NonExistingId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
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
    public void testGetEmployeeById_ZeroId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(0L);
        });
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    public void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        testEmployeeDTO.setFirstName("Jane");

        // Act
        Employee result = employeeService.updateEmployee(1L, testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_NonExistingId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDTO);
        });
    }

    @Test
    public void testUpdateEmployee_NullEmployeeDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    public void testUpdateEmployee_ChangeBadgeIdToDuplicate_ThrowsException() {
        // Arrange
        Employee anotherEmployee = new Employee();
        anotherEmployee.setId(2L);
        anotherEmployee.setBadgeId("EMP002");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByBadgeId("EMP002")).thenReturn(Optional.of(anotherEmployee));

        testEmployeeDTO.setBadgeId("EMP002");

        // Act & Assert
        assertThrows(DuplicateBadgeIdException.class, () -> {
            employeeService.updateEmployee(1L, testEmployeeDTO);
        });
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    public void testDeleteEmployee_ExistingId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).delete(any(Employee.class));

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).delete(testEmployee);
    }

    @Test
    public void testDeleteEmployee_NonExistingId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    public void testDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    @Test
    public void testSoftDeleteEmployee_ExistingId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.isDeleted());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    public void testGetAllEmployees_WithPagination_ReturnsPagedResults() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
    }

    @Test
    public void testGetAllEmployees_EmptyDatabase_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ========== FIND BY DEPARTMENT TESTS ==========

    @Test
    public void testFindByDepartment_ValidDepartment_ReturnsEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByDepartment("Logistics")).thenReturn(employees);

        // Act
        List<Employee> result = employeeService.findByDepartment("Logistics");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Logistics", result.get(0).getDepartment());
    }

    @Test
    public void testFindByDepartment_NonExistingDepartment_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findByDepartment("NonExistent")).thenReturn(Arrays.asList());

        // Act
        List<Employee> result = employeeService.findByDepartment("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByDepartment_NullDepartment_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findByDepartment(null);
        });
    }

    @Test
    public void testFindByDepartment_EmptyDepartment_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findByDepartment("");
        });
    }

    // ========== FIND BY BADGE ID TESTS ==========

    @Test
    public void testFindByBadgeId_ValidBadgeId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        Employee result = employeeService.findByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    public void testFindByBadgeId_NonExistingBadgeId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.findByBadgeId("INVALID");
        });
    }

    @Test
    public void testFindByBadgeId_NullBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findByBadgeId(null);
        });
    }

    @Test
    public void testFindByBadgeId_EmptyBadgeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.findByBadgeId("");
        });
    }

    // ========== FIND BY STATUS TESTS ==========

    @Test
    public void testFindByStatus_ActiveEmployees_ReturnsActiveEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByStatus("ACTIVE")).thenReturn(employees);

        // Act
        List<Employee> result = employeeService.findByStatus("ACTIVE");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    public void testFindByStatus_InactiveEmployees_ReturnsInactiveEmployees() {
        // Arrange
        testEmployee.setStatus("INACTIVE");
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByStatus("INACTIVE")).thenReturn(employees);

        // Act
        List<Employee> result = employeeService.findByStatus("INACTIVE");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("INACTIVE", result.get(0).getStatus());
    }
}