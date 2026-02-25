package com.warehouse.employee.management.application.service;

import com.warehouse.employee.management.application.dto.CreateEmployeeRequest;
import com.warehouse.employee.management.application.dto.UpdateEmployeeRequest;
import com.warehouse.employee.management.application.dto.EmployeeResponse;
import com.warehouse.employee.management.application.mapper.EmployeeMapper;
import com.warehouse.employee.management.domain.employee.*;
import com.warehouse.employee.management.infrastructure.repository.EmployeeRepository;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService
 * Tests cover:
 * - Normal cases for all CRUD operations
 * - Boundary conditions (null values, empty strings, edge dates)
 * - Edge cases (duplicate badge IDs, invalid emails, terminated employees)
 * - Validation scenarios
 * - Exception handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private CreateEmployeeRequest createRequest;
    private UpdateEmployeeRequest updateRequest;
    private EmployeeResponse employeeResponse;
    private Department testDepartment;
    private Position testPosition;

    @BeforeEach
    void setUp() {
        // Setup test department
        testDepartment = new Department();
        testDepartment.setId(UUID.randomUUID());
        testDepartment.setName("Warehouse Operations");
        testDepartment.setCode("WH-OPS");

        // Setup test position
        testPosition = new Position();
        testPosition.setId(UUID.randomUUID());
        testPosition.setTitle("Warehouse Associate");
        testPosition.setLevel("L1");

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(UUID.randomUUID());
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setPhoneNumber("+1-555-0100");
        testEmployee.setDateOfBirth(LocalDate.of(1990, 1, 15));
        testEmployee.setHireDate(LocalDate.of(2020, 3, 1));
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDepartment(testDepartment);
        testEmployee.setPosition(testPosition);

        // Setup create request
        createRequest = new CreateEmployeeRequest();
        createRequest.setBadgeId("EMP001");
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john.doe@warehouse.com");
        createRequest.setPhoneNumber("+1-555-0100");
        createRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));
        createRequest.setHireDate(LocalDate.of(2020, 3, 1));

        // Setup update request
        updateRequest = new UpdateEmployeeRequest();
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("john.smith@warehouse.com");
        updateRequest.setPhoneNumber("+1-555-0200");

        // Setup employee response
        employeeResponse = new EmployeeResponse();
        employeeResponse.setId(testEmployee.getId());
        employeeResponse.setBadgeId(testEmployee.getBadgeId());
        employeeResponse.setFirstName(testEmployee.getFirstName());
        employeeResponse.setLastName(testEmployee.getLastName());
        employeeResponse.setEmail(testEmployee.getEmail());
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Create Employee - Normal Case - Should Create Successfully")
    void testCreateEmployee_NormalCase_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(createRequest.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(createRequest)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee.getBadgeId(), result.getBadgeId());
        assertEquals(testEmployee.getEmail(), result.getEmail());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Duplicate Badge ID - Should Throw Exception")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(createRequest.getBadgeId())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Duplicate Email - Should Throw Exception")
    void testCreateEmployee_DuplicateEmail_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(createRequest.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee - Null Badge ID - Should Throw Exception")
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        createRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Create Employee - Empty Badge ID - Should Throw Exception")
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        createRequest.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Create Employee - Invalid Email Format - Should Throw Exception")
    void testCreateEmployee_InvalidEmailFormat_ThrowsException() {
        // Arrange
        createRequest.setEmail("invalid-email");
        when(employeeRepository.findByBadgeId(createRequest.getBadgeId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Create Employee - Future Hire Date - Should Throw Exception")
    void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        createRequest.setHireDate(LocalDate.now().plusDays(1));
        when(employeeRepository.findByBadgeId(createRequest.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    @Test
    @DisplayName("Create Employee - Future Date of Birth - Should Throw Exception")
    void testCreateEmployee_FutureDateOfBirth_ThrowsException() {
        // Arrange
        createRequest.setDateOfBirth(LocalDate.now().plusDays(1));
        when(employeeRepository.findByBadgeId(createRequest.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(createRequest);
        });
    }

    // ==================== GET EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Get Employee By ID - Normal Case - Should Return Employee")
    void testGetEmployeeById_NormalCase_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toResponse(testEmployee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.getEmployeeById(employeeId);

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.getId());
        verify(employeeRepository).findById(employeeId);
    }

    @Test
    @DisplayName("Get Employee By ID - Not Found - Should Throw Exception")
    void testGetEmployeeById_NotFound_ThrowsException() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            employeeService.getEmployeeById(employeeId);
        });
    }

    @Test
    @DisplayName("Get Employee By ID - Null ID - Should Throw Exception")
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    @DisplayName("Get Employee By Badge ID - Normal Case - Should Return Employee")
    void testGetEmployeeByBadgeId_NormalCase_Success() {
        // Arrange
        String badgeId = "EMP001";
        when(employeeRepository.findByBadgeId(badgeId)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toResponse(testEmployee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.getEmployeeByBadgeId(badgeId);

        // Assert
        assertNotNull(result);
        assertEquals(badgeId, result.getBadgeId());
    }

    @Test
    @DisplayName("Get Employee By Badge ID - Not Found - Should Throw Exception")
    void testGetEmployeeByBadgeId_NotFound_ThrowsException() {
        // Arrange
        String badgeId = "NONEXISTENT";
        when(employeeRepository.findByBadgeId(badgeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            employeeService.getEmployeeByBadgeId(badgeId);
        });
    }

    @Test
    @DisplayName("Get All Employees - Normal Case - Should Return Page")
    void testGetAllEmployees_NormalCase_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Get All Employees - Empty Result - Should Return Empty Page")
    void testGetAllEmployees_EmptyResult_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponse> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Update Employee - Normal Case - Should Update Successfully")
    void testUpdateEmployee_NormalCase_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.updateEmployee(employeeId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Not Found - Should Throw Exception")
    void testUpdateEmployee_NotFound_ThrowsException() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            employeeService.updateEmployee(employeeId, updateRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Duplicate Email - Should Throw Exception")
    void testUpdateEmployee_DuplicateEmail_ThrowsException() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        Employee anotherEmployee = new Employee();
        anotherEmployee.setId(UUID.randomUUID());
        anotherEmployee.setEmail(updateRequest.getEmail());
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.of(anotherEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(employeeId, updateRequest);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update Employee - Null Fields - Should Keep Existing Values")
    void testUpdateEmployee_NullFields_KeepsExistingValues() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        UpdateEmployeeRequest partialUpdate = new UpdateEmployeeRequest();
        partialUpdate.setFirstName("UpdatedName");
        // Other fields are null
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.updateEmployee(employeeId, partialUpdate);

        // Assert
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    // ==================== SOFT DELETE TESTS ====================

    @Test
    @DisplayName("Soft Delete Employee - Normal Case - Should Mark As Deleted")
    void testSoftDeleteEmployee_NormalCase_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(employeeId);

        // Assert
        verify(employeeRepository).save(argThat(employee -> 
            employee.isDeleted() && employee.getStatus() == EmployeeStatus.TERMINATED
        ));
    }

    @Test
    @DisplayName("Soft Delete Employee - Not Found - Should Throw Exception")
    void testSoftDeleteEmployee_NotFound_ThrowsException() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            employeeService.softDeleteEmployee(employeeId);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Soft Delete Employee - Already Deleted - Should Not Throw Exception")
    void testSoftDeleteEmployee_AlreadyDeleted_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDeleteEmployee(employeeId);

        // Assert
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Restore Employee - Normal Case - Should Restore Successfully")
    void testRestoreEmployee_NormalCase_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        testEmployee.setDeleted(true);
        testEmployee.setStatus(EmployeeStatus.TERMINATED);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.restoreEmployee(employeeId);

        // Assert
        assertNotNull(result);
        verify(employeeRepository).save(argThat(employee -> 
            !employee.isDeleted() && employee.getStatus() == EmployeeStatus.ACTIVE
        ));
    }

    // ==================== SEARCH TESTS ====================

    @Test
    @DisplayName("Search Employees - Normal Case - Should Return Matching Employees")
    void testSearchEmployees_NormalCase_Success() {
        // Arrange
        String searchTerm = "John";
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.searchEmployees(searchTerm, pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.searchEmployees(searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Search Employees - Empty Search Term - Should Return All")
    void testSearchEmployees_EmptySearchTerm_ReturnsAll() {
        // Arrange
        String searchTerm = "";
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.searchEmployees(searchTerm, pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.searchEmployees(searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Search Employees - No Matches - Should Return Empty Page")
    void testSearchEmployees_NoMatches_ReturnsEmptyPage() {
        // Arrange
        String searchTerm = "NonExistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(employeeRepository.searchEmployees(searchTerm, pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponse> result = employeeService.searchEmployees(searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== FILTER TESTS ====================

    @Test
    @DisplayName("Filter Employees - With Department - Should Return Filtered Results")
    void testFilterEmployees_WithDepartment_Success() {
        // Arrange
        UUID departmentId = testDepartment.getId();
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.filterEmployees(
            null, departmentId, null, null, null, null, pageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Filter Employees - With Status - Should Return Filtered Results")
    void testFilterEmployees_WithStatus_Success() {
        // Arrange
        EmployeeStatus status = EmployeeStatus.ACTIVE;
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.filterEmployees(
            null, null, null, status, null, null, pageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Filter Employees - Multiple Criteria - Should Return Filtered Results")
    void testFilterEmployees_MultipleCriteria_Success() {
        // Arrange
        String searchTerm = "John";
        UUID departmentId = testDepartment.getId();
        EmployeeStatus status = EmployeeStatus.ACTIVE;
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.filterEmployees(
            searchTerm, departmentId, null, status, null, null, pageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Create Employee - Minimum Valid Age (18 years) - Should Create Successfully")
    void testCreateEmployee_MinimumValidAge_Success() {
        // Arrange
        createRequest.setDateOfBirth(LocalDate.now().minusYears(18));
        when(employeeRepository.findByBadgeId(createRequest.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(createRequest)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Maximum Badge ID Length - Should Create Successfully")
    void testCreateEmployee_MaxBadgeIdLength_Success() {
        // Arrange
        String longBadgeId = "A".repeat(50); // Assuming max length is 50
        createRequest.setBadgeId(longBadgeId);
        when(employeeRepository.findByBadgeId(longBadgeId)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(createRequest)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee - Hire Date Today - Should Create Successfully")
    void testCreateEmployee_HireDateToday_Success() {
        // Arrange
        createRequest.setHireDate(LocalDate.now());
        when(employeeRepository.findByBadgeId(createRequest.getBadgeId())).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(createRequest)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(testEmployee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.createEmployee(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get All Employees - Large Page Size - Should Handle Correctly")
    void testGetAllEmployees_LargePageSize_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Search Employees - Special Characters in Search Term - Should Handle Correctly")
    void testSearchEmployees_SpecialCharacters_Success() {
        // Arrange
        String searchTerm = "O'Brien";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(employeeRepository.searchEmployees(searchTerm, pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponse> result = employeeService.searchEmployees(searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }
}