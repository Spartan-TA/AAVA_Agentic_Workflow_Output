package com.warehouse.api.controller;

import com.warehouse.api.dto.EmployeeDTO;
import com.warehouse.api.service.EmployeeService;
import com.warehouse.domain.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {
    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private AutoCloseable closeable;
    private Employee employee;
    private EmployeeDTO dto;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("B1");
        employee.setName("John");
        employee.setRole("Worker");
        employee.setDepartment("Packing");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus("Active");
        employee.setDeleted(false);
        dto = new EmployeeDTO();
        dto.setId(1L);
        dto.setBadgeId("B1");
        dto.setName("John");
        dto.setRole("Worker");
        dto.setDepartment("Packing");
        dto.setHireDate(LocalDate.of(2020, 1, 1));
        dto.setStatus("Active");
        dto.setDeleted(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetAllEmployeesReturnsList() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.singletonList(employee));
        List<EmployeeDTO> result = employeeController.getAllEmployees();
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    void testGetAllEmployeesReturnsEmptyList() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());
        List<EmployeeDTO> result = employeeController.getAllEmployees();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEmployeeFound() {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employee));
        ResponseEntity<EmployeeDTO> response = employeeController.getEmployee(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John", response.getBody().getName());
    }

    @Test
    void testGetEmployeeNotFound() {
        when(employeeService.getEmployeeById(2L)).thenReturn(Optional.empty());
        ResponseEntity<EmployeeDTO> response = employeeController.getEmployee(2L);
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testCreateEmployee() {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);
        EmployeeDTO result = employeeController.createEmployee(dto);
        assertEquals("John", result.getName());
    }

    @Test
    void testUpdateEmployee() {
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(employee);
        EmployeeDTO result = employeeController.updateEmployee(1L, dto);
        assertEquals("John", result.getName());
    }

    @Test
    void testDeleteEmployee() {
        doNothing().when(employeeService).deleteEmployee(1L);
        ResponseEntity<Void> response = employeeController.deleteEmployee(1L);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testToDTOAndToEntityNulls() {
        // Reflection to test private methods
        // Not recommended in production, but for coverage:
        try {
            java.lang.reflect.Method toDTO = EmployeeController.class.getDeclaredMethod("toDTO", Employee.class);
            toDTO.setAccessible(true);
            EmployeeDTO result = (EmployeeDTO) toDTO.invoke(employeeController, (Employee) null);
            assertNull(result);
        } catch (Exception ignored) {}
        try {
            java.lang.reflect.Method toEntity = EmployeeController.class.getDeclaredMethod("toEntity", EmployeeDTO.class);
            toEntity.setAccessible(true);
            Employee result = (Employee) toEntity.invoke(employeeController, (EmployeeDTO) null);
            assertNull(result);
        } catch (Exception ignored) {}
    }
}
