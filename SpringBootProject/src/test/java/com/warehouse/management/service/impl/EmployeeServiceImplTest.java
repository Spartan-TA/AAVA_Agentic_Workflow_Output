package com.warehouse.management.service.impl;

import com.warehouse.management.entity.Employee;
import com.warehouse.management.repository.EmployeeRepository;
import com.warehouse.management.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployee() {
        Employee employee = new Employee();
        employee.setName("John Doe");
        when(employeeRepository.save(employee)).thenReturn(employee);
        Employee result = employeeService.createEmployee(employee);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetAllEmployees() {
        Employee e1 = new Employee();
        Employee e2 = new Employee();
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(e1, e2));
        List<Employee> employees = employeeService.getAllEmployees();
        assertEquals(2, employees.size());
    }

    @Test
    void testGetEmployeeById() {
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Optional<Employee> result = employeeService.getEmployeeById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }
}
