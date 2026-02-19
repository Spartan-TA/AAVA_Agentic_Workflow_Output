package com.company.wems.controller;

import com.company.wems.dto.request.EmployeeRequestDTO;
import com.company.wems.dto.response.EmployeeResponseDTO;
import com.company.wems.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests all REST endpoints with security, validation, and error handling
 * 
 * @author WEMS Test Suite Generator
 * @version 1.0
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeRequestDTO validRequest;
    private EmployeeResponseDTO validResponse;

    @BeforeEach
    void setUp() {
        // Setup valid request DTO
        validRequest = new EmployeeRequestDTO();
        validRequest.setBadgeId("EMP001");
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPhone("+1234567890");
        validRequest.setRole("WORKER");
        validRequest.setDepartment("Warehouse");
        validRequest.setHireDate(LocalDate.of(2023, 1, 15));
        validRequest.setTenantId(1L);

        // Setup valid response DTO
        validResponse = new EmployeeResponseDTO();
        validResponse.setId(1L);
        validResponse.setBadgeId("EMP001");
        validResponse.setFirstName("John");
        validResponse.setLastName("Doe");
        validResponse.setEmail("john.doe@example.com");
        validResponse.setPhone("+1234567890");
        validResponse.setRole("WORKER");
        validResponse.setDepartment("Warehouse");
        validResponse.setHireDate(LocalDate.of(2023, 1, 15));
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_WithValidData_Returns201Created() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_WithNullBadgeId_Returns400BadRequest() throws Exception {
        // Arrange
        validRequest.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_WithEmptyBadgeId_Returns400BadRequest() throws Exception {
        // Arrange
        validRequest.setBadgeId("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_WithInvalidEmail_Returns400BadRequest() throws Exception {
        // Arrange
        validRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_WithWorkerRole_Returns403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    void testCreateEmployee_WithoutAuthentication_Returns401Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_WithValidId_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong())).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_WithNonExistentId_Returns404NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong())).thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(999L);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_WithInvalidId_Returns400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getEmployeeById(anyLong());
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_WithPagination_Returns200OK() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(validResponse);
        Page<EmployeeResponseDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_WithInvalidPageNumber_Returns400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_WithInvalidPageSize_Returns400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getAllEmployees(any());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_WithValidData_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class))).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_WithNonExistentId_Returns404NotFound() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testUpdateEmployee_WithWorkerRole_Returns403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeRequestDTO.class));
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_WithValidId_Returns204NoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_WithNonExistentId_Returns404NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found")).when(employeeService).deleteEmployee(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployee(999L);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testDeleteEmployee_WithHRRole_Returns403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    // ==================== GET EMPLOYEE BY BADGE ID TESTS ====================

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testGetEmployeeByBadgeId_WithValidBadgeId_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId(anyString())).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/badge/EMP001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).getEmployeeByBadgeId("EMP001");
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testGetEmployeeByBadgeId_WithNonExistentBadgeId_Returns404NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId(anyString()))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/badge/INVALID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeByBadgeId("INVALID");
    }

    // ==================== SEARCH EMPLOYEES TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    void testSearchEmployeesByDepartment_WithValidDepartment_Returns200OK() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(validResponse);
        when(employeeService.searchEmployeesByDepartment(anyString())).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search/department")
                .param("department", "Warehouse")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Warehouse"));

        verify(employeeService, times(1)).searchEmployeesByDepartment("Warehouse");
    }

    @Test
    @WithMockUser(roles = "HR")
    void testSearchEmployeesByRole_WithValidRole_Returns200OK() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(validResponse);
        when(employeeService.searchEmployeesByRole(anyString())).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search/role")
                .param("role", "WORKER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("WORKER"));

        verify(employeeService, times(1)).searchEmployeesByRole("WORKER");
    }