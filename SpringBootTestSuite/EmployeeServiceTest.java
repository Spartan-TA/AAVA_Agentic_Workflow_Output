package com.warehouse.management.employee;

import com.warehouse.management.common.exceptions.BusinessException;
import com.warehouse.management.common.exceptions.ResourceNotFoundException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal operations, boundary conditions, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee testEmployee;
    private EmployeeRequest testRequest;
    private EmployeeResponse testResponse;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        // Setup test employee entity
        testEmployee = new Employee();
        testEmployee.setId(testId);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setRole(EmployeeRole.WORKER);
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDepartment("Warehouse");
        testEmployee.setHireDate(LocalDate.now());
        
        // Setup test request DTO
        testRequest = new EmployeeRequest();
        testRequest.setBadgeId("EMP001");
        testRequest.setFirstName("John");
        testRequest.setLastName("Doe");
        testRequest.setEmail("john.doe@warehouse.com");
        testRequest.setRole(EmployeeRole.WORKER);
        testRequest.setDepartment("Warehouse");
        testRequest.setHireDate(LocalDate.now());
        
        // Setup test response DTO
        testResponse = new EmployeeResponse();
        testResponse.setId(testId);
        testResponse.setBadgeId("EMP001");
        testResponse.setFirstName("John");
        testResponse.setLastName("Doe");
        testResponse.setEmail("john.doe@warehouse.com");
        testResponse.setRole(EmployeeRole.WORKER);
        testResponse.setStatus(EmployeeStatus.ACTIVE);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.existsByBadgeId(testRequest.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(testRequest)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.createEmployee(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testResponse.getBadgeId(), result.getBadgeId());
        assertEquals(testResponse.getFirstName(), result.getFirstName());
        assertEquals(testResponse.getEmail(), result.getEmail());
        verify(employeeRepository, times(1)).existsByBadgeId(testRequest.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsBusinessException() {
        // Arrange
        when(employeeRepository.existsByBadgeId(testRequest.getBadgeId())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testRequest);
        });
        
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, times(1)).existsByBadgeId(testRequest.getBadgeId());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.createEmployee(null);
        });
        
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsBusinessException() {
        // Arrange
        testRequest.setBadgeId("");

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testRequest);
        });
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsBusinessException() {
        // Arrange
        testRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testRequest);
        });
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    void testGetEmployeeById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(testId)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toResponse(testEmployee)).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.getEmployeeById(testId);

        // Assert
        assertNotNull(result);
        assertEquals(testResponse.getId(), result.getId());
        assertEquals(testResponse.getBadgeId(), result.getBadgeId());
        verify(employeeRepository, times(1)).findById(testId);
    }

    @Test
    void testGetEmployeeById_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(nonExistentId);
        });
        
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        // Arrange
        EmployeeRequest updateRequest = new EmployeeRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@warehouse.com");
        updateRequest.setDepartment("Logistics");
        
        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(testId);
        updatedEmployee.setBadgeId("EMP001");
        updatedEmployee.setFirstName("Jane");
        updatedEmployee.setLastName("Smith");
        updatedEmployee.setEmail("jane.smith@warehouse.com");
        
        when(employeeRepository.findById(testId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);
        when(employeeMapper.toResponse(updatedEmployee)).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.updateEmployee(testId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findById(testId);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(nonExistentId, testRequest);
        });
        
        verify(employeeRepository, times(1)).findById(nonExistentId);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.updateEmployee(testId, null);
        });
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    void testDeleteEmployee_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(testId)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).delete(testEmployee);

        // Act
        employeeService.deleteEmployee(testId);

        // Assert
        verify(employeeRepository, times(1)).findById(testId);
        verify(employeeRepository, times(1)).delete(testEmployee);
    }

    @Test
    void testDeleteEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(nonExistentId);
        });
        
        verify(employeeRepository, times(1)).findById(nonExistentId);
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void testDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    void testGetAllEmployees_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetAllEmployees_EmptyResult_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponse> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    // ========== SEARCH EMPLOYEES TESTS ==========

    @Test
    void testSearchEmployeesByDepartment_ValidDepartment_Success() {
        // Arrange
        String department = "Warehouse";
        List<Employee> employees = Arrays.asList(testEmployee);
        
        when(employeeRepository.findByDepartment(department)).thenReturn(employees);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        List<EmployeeResponse> result = employeeService.searchByDepartment(department);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findByDepartment(department);
    }

    @Test
    void testSearchEmployeesByDepartment_EmptyDepartment_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.searchByDepartment("");
        });
    }

    @Test
    void testSearchEmployeesByDepartment_NullDepartment_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.searchByDepartment(null);
        });
    }

    @Test
    void testSearchEmployeesByStatus_ValidStatus_Success() {
        // Arrange
        EmployeeStatus status = EmployeeStatus.ACTIVE;
        List<Employee> employees = Arrays.asList(testEmployee);
        
        when(employeeRepository.findByStatus(status)).thenReturn(employees);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        List<EmployeeResponse> result = employeeService.searchByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findByStatus(status);
    }

    @Test
    void testSearchEmployeesByStatus_NullStatus_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.searchByStatus(null);
        });
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testCreateEmployee_MaxLengthFields_Success() {
        // Arrange
        String maxLengthString = "A".repeat(255);
        testRequest.setFirstName(maxLengthString);
        testRequest.setLastName(maxLengthString);
        testRequest.setEmail(maxLengthString + "@test.com");
        
        when(employeeRepository.existsByBadgeId(testRequest.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(testRequest)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.createEmployee(testRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testRequest.setFirstName("O'Brien");
        testRequest.setLastName("MÃ¼ller-Schmidt");
        
        when(employeeRepository.existsByBadgeId(testRequest.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(testRequest)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.createEmployee(testRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_FutureHireDate_ThrowsBusinessException() {
        // Arrange
        testRequest.setHireDate(LocalDate.now().plusDays(30));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testRequest);
        });
    }

    @Test
    void testCreateEmployee_InvalidEmailFormat_ThrowsBusinessException() {
        // Arrange
        testRequest.setEmail("invalid-email");

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(testRequest);
        });
    }
}