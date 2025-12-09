package com.warehouse.ems.test;

import com.warehouse.ems.employee.EmployeeService;
import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.employee.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("EmployeeService Unit Tests")
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusYears(1))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Create employee with valid data should succeed")
    void testCreateEmployeeWithValidData() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee result = employeeService.createEmployee(validEmployee);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    @Test
    @DisplayName("Create employee with null name should throw exception")
    void testCreateEmployeeWithNullName_ShouldThrowException() {
        Employee emp = validEmployee.toBuilder().name(null).build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    @DisplayName("Create employee with duplicate badgeId should throw exception")
    void testCreateEmployeeWithDuplicateBadgeId_ShouldThrowException() {
        when(employeeRepository.save(any(Employee.class))).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.createEmployee(validEmployee));
    }

    @Test
    @DisplayName("Create employee with invalid role should throw exception")
    void testCreateEmployeeWithInvalidRole_ShouldThrowException() {
        Employee emp = validEmployee.toBuilder().role("INVALID_ROLE").build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    @DisplayName("Update employee should persist changes")
    void testUpdateEmployee() {
        Employee updated = validEmployee.toBuilder().department("Receiving").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertEquals("Receiving", result.getDepartment());
    }

    @Test
    @DisplayName("Soft delete employee should set status to INACTIVE")
    void testSoftDeleteEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Employee inactive = validEmployee.toBuilder().status("INACTIVE").build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(inactive);
        Employee result = employeeService.softDeleteEmployee(1L);
        assertEquals("INACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("Get employees by department should return correct list")
    void testGetEmployeesByDepartment() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByDepartment("Shipping")).thenReturn(employees);
        List<Employee> result = employeeService.getEmployeesByDepartment("Shipping");
        assertEquals(1, result.size());
        assertEquals("Shipping", result.get(0).getDepartment());
    }

    @Test
    @DisplayName("Get employees with pagination should return paged results")
    void testGetEmployeesWithPagination() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findAll(any())).thenReturn(employees);
        List<Employee> result = employeeService.getEmployeesPaged(0, 10);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Create employee with empty name should throw exception")
    void testCreateEmployeeWithEmptyName_ShouldThrowException() {
        Employee emp = validEmployee.toBuilder().name("").build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    @DisplayName("Create employee with null badgeId should throw exception")
    void testCreateEmployeeWithNullBadgeId_ShouldThrowException() {
        Employee emp = validEmployee.toBuilder().badgeId(null).build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    @DisplayName("Create employee with invalid department should throw exception")
    void testCreateEmployeeWithInvalidDepartment_ShouldThrowException() {
        Employee emp = validEmployee.toBuilder().department("").build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    @DisplayName("Create employee with future hireDate should throw exception")
    void testCreateEmployeeWithFutureHireDate_ShouldThrowException() {
        Employee emp = validEmployee.toBuilder().hireDate(LocalDate.now().plusDays(1)).build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    @DisplayName("Get employee by id not found should throw exception")
    void testGetEmployeeByIdNotFound_ShouldThrowException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    @DisplayName("Create employee with null input should throw exception")
    void testCreateEmployeeWithNullInput_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }
}
