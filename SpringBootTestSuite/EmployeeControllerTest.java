package com.warehouse.ems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.dto.employee.*;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

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

    private EmployeeResponse employeeResponse() {
        return EmployeeResponse.builder()
                .id(1L)
                .badgeId("B123")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();
    }

    private EmployeeCreateRequest validCreateRequest() {
        return EmployeeCreateRequest.builder()
                .badgeId("B123")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();
    }

    private EmployeeUpdateRequest validUpdateRequest() {
        return EmployeeUpdateRequest.builder()
                .name("Jane Smith")
                .role("Supervisor")
                .department("Shipping")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("Inactive")
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_ValidRequest_Returns201() throws Exception {
        EmployeeCreateRequest request = validCreateRequest();
        EmployeeResponse response = employeeResponse();
        when(employeeService.createEmployee(any())).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.badgeId").value("B123"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_InvalidRequest_Returns400() throws Exception {
        EmployeeCreateRequest request = validCreateRequest();
        request.setBadgeId(""); // Invalid

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployees_WithPagination_Returns200() throws Exception {
        EmployeeResponse response = employeeResponse();
        Page<EmployeeResponse> page = new PageImpl<>(Collections.singletonList(response));
        when(employeeService.getEmployees(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/employees?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployee_ValidId_Returns200() throws Exception {
        EmployeeResponse response = employeeResponse();
        when(employeeService.getEmployee(1L)).thenReturn(response);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployee_InvalidId_Returns404() throws Exception {
        when(employeeService.getEmployee(99L)).thenThrow(new ResourceNotFoundException("Employee not found"));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEmployee_ValidRequest_Returns200() throws Exception {
        EmployeeUpdateRequest request = validUpdateRequest();
        EmployeeResponse response = employeeResponse();
        response.setName("Jane Smith");
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Smith"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_ValidId_Returns204() throws Exception {
        doNothing().when(employeeService).softDeleteEmployee(1L);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testCreateEmployee_UnauthorizedUser_Returns403() throws Exception {
        EmployeeCreateRequest request = validCreateRequest();
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testDeleteEmployee_NonAdminUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}
