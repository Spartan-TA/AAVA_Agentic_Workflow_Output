package com.warehouse.management.employee;

import com.warehouse.management.employee.EmployeeController;
import com.warehouse.management.employee.EmployeeService;
import com.warehouse.management.employee.EmployeeDTO;
import com.warehouse.management.employee.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private EmployeeDTO employeeDTO;
    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeDTO = new EmployeeDTO("John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        employee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
    }

    @Test
    void testPostEmployee_Valid() {
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(employee);
        ResponseEntity<Employee> response = employeeController.createEmployee(employeeDTO);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("BADGE123", response.getBody().getBadgeId());
    }

    @Test
    void testPostEmployee_Invalid() {
        when(employeeService.create(any(EmployeeDTO.class))).thenThrow(new IllegalArgumentException("Invalid input"));
        assertThrows(IllegalArgumentException.class, () -> employeeController.createEmployee(employeeDTO));
    }

    @Test
    void testGetEmployee_Valid() {
        when(employeeService.findByBadgeId("BADGE123")).thenReturn(employee);
        ResponseEntity<Employee> response = employeeController.getEmployee("BADGE123");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("BADGE123", response.getBody().getBadgeId());
    }

    @Test
    void testGetEmployee_NotFound() {
        when(employeeService.findByBadgeId("INVALID")).thenThrow(new NoSuchElementException());
        assertThrows(NoSuchElementException.class, () -> employeeController.getEmployee("INVALID"));
    }

    @Test
    void testPutEmployee_Valid() {
        when(employeeService.update(eq(1L), any(EmployeeDTO.class))).thenReturn(employee);
        ResponseEntity<Employee> response = employeeController.updateEmployee(1L, employeeDTO);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testPatchEmployee_Valid() {
        when(employeeService.update(eq(1L), any(EmployeeDTO.class))).thenReturn(employee);
        ResponseEntity<Employee> response = employeeController.patchEmployee(1L, employeeDTO);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteEmployee_Valid() {
        when(employeeService.softDelete(1L)).thenReturn(employee);
        ResponseEntity<Employee> response = employeeController.deleteEmployee(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("DELETED", response.getBody().getStatus());
    }

    @Test
    void testListEmployees_Pagination() {
        List<Employee> employees = Arrays.asList(employee);
        when(employeeService.list(anyInt(), anyInt(), anyString(), any())).thenReturn(employees);
        ResponseEntity<List<Employee>> response = employeeController.listEmployees(0, 10, "ACTIVE", null);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }
}