package com.company.warehouse.employee.controller;

import com.company.warehouse.employee.dto.EmployeeDTO;
import com.company.warehouse.employee.entity.Role;
import com.company.warehouse.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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

    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setBadgeId("EMP001");
        employeeDTO.setName("John Doe");
        employeeDTO.setRole(Role.WORKER);
        employeeDTO.setDepartment("Logistics");
        employeeDTO.setHireDate(LocalDate.now());
        employeeDTO.setStatus("ACTIVE");
    }

    @Test
    void createEmployee_ValidInput_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId", is("EMP001")));
    }

    @Test
    void createEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        EmployeeDTO invalidDto = new EmployeeDTO();
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmployee_ValidInput_ReturnsOk() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("EMP001")));
    }

    @Test
    void updateEmployee_NotFound_ReturnsNotFound() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(EmployeeDTO.class))).thenThrow(new RuntimeException("Employee not found"));
        mockMvc.perform(put("/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmployee_ValidId_ReturnsOk() throws Exception {
        when(employeeService.getEmployee(1L)).thenReturn(employeeDTO);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("EMP001")));
    }

    @Test
    void getEmployee_NotFound_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployee(99L)).thenThrow(new RuntimeException("Employee not found"));
        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listEmployees_NoFilters_ReturnsOk() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.singletonList(employeeDTO));
        when(employeeService.listEmployees(any(Pageable.class), any(), any())).thenReturn(page);
        mockMvc.perform(get("/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].badgeId", is("EMP001")));
    }

    @Test
    void listEmployees_WithFilters_ReturnsFiltered() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.singletonList(employeeDTO));
        when(employeeService.listEmployees(any(Pageable.class), eq("Logistics"), eq("ACTIVE"))).thenReturn(page);
        mockMvc.perform(get("/employees?department=Logistics&status=ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void listEmployees_EmptyResult_ReturnsEmptyContent() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.emptyList());
        when(employeeService.listEmployees(any(Pageable.class), any(), any())).thenReturn(page);
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void softDeleteEmployee_ValidId_ReturnsNoContent() throws Exception {
        doNothing().when(employeeService).softDeleteEmployee(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void softDeleteEmployee_NotFound_ReturnsNotFound() throws Exception {
        doThrow(new RuntimeException("Employee not found")).when(employeeService).softDeleteEmployee(99L);
        mockMvc.perform(delete("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEmployee_EmptyBadgeId_ReturnsBadRequest() throws Exception {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("");
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEmployee_NullId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/employees/null"))
                .andExpect(status().isBadRequest());
    }
}
