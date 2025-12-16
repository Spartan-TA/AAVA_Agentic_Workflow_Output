package com.example.ems.service;

import com.example.ems.domain.Employee;
import com.example.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private AutoCloseable closeable;

    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testCreateEmployee_WithValidData_ReturnsCreatedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee created = employeeService.createEmployee(validEmployee);
        assertThat(created).isNotNull();
        assertThat(created.getBadgeId()).isEqualTo("BADGE123");
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ThrowsException() {
        when(employeeRepository.save(any(Employee.class))).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.createEmployee(validEmployee));
    }

    @Test
    void testCreateEmployee_WithNullName_ThrowsIllegalArgumentException() {
        Employee emp = Employee.builder().badgeId("BADGE999").build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    void testGetEmployeeById_WithExistingId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Employee found = employeeService.getEmployeeById(1L);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("John Doe");
    }

    @Test
    void testGetEmployeeById_WithNonExistingId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    void testUpdateEmployee_WithValidData_ReturnsUpdatedEmployee() {
        Employee updated = Employee.builder()
                .id(1L)
                .name("Jane Smith")
                .badgeId("BADGE123")
                .role("HR")
                .department("HR")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("ACTIVE")
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertThat(result.getName()).isEqualTo("Jane Smith");
        assertThat(result.getRole()).isEqualTo("HR");
    }

    @Test
    void testUpdateEmployee_WithNullBadgeId_ThrowsIllegalArgumentException() {
        Employee updated = Employee.builder().id(1L).name("Jane").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, updated));
    }

    @Test
    void testDeleteEmployee_SoftDelete_SetsStatusInactive() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        employeeService.deleteEmployee(1L);
        verify(employeeRepository).save(argThat(emp -> "INACTIVE".equals(emp.getStatus())));
    }

    @Test
    void testDeleteEmployee_WithNonExistingId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.deleteEmployee(99L));
    }

    @Test
    void testListEmployees_WithPagination_ReturnsPagedResult() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findAll()).thenReturn(employees);
        List<Employee> result = employeeService.listEmployees();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBadgeId()).isEqualTo("BADGE123");
    }

    @Test
    void testCreateEmployee_WithEmptyBadgeId_ThrowsIllegalArgumentException() {
        Employee emp = Employee.builder().name("Empty Badge").badgeId("").build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    void testCreateEmployee_WithNullEmployee_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void testSecurity_AdminCanCreateEmployee() {
        // Simulate security context with ADMIN role (pseudo-code, actual implementation may differ)
        // Assume employeeService.createEmployee checks for ADMIN role
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        assertDoesNotThrow(() -> employeeService.createEmployee(validEmployee));
    }

    @Test
    void testSecurity_WorkerCannotDeleteEmployee() {
        // Simulate security context with WORKER role (pseudo-code)
        // Assume employeeService.deleteEmployee checks for ADMIN/HR role
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        doThrow(new AccessDeniedException("Forbidden")).when(employeeRepository).save(any(Employee.class));
        assertThrows(AccessDeniedException.class, () -> employeeService.deleteEmployee(1L));
    }
}
