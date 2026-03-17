package com.warehouse.ems.service;

import com.warehouse.ems.dto.EmployeeRequestDto;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService.
 * Covers normal operation, null/invalid input, duplicate entries, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeRequestDto employeeRequestDto;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setDeletedAt(null);

        employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setBadgeId("BADGE123");
        employeeRequestDto.setFirstName("John");
        employeeRequestDto.setLastName("Doe");
        employeeRequestDto.setEmail("john.doe@example.com");
        employeeRequestDto.setRole("WORKER");
        employeeRequestDto.setDepartment("Logistics");
        employeeRequestDto.setShiftGroup("A");
        employeeRequestDto.setHireDate(LocalDate.now());
        employeeRequestDto.setStatus("ACTIVE");
    }

    /**
     * Test creating an employee with valid input returns the saved employee.
     */
    @Test
    void testCreateEmployee_ValidInput_ReturnsEmployee() {
        when(employeeRepository.existsByBadgeId(employeeRequestDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.createEmployee(employeeRequestDto);

        assertNotNull(result, "Employee should not be null");
        assertEquals("BADGE123", result.getBadgeId(), "Badge ID should match");
        verify(employeeRepository).save(any(Employee.class));
    }

    /**
     * Test creating an employee with duplicate badgeId throws exception.
     */
    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.existsByBadgeId(employeeRequestDto.getBadgeId())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                employeeService.createEmployee(employeeRequestDto),
                "Should throw IllegalArgumentException for duplicate badgeId");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Test getEmployeeById with valid ID returns employee.
     */
    @Test
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(1L);
        assertNotNull(result, "Employee should be found");
        assertEquals(1L, result.getId());
    }

    /**
     * Test getEmployeeById with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testGetEmployeeById_NonExistentId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                employeeService.getEmployeeById(99L),
                "Should throw EntityNotFoundException for missing employee");
    }

    /**
     * Test getAllEmployees returns paged result.
     */
    @Test
    void testGetAllEmployees_ValidPageable_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        Page<Employee> result = employeeService.getAllEmployees(pageable);
        assertEquals(1, result.getTotalElements(), "Should return one employee");
    }

    /**
     * Test updateEmployee with valid input updates and returns employee.
     */
    @Test
    void testUpdateEmployee_ValidInput_ReturnsUpdatedEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.updateEmployee(1L, employeeRequestDto);
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    /**
     * Test updateEmployee with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testUpdateEmployee_NonExistentId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                employeeService.updateEmployee(99L, employeeRequestDto));
    }

    /**
     * Test deleteEmployee performs soft delete.
     */
    @Test
    void testDeleteEmployee_ValidId_SoftDeletesEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
        verify(employeeRepository).save(any(Employee.class));
    }

    /**
     * Test deleteEmployee with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testDeleteEmployee_NonExistentId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                employeeService.deleteEmployee(99L));
    }

    /**
     * Test existsByBadgeId returns true for existing badgeId.
     */
    @Test
    void testExistsByBadgeId_ExistingBadgeId_ReturnsTrue() {
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(true);
        assertTrue(employeeService.existsByBadgeId("BADGE123"));
    }

    /**
     * Test createEmployee with null input throws exception.
     */
    @Test
    void testCreateEmployee_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                employeeService.createEmployee(null),
                "Should throw IllegalArgumentException for null input");
    }

    /**
     * Test createEmployee with empty badgeId throws exception.
     */
    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsIllegalArgumentException() {
        employeeRequestDto.setBadgeId("");
        assertThrows(IllegalArgumentException.class, () ->
                employeeService.createEmployee(employeeRequestDto),
                "Should throw IllegalArgumentException for empty badgeId");
    }

    /**
     * Test updateEmployee with null DTO throws exception.
     */
    @Test
    void testUpdateEmployee_NullDto_ThrowsIllegalArgumentException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThrows(IllegalArgumentException.class, () ->
                employeeService.updateEmployee(1L, null));
    }

    /**
     * Test getAllEmployees with null pageable throws exception.
     */
    @Test
    void testGetAllEmployees_NullPageable_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                employeeService.getAllEmployees(null));
    }
}
