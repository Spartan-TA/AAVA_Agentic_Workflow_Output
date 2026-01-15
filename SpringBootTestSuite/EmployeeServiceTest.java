package com.company.wms.employee.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private CreateEmployeeRequest validCreateRequest;
    private UpdateEmployeeRequest validUpdateRequest;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        validCreateRequest = new CreateEmployeeRequest("John Doe", "john.doe@company.com", "BADGE123", "HR", "ACTIVE", "WORKER", LocalDate.now().minusDays(1));
        validUpdateRequest = new UpdateEmployeeRequest("Jane Doe", "jane.doe@company.com", "HR", "ACTIVE", "SUPERVISOR");
        employee = new Employee(1L, "John Doe", "john.doe@company.com", "BADGE123", "HR", "ACTIVE", "WORKER", LocalDate.now().minusDays(1));
    }

    @Test
    public void testCreateEmployee_WithValidInput_Success() {
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(validCreateRequest);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testCreateEmployee_WithNullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    public void testCreateEmployee_WithEmptyName_ThrowsValidationException() {
        CreateEmployeeRequest req = new CreateEmployeeRequest("", "john.doe@company.com", "BADGE123", "HR", "ACTIVE", "WORKER", LocalDate.now().minusDays(1));
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(req));
    }

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ThrowsConstraintViolationException() {
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(true);
        assertThrows(ConstraintViolationException.class, () -> employeeService.createEmployee(validCreateRequest));
    }

    @Test
    public void testCreateEmployee_WithInvalidEmail_ThrowsValidationException() {
        CreateEmployeeRequest req = new CreateEmployeeRequest("John Doe", "invalid-email", "BADGE123", "HR", "ACTIVE", "WORKER", LocalDate.now().minusDays(1));
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(req));
    }

    @Test
    public void testCreateEmployee_WithFutureHireDate_ThrowsValidationException() {
        CreateEmployeeRequest req = new CreateEmployeeRequest("John Doe", "john.doe@company.com", "BADGE123", "HR", "ACTIVE", "WORKER", LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(req));
    }

    @Test
    public void testUpdateEmployee_WithValidInput_Success() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.updateEmployee(1L, validUpdateRequest);
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
    }

    @Test
    public void testUpdateEmployee_WithNonExistentId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(99L, validUpdateRequest));
    }

    @Test
    public void testUpdateEmployee_WithNullId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(null, validUpdateRequest));
    }

    @Test
    public void testDeleteEmployee_SoftDeletesEmployee_Success() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        Employee inactiveEmployee = new Employee(1L, "John Doe", "john.doe@company.com", "BADGE123", "HR", "INACTIVE", "WORKER", LocalDate.now().minusDays(1));
        when(employeeRepository.save(any(Employee.class))).thenReturn(inactiveEmployee);
        Employee result = employeeService.deleteEmployee(1L);
        assertEquals("INACTIVE", result.getStatus());
    }

    @Test
    public void testDeleteEmployee_WithNonExistentId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(99L));
    }

    @Test
    public void testFindById_WithValidId_ReturnsEmployee() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        Employee result = employeeService.findById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testFindById_WithNonExistentId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.findById(99L));
    }

    @Test
    public void testFindAll_WithPagination_ReturnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.findAll(pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testFindByDepartment_WithValidDepartment_ReturnsFilteredResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.findByDepartment(eq("HR"), eq(pageable))).thenReturn(page);
        Page<Employee> result = employeeService.findByDepartment("HR", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testFindByStatus_WithActiveStatus_ReturnsOnlyActiveEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.findByStatus(eq("ACTIVE"), eq(pageable))).thenReturn(page);
        Page<Employee> result = employeeService.findByStatus("ACTIVE", pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("ACTIVE", result.getContent().get(0).getStatus());
    }

    @Test
    public void testFindByRole_WithValidRole_ReturnsFilteredResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.findByRole(eq("WORKER"), eq(pageable))).thenReturn(page);
        Page<Employee> result = employeeService.findByRole("WORKER", pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("WORKER", result.getContent().get(0).getRole());
    }
}
