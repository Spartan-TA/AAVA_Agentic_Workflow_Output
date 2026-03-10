package com.example.warehouse.employee;

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

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEmployees() {
        Employee emp1 = new Employee();
        Employee emp2 = new Employee();
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(emp1, emp2));
        List<Employee> employees = employeeService.getAllEmployees();
        assertEquals(2, employees.size());
    }

    @Test
    void testGetEmployeeById() {
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        Optional<Employee> found = employeeService.getEmployeeById(1L);
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
    }
}
