package com.companyname.wems.employee.controller;

import com.companyname.wems.employee.service.EmployeeService;
import com.companyname.wems.employee.dto.EmployeeDTO;
import com.companyname.wems.employee.dto.CreateEmployeeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private EmployeeService employeeService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private EmployeeDTO testEmployeeDTO;
    private CreateEmployeeRequest validRequest;
    
    @BeforeEach
    void setUp() {
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setFirstName("John");
        testEmployeeDTO.setLastName("Doe");
        testEmployeeDTO.setEmail("john.doe@example.com");
        
        validRequest = new CreateEmployeeRequest();
        validRequest.setBadgeId("EMP001");
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
    }
    
    @Test
    void testCreateEmployee_ValidInput_Returns201() throws Exception {
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(testEmployeeDTO);
        
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }
    
    @Test
    void testCreateEmployee_InvalidInput_Returns400() throws Exception {
        validRequest.setFirstName("");
        
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetEmployeeById_ValidId_Returns200() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);
        
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }
    
    @Test
    void testGetEmployeeById_InvalidId_Returns404() throws Exception {
        when(employeeService.getEmployeeById(999L)).thenThrow(new ResourceNotFoundException("Employee not found"));
        
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound());
    }
}
