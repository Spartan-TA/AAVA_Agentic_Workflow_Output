package com.warehouse.ems.controller;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(1L, "John Doe", "B123", "WORKER", "Logistics", "A", LocalDate.now(), "ACTIVE", false);
    }

    @Test
    void testCreateEmployee_ValidInput_ReturnsCreated() {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);

        ResponseEntity<Employee> response = employeeController.createEmployee(employee);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(employee, response.getBody());
    }

    @Test
    void testCreateEmployee_NullInput_ReturnsBadRequest() {
        ResponseEntity<Employee> response = employeeController.createEmployee(null);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testGetEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        ResponseEntity<Employee> response = employeeController.getEmployeeById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(employee, response.getBody());
    }

    @Test
    void testGetEmployeeById_NonExistingId_ReturnsNotFound() {
        when(employeeService.getEmployeeById(2L)).thenThrow(NoSuchElementException.class);

        ResponseEntity<Employee> response = employeeController.getEmployeeById(2L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testUpdateEmployee_ValidInput_ReturnsOk() {
        Employee updated = new Employee(1L, "Jane Doe", "B123", "HR", "HR", "B", LocalDate.now(), "ACTIVE", false);
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(updated);

        ResponseEntity<Employee> response = employeeController.updateEmployee(1L, updated);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updated, response.getBody());
    }

    @Test
    void testUpdateEmployee_NullInput_ReturnsBadRequest() {
        ResponseEntity<Employee> response = employeeController.updateEmployee(1L, null);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testDeleteEmployee_ValidId_ReturnsNoContent() {
        doNothing().when(employeeService).deleteEmployee(1L);

        ResponseEntity<Void> response = employeeController.deleteEmployee(1L);

        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testDeleteEmployee_NonExistingId_ReturnsNotFound() {
        doThrow(NoSuchElementException.class).when(employeeService).deleteEmployee(2L);

        ResponseEntity<Void> response = employeeController.deleteEmployee(2L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetAllEmployees_ReturnsList() {
        List<Employee> employees = Arrays.asList(employee, new Employee(2L, "Alice", "B124", "SUPERVISOR", "Ops", "B", LocalDate.now(), "ACTIVE", false));
        when(employeeService.getAllEmployees()).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetAllEmployees_EmptyList_ReturnsOk() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }
}