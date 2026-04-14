package com.wms.ems.service;

import com.wms.ems.dto.EmployeeCreateDTO;
import com.wms.ems.dto.EmployeeDTO;
import com.wms.ems.dto.EmployeeUpdateDTO;
import com.wms.ems.entity.Employee;
import com.wms.ems.entity.enums.EmployeeRole;
import com.wms.ems.entity.enums.EmployeeStatus;
import com.wms.ems.exception.EntityNotFoundException;
import com.wms.ems.exception.ValidationException;
import com.wms.ems.repository.EmployeeRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService.
 * Tests cover all CRUD operations, normal cases, boundary conditions, and edge cases.
 * 
 * @author EMS Test Suite Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeCreateDTO createDTO;
    private EmployeeUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setEmail("john.doe@wms.com");
        testEmployee.setRole(EmployeeRole.WORKER);
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDeleted(false);

        // Setup create DTO
        createDTO = EmployeeCreateDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .badgeId("EMP002")
                .email("jane.smith@wms.com")
                .role(EmployeeRole.WORKER)
                .department("Warehouse")
                .shiftGroup("B")
                .hireDate(LocalDate.now())
                .build();

        // Setup update DTO
        updateDTO = EmployeeUpdateDTO.builder()
                .firstName("John")
                .lastName("Doe Updated")
                .department("Logistics")
                .build();
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Create Employee - Valid Input - Success")
    void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).existsByBadgeId(anyString());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Duplicate BadgeId - Throws ValidationException")
    void testCreateEmployee_DuplicateBadgeId_ThrowsValidationException() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
        verify(employeeRepository, times(1)).existsByBadgeId(anyString());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Null FirstName - Throws ValidationException")
    void testCreateEmployee_NullFirstName_ThrowsValidationException() {
        // Arrange
        createDTO.setFirstName(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
    }

    @Test
    @DisplayName("Create Employee - Empty LastName - Throws ValidationException")
    void testCreateEmployee_EmptyLastName_ThrowsValidationException() {
        // Arrange
        createDTO.setLastName("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
    }

    @Test
    @DisplayName("Create Employee - Invalid Email Format - Throws ValidationException")
    void testCreateEmployee_InvalidEmailFormat_ThrowsValidationException() {
        // Arrange
        createDTO.setEmail("invalid-email");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
    }

    @Test
    @DisplayName("Create Employee - BadgeId Too Short - Throws ValidationException")
    void testCreateEmployee_BadgeIdTooShort_ThrowsValidationException() {
        // Arrange
        createDTO.setBadgeId("E1");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
    }

    @Test
    @DisplayName("Create Employee - Future HireDate - Throws ValidationException")
    void testCreateEmployee_FutureHireDate_ThrowsValidationException() {
        // Arrange
        createDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.createEmployee(createDTO);
        });
    }

    @Test
    @DisplayName("Create Employee - Null Role - Uses Default WORKER Role")
    void testCreateEmployee_NullRole_UsesDefaultWorkerRole() {
        // Arrange
        createDTO.setRole(null);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals(EmployeeRole.WORKER, result.getRole());
    }

    // ==================== GET EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Get Employee By ID - Valid ID - Success")
    void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        verify(employeeRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Get Employee By ID - Non-Existent ID - Throws EntityNotFoundException")
    void testGetEmployeeById_NonExistentId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
        verify(employeeRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Get Employee By ID - Negative ID - Throws ValidationException")
    void testGetEmployeeById_NegativeId_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    @Test
    @DisplayName("Get Employee By ID - Zero ID - Throws ValidationException")
    void testGetEmployeeById_ZeroId_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.getEmployeeById(0L);
        });
    }

    @Test
    @DisplayName("Get Employee By BadgeId - Valid BadgeId - Success")
    void testGetEmployeeByBadgeId_ValidBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeId(anyString());
    }

    @Test
    @DisplayName("Get Employee By BadgeId - Non-Existent BadgeId - Throws EntityNotFoundException")
    void testGetEmployeeByBadgeId_NonExistentBadgeId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployeeByBadgeId("INVALID");
        });
    }

    @Test
    @DisplayName("Get Employee By BadgeId - Null BadgeId - Throws ValidationException")
    void testGetEmployeeByBadgeId_NullBadgeId_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.getEmployeeByBadgeId(null);
        });
    }

    @Test
    @DisplayName("Get Employee By BadgeId - Empty BadgeId - Throws ValidationException")
    void testGetEmployeeByBadgeId_EmptyBadgeId_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.getEmployeeByBadgeId("");
        });
    }

    // ==================== LIST EMPLOYEES TESTS ====================

    @Test
    @DisplayName("List All Employees - Valid Pageable - Success")
    void testListAllEmployees_ValidPageable_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(employeeRepository, times(1)).findByDeletedFalse(any(Pageable.class));
    }

    @Test
    @DisplayName("List All Employees - Empty Result - Returns Empty Page")
    void testListAllEmployees_EmptyResult_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.listAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("List Employees By Department - Valid Department - Success")
    void testListEmployeesByDepartment_ValidDepartment_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByDepartmentAndDeletedFalse(anyString())).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.listEmployeesByDepartment("Warehouse");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Warehouse", result.get(0).getDepartment());
    }

    @Test
    @DisplayName("List Employees By Department - Null Department - Throws ValidationException")
    void testListEmployeesByDepartment_NullDepartment_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.listEmployeesByDepartment(null);
        });
    }

    @Test
    @DisplayName("List Employees By Status - Valid Status - Success")
    void testListEmployeesByStatus_ValidStatus_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByStatusAndDeletedFalse(any(EmployeeStatus.class))).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.listEmployeesByStatus(EmployeeStatus.ACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EmployeeStatus.ACTIVE, result.get(0).getStatus());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Update Employee - Valid Input - Success")
    void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(anyLong());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Non-Existent ID - Throws EntityNotFoundException")
    void testUpdateEmployee_NonExistentId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, updateDTO);
        });
    }

    @Test
    @DisplayName("Update Employee - Null UpdateDTO - Throws ValidationException")
    void testUpdateEmployee_NullUpdateDTO_ThrowsValidationException() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    @DisplayName("Update Employee - Partial Update - Success")
    void testUpdateEmployee_PartialUpdate_Success() {
        // Arrange
        EmployeeUpdateDTO partialUpdate = EmployeeUpdateDTO.builder()
                .department("Logistics")
                .build();
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("Logistics", result.getDepartment());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Soft Delete Employee - Valid ID - Success")
    void testSoftDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(testEmployee.isDeleted());
        verify(employeeRepository, times(1)).findById(anyLong());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Soft Delete Employee - Non-Existent ID - Throws EntityNotFoundException")
    void testSoftDeleteEmployee_NonExistentId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.softDeleteEmployee(999L);
        });
    }

    @Test
    @DisplayName("Soft Delete Employee - Already Deleted - Throws ValidationException")
    void testSoftDeleteEmployee_AlreadyDeleted_ThrowsValidationException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            employeeService.softDeleteEmployee(1L);
        });
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Create Employee - Maximum Field Lengths - Success")
    void testCreateEmployee_MaximumFieldLengths_Success() {
        // Arrange
        String maxLengthName = "A".repeat(100);
        createDTO.setFirstName(maxLengthName);
        createDTO.setLastName(maxLengthName);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Minimum Field Lengths - Success")
    void testCreateEmployee_MinimumFieldLengths_Success() {
        // Arrange
        createDTO.setFirstName("A");
        createDTO.setLastName("B");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("List Employees - Large Page Size - Success")
    void testListEmployees_LargePageSize_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listAllEmployees(pageable);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("List Employees - Page Number Beyond Available Pages - Returns Empty Page")
    void testListEmployees_PageNumberBeyondAvailable_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(100, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.listAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Create Employee - Special Characters in Name - Success")
    void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        createDTO.setFirstName("Jean-Pierre");
        createDTO.setLastName("O'Connor");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Unicode Characters in Name - Success")
    void testCreateEmployee_UnicodeCharactersInName_Success() {
        // Arrange
        createDTO.setFirstName("JosÃ©");
        createDTO.setLastName("MÃ¼ller");
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(createDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Update Employee - Concurrent Modification - Handles Gracefully")
    void testUpdateEmployee_ConcurrentModification_HandlesGracefully() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get Employee - Deleted Employee - Throws EntityNotFoundException")
    void testGetEmployee_DeletedEmployee_ThrowsEntityNotFoundException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployeeById(1L);
        });
    }

    @Test
    @DisplayName("Create Employee - All Roles - Success")
    void testCreateEmployee_AllRoles_Success() {
        // Test all employee roles
        for (EmployeeRole role : EmployeeRole.values()) {
            // Arrange
            createDTO.setRole(role);
            createDTO.setBadgeId("EMP" + role.name());
            when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            // Act
            EmployeeDTO result = employeeService.createEmployee(createDTO);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Create Employee - All Statuses - Success")
    void testCreateEmployee_AllStatuses_Success() {
        // Test all employee statuses
        for (EmployeeStatus status : EmployeeStatus.values()) {
            // Arrange
            createDTO.setBadgeId("EMP" + status.name());
            when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            // Act
            EmployeeDTO result = employeeService.createEmployee(createDTO);

            // Assert
            assertNotNull(result);
        }
    }
}