package com.company.wems.employee.service;

import com.company.wems.employee.dto.EmployeeDTO;
import com.company.wems.employee.entity.Employee;
import com.company.wems.employee.repository.EmployeeRepository;
import com.company.wems.common.exception.DuplicateResourceException;
import com.company.wems.common.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService
 * Tests cover normal operations, boundary conditions, and edge cases
 */
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDTO validEmployeeDTO;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup valid test data
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setBadgeId("EMP001");
        validEmployeeDTO.setFirstName("John");
        validEmployeeDTO.setLastName("Doe");
        validEmployeeDTO.setEmail("john.doe@company.com");
        validEmployeeDTO.setPhone("+1234567890");
        validEmployeeDTO.setRole("WORKER");
        validEmployeeDTO.setDepartment("Warehouse");
        validEmployeeDTO.setHireDate(LocalDate.now());
        
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setEmail("john.doe@company.com");
        validEmployee.setDeleted(false);
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Create Employee - Valid Input - Should Return Created Employee")
    void testCreateEmployee_WithValidInput_ShouldReturnCreatedEmployee() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(validEmployeeDTO.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(validEmployeeDTO)).thenReturn(validEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toDTO(validEmployee)).thenReturn(validEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(validEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(validEmployeeDTO.getBadgeId(), result.getBadgeId());
        assertEquals(validEmployeeDTO.getFirstName(), result.getFirstName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Duplicate Badge ID - Should Throw DuplicateResourceException")
    void testCreateEmployee_WithDuplicateBadgeId_ShouldThrowException() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(validEmployeeDTO.getBadgeId())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Null Badge ID - Should Throw Exception")
    void testCreateEmployee_WithNullBadgeId_ShouldThrowException() {
        // Arrange
        validEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Create Employee - Empty Badge ID - Should Throw Exception")
    void testCreateEmployee_WithEmptyBadgeId_ShouldThrowException() {
        // Arrange
        validEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Create Employee - Null First Name - Should Throw Exception")
    void testCreateEmployee_WithNullFirstName_ShouldThrowException() {
        // Arrange
        validEmployeeDTO.setFirstName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Create Employee - Invalid Email Format - Should Throw Exception")
    void testCreateEmployee_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        validEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Create Employee - Future Hire Date - Should Throw Exception")
    void testCreateEmployee_WithFutureHireDate_ShouldThrowException() {
        // Arrange
        validEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.createEmployee(validEmployeeDTO);
        });
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Update Employee - Valid Input - Should Return Updated Employee")
    void testUpdateEmployee_WithValidInput_ShouldReturnUpdatedEmployee() {
        // Arrange
        Long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(validEmployee));
        doNothing().when(employeeMapper).updateEntityFromDTO(validEmployeeDTO, validEmployee);
        when(employeeRepository.save(validEmployee)).thenReturn(validEmployee);
        when(employeeMapper.toDTO(validEmployee)).thenReturn(validEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(employeeId, validEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    @Test
    @DisplayName("Update Employee - Non-Existent ID - Should Throw ResourceNotFoundException")
    void testUpdateEmployee_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Long employeeId = 999L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(employeeId, validEmployeeDTO);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Deleted Employee - Should Throw ResourceNotFoundException")
    void testUpdateEmployee_WithDeletedEmployee_ShouldThrowException() {
        // Arrange
        Long employeeId = 1L;
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(employeeId, validEmployeeDTO);
        });
    }

    @Test
    @DisplayName("Update Employee - Null ID - Should Throw Exception")
    void testUpdateEmployee_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.updateEmployee(null, validEmployeeDTO);
        });
    }

    // ==================== GET EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Get All Employees - Valid Pageable - Should Return Page of Employees")
    void testGetAllEmployees_WithValidPageable_ShouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(validEmployee));
        when(employeeRepository.findByDeletedFalse(pageable)).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(validEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findByDeletedFalse(pageable);
    }

    @Test
    @DisplayName("Get All Employees - Empty Result - Should Return Empty Page")
    void testGetAllEmployees_WithNoEmployees_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeRepository.findByDeletedFalse(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Get All Employees - Null Pageable - Should Throw Exception")
    void testGetAllEmployees_WithNullPageable_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.getAllEmployees(null);
        });
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Delete Employee - Valid ID - Should Soft Delete Employee")
    void testDeleteEmployee_WithValidId_ShouldSoftDeleteEmployee() {
        // Arrange
        Long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(validEmployee)).thenReturn(validEmployee);

        // Act
        employeeService.deleteEmployee(employeeId);

        // Assert
        assertTrue(validEmployee.getDeleted());
        assertEquals(Employee.EmployeeStatus.TERMINATED, validEmployee.getStatus());
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    @Test
    @DisplayName("Delete Employee - Non-Existent ID - Should Throw ResourceNotFoundException")
    void testDeleteEmployee_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Long employeeId = 999L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(employeeId);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Delete Employee - Already Deleted - Should Throw ResourceNotFoundException")
    void testDeleteEmployee_WithAlreadyDeletedEmployee_ShouldThrowException() {
        // Arrange
        Long employeeId = 1L;
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(employeeId);
        });
    }

    @Test
    @DisplayName("Delete Employee - Null ID - Should Throw Exception")
    void testDeleteEmployee_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    @Test
    @DisplayName("Delete Employee - Negative ID - Should Throw Exception")
    void testDeleteEmployee_WithNegativeId_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.deleteEmployee(-1L);
        });
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Create Employee - Maximum Length Badge ID - Should Create Successfully")
    void testCreateEmployee_WithMaxLengthBadgeId_ShouldCreateSuccessfully() {
        // Arrange
        String maxLengthBadgeId = "A".repeat(50);
        validEmployeeDTO.setBadgeId(maxLengthBadgeId);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(maxLengthBadgeId)).thenReturn(false);
        when(employeeMapper.toEntity(validEmployeeDTO)).thenReturn(validEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toDTO(validEmployee)).thenReturn(validEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(validEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Minimum Valid Data - Should Create Successfully")
    void testCreateEmployee_WithMinimumValidData_ShouldCreateSuccessfully() {
        // Arrange
        validEmployeeDTO.setPhone(null);
        validEmployeeDTO.setDepartment(null);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(validEmployeeDTO.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(validEmployeeDTO)).thenReturn(validEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toDTO(validEmployee)).thenReturn(validEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(validEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Special Characters in Name - Should Create Successfully")
    void testCreateEmployee_WithSpecialCharactersInName_ShouldCreateSuccessfully() {
        // Arrange
        validEmployeeDTO.setFirstName("Jean-Pierre");
        validEmployeeDTO.setLastName("O'Connor");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(validEmployeeDTO.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(validEmployeeDTO)).thenReturn(validEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toDTO(validEmployee)).thenReturn(validEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(validEmployeeDTO);

        // Assert
        assertNotNull(result);
    }
}