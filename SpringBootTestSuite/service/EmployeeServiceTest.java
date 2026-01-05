package com.example.warehouse.test.service;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.Department;
import com.example.warehouse.entity.Role;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee("Alice Brown", "B789", Role.SUPERVISOR, new Department("Operations"), "C", LocalDate.now().minusMonths(6), "ACTIVE");
    }

    @Test
    void testCreateEmployee_ValidEmployee_ShouldReturnSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee saved = employeeService.createEmployee(employee);
        assertEquals("Alice Brown", saved.getName());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void testCreateEmployee_NullEmployee_ShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
        assertTrue(ex.getMessage().contains("employee"));
    }

    @Test
    void testGetEmployeeByBadgeId_Existing_ShouldReturnEmployee() {
        when(employeeRepository.findByBadgeId("B789")).thenReturn(Optional.of(employee));
        Employee found = employeeService.getEmployeeByBadgeId("B789");
        assertEquals("Alice Brown", found.getName());
    }

    @Test
    void testGetEmployeeByBadgeId_NotFound_ShouldThrowException() {
        when(employeeRepository.findByBadgeId("X000")).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> employeeService.getEmployeeByBadgeId("X000"));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void testUpdateEmployeeStatus_Valid_ShouldUpdateStatus() {
        when(employeeRepository.findByBadgeId("B789")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee updated = employeeService.updateEmployeeStatus("B789", "INACTIVE");
        assertEquals("INACTIVE", updated.getStatus());
    }
}