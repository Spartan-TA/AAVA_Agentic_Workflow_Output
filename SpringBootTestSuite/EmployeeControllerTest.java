package com.warehouse.employee.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.management.dto.*;
import com.warehouse.employee.management.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    EmployeeService employeeService;
    @Autowired
    ObjectMapper objectMapper;

    EmployeeResponse employeeResponse;
    EmployeeCreateRequest createRequest;
    EmployeeUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        employeeResponse = EmployeeResponse.builder()
                .id(1L)
                .badgeId("BID1")
                .firstName("John")
                .lastName("Doe")
                .email("john@ex.com")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        createRequest = EmployeeCreateRequest.builder()
                .badgeId("BID1")
                .firstName("John")
                .lastName("Doe")
                .email("john@ex.com")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
        updateRequest = EmployeeUpdateRequest.builder()
                .firstName("Johnny")
                .lastName("Doey")
                .email("johnny@ex.com")
                .role("SUPERVISOR")
                .department("Warehouse")
                .shiftGroup("B")
                .status("INACTIVE")
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testCreateEmployee_Valid_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any())).thenReturn(employeeResponse);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("BID1"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testCreateEmployee_Invalid_ReturnsBadRequest() throws Exception {
        EmployeeCreateRequest invalid = EmployeeCreateRequest.builder().build();
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployee_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"})
    void testGetEmployee_Found() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employeeResponse));
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"})
    void testGetEmployee_NotFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetEmployee_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void testGetAllEmployees() throws Exception {
        Page<EmployeeResponse> page = new PageImpl<>(List.of(employeeResponse));
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("BID1"));
    }

    @Test
    void testGetAllEmployees_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void testSearchEmployees() throws Exception {
        Page<EmployeeResponse> page = new PageImpl<>(List.of(employeeResponse));
        when(employeeService.searchEmployees(eq("john"), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees/search").param("query", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    void testSearchEmployees_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees/search").param("query", "john"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void testUpdateEmployee_Found() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(Optional.of(employeeResponse));
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void testUpdateEmployee_NotFound() throws Exception {
        when(employeeService.updateEmployee(eq(2L), any())).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/employees/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateEmployee_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testDeleteEmployee_Found() throws Exception {
        when(employeeService.deleteEmployee(1L)).thenReturn(true);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testDeleteEmployee_NotFound() throws Exception {
        when(employeeService.deleteEmployee(2L)).thenReturn(false);
        mockMvc.perform(delete("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteEmployee_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}
