package com.warehouse.employee.management.service;

import com.warehouse.employee.management.domain.Employee;
import com.warehouse.employee.management.dto.EmployeeDTO;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal cases, boundary conditions, and edge cases
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Test Suite")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;
    private List<Employee> employeeList;

    @BeforeEach
    void setUp() {
        // Initialize test employee entity
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setPhoneNumber("+1234567890");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Shipping");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
        testEmployee.setCreatedAt(LocalDateTime.now());
        testEmployee.setUpdatedAt(LocalDateTime.now());

        // Initialize test employee DTO
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setFirstName("John");
        testEmployeeDTO.setLastName("Doe");
        testEmployeeDTO.setEmail("john.doe@warehouse.com");
        testEmployeeDTO.setPhoneNumber("+1234567890");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Shipping");
        testEmployeeDTO.setShiftGroup("Morning");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");

        // Initialize employee list
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setBadgeId("EMP002");
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("jane.smith@warehouse.com");
        employee2.setDeleted(false);

        employeeList = Arrays.asList(testEmployee, employee2);
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    @DisplayName("Test getAllEmployees - Normal Case - Returns Employee List")
    void testGetAllEmployees_ValidRequest_ReturnsEmployeeList() {
        // Arrange
        when(employeeRepository.findByDeletedFalse()).thenReturn(employeeList);

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("EMP001", result.get(0).getBadgeId());
        assertEquals("EMP002", result.get(1).getBadgeId());
        verify(employeeRepository, times(1)).findByDeletedFalse();
    }

    @Test
    @DisplayName("Test getAllEmployees - Empty Database - Returns Empty List")
    void testGetAllEmployees_EmptyDatabase_ReturnsEmptyList() {
        // Arrange
        when(employeeRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findByDeletedFalse();
    }

    @Test
    @DisplayName("Test getAllEmployees - Only Deleted Employees - Returns Empty List")
    void testGetAllEmployees_OnlyDeletedEmployees_ReturnsEmptyList() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findByDeletedFalse();
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    @DisplayName("Test getEmployeeById - Valid ID - Returns Employee")
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(employeeRepository, times(1)).findByIdAndDeletedFalse(1L);
    }

    @Test
    @DisplayName("Test getEmployeeById - Invalid ID - Throws ResourceNotFoundException")
    void testGetEmployeeById_InvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> employeeService.getEmployeeById(999L)
        );
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findByIdAndDeletedFalse(999L);
    }

    @Test
    @DisplayName("Test getEmployeeById - Null ID - Throws IllegalArgumentException")
    void testGetEmployeeById_NullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeeById(null));
    }

    @Test
    @DisplayName("Test getEmployeeById - Negative ID - Throws ResourceNotFoundException")
    void testGetEmployeeById_NegativeId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(-1L));
    }

    @Test
    @DisplayName("Test getEmployeeById - Deleted Employee - Throws ResourceNotFoundException")
    void testGetEmployeeById_DeletedEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(1L));
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test createEmployee - Valid DTO - Returns Created Employee")
    void testCreateEmployee_ValidDTO_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John", result.getFirstName());
        verify(employeeRepository, times(1)).findByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test createEmployee - Duplicate BadgeId - Throws IllegalArgumentException")
    void testCreateEmployee_DuplicateBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.createEmployee(testEmployeeDTO)
        );
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, times(1)).findByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test createEmployee - Null DTO - Throws IllegalArgumentException")
    void testCreateEmployee_NullDTO_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    @DisplayName("Test createEmployee - Empty BadgeId - Throws IllegalArgumentException")
    void testCreateEmployee_EmptyBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDTO));
    }

    @Test
    @DisplayName("Test createEmployee - Null BadgeId - Throws IllegalArgumentException")
    void testCreateEmployee_NullBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDTO));
    }

    @Test
    @DisplayName("Test createEmployee - Invalid Email Format - Throws IllegalArgumentException")
    void testCreateEmployee_InvalidEmailFormat_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDTO));
    }

    @Test
    @DisplayName("Test createEmployee - Empty FirstName - Throws IllegalArgumentException")
    void testCreateEmployee_EmptyFirstName_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setFirstName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDTO));
    }

    @Test
    @DisplayName("Test createEmployee - Empty LastName - Throws IllegalArgumentException")
    void testCreateEmployee_EmptyLastName_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setLastName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployeeDTO));
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test updateEmployee - Valid ID and DTO - Returns Updated Employee")
    void testUpdateEmployee_ValidIdAndDTO_ReturnsUpdatedEmployee() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setBadgeId("EMP001");
        updateDTO.setFirstName("John Updated");
        updateDTO.setLastName("Doe Updated");
        updateDTO.setEmail("john.updated@warehouse.com");
        updateDTO.setPhoneNumber("+9876543210");
        updateDTO.setRole("SUPERVISOR");
        updateDTO.setDepartment("Receiving");
        updateDTO.setShiftGroup("Evening");
        updateDTO.setHireDate(LocalDate.of(2023, 1, 15));
        updateDTO.setStatus("ACTIVE");

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findByIdAndDeletedFalse(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test updateEmployee - Invalid ID - Throws ResourceNotFoundException")
    void testUpdateEmployee_InvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> employeeService.updateEmployee(999L, testEmployeeDTO)
        );
        verify(employeeRepository, times(1)).findByIdAndDeletedFalse(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test updateEmployee - Null DTO - Throws IllegalArgumentException")
    void testUpdateEmployee_NullDTO_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    @DisplayName("Test updateEmployee - Null ID - Throws IllegalArgumentException")
    void testUpdateEmployee_NullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(null, testEmployeeDTO));
    }

    @Test
    @DisplayName("Test updateEmployee - Deleted Employee - Throws ResourceNotFoundException")
    void testUpdateEmployee_DeletedEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> employeeService.updateEmployee(1L, testEmployeeDTO)
        );
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test deleteEmployee - Valid ID - Soft Deletes Employee")
    void testDeleteEmployee_ValidId_SoftDeletesEmployee() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findByIdAndDeletedFalse(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test deleteEmployee - Invalid ID - Throws ResourceNotFoundException")
    void testDeleteEmployee_InvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> employeeService.deleteEmployee(999L)
        );
        verify(employeeRepository, times(1)).findByIdAndDeletedFalse(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test deleteEmployee - Null ID - Throws IllegalArgumentException")
    void testDeleteEmployee_NullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.deleteEmployee(null));
    }

    @Test
    @DisplayName("Test deleteEmployee - Already Deleted Employee - Throws ResourceNotFoundException")
    void testDeleteEmployee_AlreadyDeletedEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> employeeService.deleteEmployee(1L)
        );
    }

    @Test
    @DisplayName("Test deleteEmployee - Negative ID - Throws ResourceNotFoundException")
    void testDeleteEmployee_NegativeId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findByIdAndDeletedFalse(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> employeeService.deleteEmployee(-1L)
        );
    }

    // ==================== BOUNDARY AND EDGE CASE TESTS ====================

    @Test
    @DisplayName("Test createEmployee - Maximum Length Fields - Success")
    void testCreateEmployee_MaximumLengthFields_Success() {
        // Arrange
        String longString = "A".repeat(255);
        testEmployeeDTO.setFirstName(longString);
        testEmployeeDTO.setLastName(longString);
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test createEmployee - Special Characters in Name - Success")
    void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testEmployeeDTO.setFirstName("Jean-Pierre");
        testEmployeeDTO.setLastName("O'Connor");
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test getAllEmployees - Large Dataset - Returns All Employees")
    void testGetAllEmployees_LargeDataset_ReturnsAllEmployees() {
        // Arrange
        List<Employee> largeList = Arrays.asList(new Employee[1000]);
        when(employeeRepository.findByDeletedFalse()).thenReturn(largeList);

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(1000, result.size());
    }

    @Test
    @DisplayName("Test createEmployee - Future Hire Date - Success")
    void testCreateEmployee_FutureHireDate_Success() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(30));
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test createEmployee - Past Hire Date - Success")
    void testCreateEmployee_PastHireDate_Success() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.of(2000, 1, 1));
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
    }
}