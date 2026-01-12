package com.warehouse.ems.service;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(1L, "John Doe", "B123", "WORKER", "Logistics", "A", LocalDate.now(), "ACTIVE", false);
    }

    @Test
    void testCreateEmployee_WithValidInput_ReturnsEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.createEmployee(employee);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void testCreateEmployee_WithNullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void testGetEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetEmployeeById_NonExistingId_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    void testUpdateEmployee_ValidInput_ReturnsUpdatedEmployee() {
        Employee updated = new Employee(1L, "Jane Doe", "B123", "HR", "HR", "B", LocalDate.now(), "ACTIVE", false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);

        Employee result = employeeService.updateEmployee(1L, updated);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        verify(employeeRepository).save(updated);
    }

    @Test
    void testUpdateEmployee_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    void testDeleteEmployee_ValidId_SetsDeletedTrue() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).delete(employee);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void testDeleteEmployee_NonExistingId_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> employeeService.deleteEmployee(2L));
    }

    @Test
    void testGetAllEmployees_ReturnsList() {
        List<Employee> employees = Arrays.asList(employee, new Employee(2L, "Alice", "B124", "SUPERVISOR", "Ops", "B", LocalDate.now(), "ACTIVE", false));
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
    }

    @Test
    void testGetAllEmployees_EmptyList() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Employee> result = employeeService.getAllEmployees();

        assertTrue(result.isEmpty());
    }
}