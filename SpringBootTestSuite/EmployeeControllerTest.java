// EmployeeControllerTest.java
package com.warehouse.ems.employee.controller;

import com.warehouse.ems.employee.dto.*;
import com.warehouse.ems.employee.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO employeeDTO;
    private EmployeeCreateDTO createDTO;
    private EmployeeUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        employeeDTO = EmployeeDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .hireDate("2023-01-01")
                .department("IT")
                .position("Developer")
                .salary(50000.0)
                .active(true)
                .createdAt("2023-01-01T00:00:00")
                .updatedAt("2023-01-01T00:00:00")
                .build();
        createDTO = EmployeeCreateDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .hireDate("2023-01-01")
                .department("IT")
                .position("Developer")
                .salary(50000.0)
                .build();
        updateDTO = EmployeeUpdateDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("0987654321")
                .hireDate("2023-02-01")
                .department("HR")
                .position("Manager")
                .salary(60000.0)
                .active(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        // No resources to clean up
    }

    @Test
    void testCreateEmployee_ValidRequest() throws Exception {
        when(employeeService.createEmployee(any(EmployeeCreateDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","phone":"1234567890","hireDate":"2023-01-01","department":"IT","position":"Developer","salary":50000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void testCreateEmployee_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"firstName":"","lastName":"","email":"invalid-email"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(employeeDTO));
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    void testGetEmployeeById_ValidId() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeDTO);
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void testGetEmployeeById_NonExistentId() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenThrow(new RuntimeException("Employee not found"));
        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateEmployee_ValidRequest() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeUpdateDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"firstName":"Jane","lastName":"Smith","email":"jane.smith@example.com","phone":"0987654321","hireDate":"2023-02-01","department":"HR","position":"Manager","salary":60000.0,"active":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteEmployee_ValidId() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testBulkImport_ValidRequest() throws Exception {
        BulkImportResult result = BulkImportResult.builder()
                .successCount(1)
                .failureCount(0)
                .errorMessages(Collections.emptyList())
                .build();
        when(employeeService.bulkImport(anyList())).thenReturn(result);
        mockMvc.perform(post("/api/employees/bulk-import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","phone":"1234567890","hireDate":"2023-01-01","department":"IT","position":"Developer","salary":50000.0}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1));
    }
}