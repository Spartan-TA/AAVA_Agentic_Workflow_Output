package com.warehouse.ems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.dto.EmployeeRequestDto;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for EmployeeController.
 * Covers successful requests, error responses, security, and validation.
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;
    private EmployeeRequestDto employeeRequestDto;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");

        employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setBadgeId("BADGE123");
        employeeRequestDto.setFirstName("John");
        employeeRequestDto.setLastName("Doe");
        employeeRequestDto.setEmail("john.doe@example.com");
        employeeRequestDto.setRole("WORKER");
        employeeRequestDto.setDepartment("Logistics");
        employeeRequestDto.setShiftGroup("A");
        employeeRequestDto.setHireDate(LocalDate.now());
        employeeRequestDto.setStatus("ACTIVE");
    }

    /**
     * Test POST /employees with ADMIN role returns 201 Created.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_AdminRole_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDto.class))).thenReturn(employee);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    /**
     * Test POST /employees with non-ADMIN role returns 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_NonAdminRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isForbidden());
    }

    /**
     * Test GET /employees/{id} with valid ID and authorized role returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_ValidId_ReturnsOk() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    /**
     * Test GET /employees/{id} with non-existent ID returns 404 Not Found.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NonExistentId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenThrow(new com.warehouse.ems.exception.EntityNotFoundException("Not found"));
        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test GET /employees with SUPERVISOR role returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testGetAllEmployees_SupervisorRole_ReturnsOk() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    /**
     * Test PUT /employees/{id} with ADMIN role returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_AdminRole_ReturnsOk() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequestDto.class))).thenReturn(employee);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    /**
     * Test DELETE /employees/{id} with ADMIN role returns 204 No Content.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_AdminRole_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    /**
     * Test GET /employees without authentication returns 401 Unauthorized.
     */
    @Test
    void testGetAllEmployees_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test POST /employees with invalid DTO returns 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidDto_ReturnsBadRequest() throws Exception {
        EmployeeRequestDto invalidDto = new EmployeeRequestDto();
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
