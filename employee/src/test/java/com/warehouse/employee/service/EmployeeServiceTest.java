package com.warehouse.employee.service;

import com.warehouse.employee.entity.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    void testGetEmployeeById() {
        Employee emp = Employee.builder().id(1L).name("John Doe").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        Optional<Employee> result = employeeService.getEmployeeById(1L);
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void testAddEmployee() {
        Employee emp = Employee.builder().name("Jane Smith").build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        Employee saved = employeeService.addEmployee(emp);
        assertEquals("Jane Smith", saved.getName());
    }
}
