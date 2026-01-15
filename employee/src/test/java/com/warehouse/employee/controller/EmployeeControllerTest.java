package com.warehouse.employee.controller;

import com.warehouse.employee.entity.Employee;
import com.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {
    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetEmployeeById() {
        Employee emp = Employee.builder().id(1L).name("John Doe").build();
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(emp));
        ResponseEntity<Employee> response = employeeController.getEmployeeById(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(employeeService.getEmployeeById(2L)).thenReturn(Optional.empty());
        ResponseEntity<Employee> response = employeeController.getEmployeeById(2L);
        assertEquals(404, response.getStatusCodeValue());
    }
}
