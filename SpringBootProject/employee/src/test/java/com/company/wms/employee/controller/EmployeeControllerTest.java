package com.company.wms.employee.controller;

import com.company.wms.employee.dto.CreateEmployeeRequest;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.dto.UpdateEmployeeRequest;
import com.company.wms.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EmployeeController.
 */
class EmployeeControllerTest {
    private MockMvc mockMvc;
    @Mock
    private EmployeeService employeeService;
    @InjectMocks
    private EmployeeController employeeController;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateEmployee() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setDepartment("HR");
        request.setPosition("Manager");
        request.setHireDate(LocalDate.now());
        EmployeeDTO dto = new EmployeeDTO(1L, "John", "Doe", "john.doe@example.com", "HR", "Manager", LocalDate.now(), true);
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(dto);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testUpdateEmployee() throws Exception {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@example.com");
        request.setDepartment("Finance");
        request.setPosition("Analyst");
        request.setHireDate(LocalDate.now());
        EmployeeDTO dto = new EmployeeDTO(2L, "Jane", "Smith", "jane.smith@example.com", "Finance", "Analyst", LocalDate.now(), true);
        when(employeeService.updateEmployee(anyLong(), any(UpdateEmployeeRequest.class))).thenReturn(dto);
        mockMvc.perform(put("/api/employees/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void testDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetEmployee() throws Exception {
        EmployeeDTO dto = new EmployeeDTO(4L, "Alice", "Brown", "alice.brown@example.com", "IT", "Developer", LocalDate.now(), true);
        when(employeeService.getEmployee(4L)).thenReturn(dto);
        mockMvc.perform(get("/api/employees/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void testListAllActiveEmployees() throws Exception {
        EmployeeDTO dto = new EmployeeDTO(5L, "Bob", "White", "bob.white@example.com", "Ops", "Worker", LocalDate.now(), true);
        when(employeeService.listAllActiveEmployees()).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/employees/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Bob"));
    }
}