package com.example.warehouse.controller;

import com.example.warehouse.dto.EmployeeDto;
import com.example.warehouse.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_ValidRequest_Returns201() throws Exception {
        EmployeeDto dto = new EmployeeDto();
        dto.setName("John Doe");
        dto.setBadgeId("BADGE123");
        Mockito.when(employeeService.createEmployee(any())).thenReturn(dto);

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidRequest_Returns400() throws Exception {
        EmployeeDto dto = new EmployeeDto(); // missing required fields

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployee_UnauthorizedUser_Returns401() throws Exception {
        EmployeeDto dto = new EmployeeDto();
        dto.setName("John Doe");
        dto.setBadgeId("BADGE123");

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_ValidRequest_Returns200() throws Exception {
        Mockito.when(employeeService.getAllEmployees(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_ExistingId_Returns200() throws Exception {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(1L);
        Mockito.when(employeeService.getEmployeeById(1L)).thenReturn(dto);

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NonExistingId_Returns404() throws Exception {
        Mockito.when(employeeService.getEmployeeById(99L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_ValidRequest_Returns200() throws Exception {
        EmployeeDto dto = new EmployeeDto();
        dto.setName("Jane Doe");
        Mockito.when(employeeService.updateEmployee(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_InvalidId_Returns404() throws Exception {
        EmployeeDto dto = new EmployeeDto();
        Mockito.when(employeeService.updateEmployee(eq(99L), any())).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(put("/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_ValidId_Returns204() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testDeleteEmployee_UnauthorizedRole_Returns403() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isForbidden());
    }
}