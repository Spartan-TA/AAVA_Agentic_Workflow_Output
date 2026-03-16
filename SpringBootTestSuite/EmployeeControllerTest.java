package com.warehouse.ems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("B123", "John Doe", "WORKER");
        employee.setId(1L);
        employee.setDepartment("Logistics");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus("ACTIVE");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllEmployees_Admin() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeService.getEmployees(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(employee)));
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("B123"));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetEmployeeById_Found() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employee));
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B123"));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void testGetEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployeeByBadgeId_Found() throws Exception {
        when(employeeService.getEmployeeByBadgeId("B123")).thenReturn(Optional.of(employee));
        mockMvc.perform(get("/api/employees/badge/B123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B123"));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetEmployeeByBadgeId_NotFound() throws Exception {
        when(employeeService.getEmployeeByBadgeId("B999")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/employees/badge/B999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_Normal() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("B123"));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testCreateEmployee_DuplicateBadgeId() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenThrow(new RuntimeException("Badge ID already exists"));
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEmployee_Normal() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(employee);
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B123"));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testUpdateEmployee_NotFound() throws Exception {
        when(employeeService.updateEmployee(eq(2L), any(Employee.class))).thenThrow(new RuntimeException("Employee not found"));
        mockMvc.perform(put("/api/employees/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_Normal() throws Exception {
        doNothing().when(employeeService).softDeleteEmployee(1L);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_NotFound() throws Exception {
        doThrow(new RuntimeException("Employee not found")).when(employeeService).softDeleteEmployee(2L);
        mockMvc.perform(delete("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetEmployeesByDepartment() throws Exception {
        when(employeeService.getEmployeesByDepartment("Logistics")).thenReturn(List.of(employee));
        mockMvc.perform(get("/api/employees/department/Logistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Logistics"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployeesByRole() throws Exception {
        when(employeeService.getEmployeesByRole("WORKER")).thenReturn(List.of(employee));
        mockMvc.perform(get("/api/employees/role/WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("WORKER"));
    }

    // Security tests
    @Test
    void testGetAllEmployees_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testGetAllEmployees_Forbidden() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testDeleteEmployee_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}
