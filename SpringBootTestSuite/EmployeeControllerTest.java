package com.warehouse.ems.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.employee.dto.EmployeeDTO;
import com.warehouse.ems.employee.service.EmployeeService;
import com.warehouse.ems.exception.NotFoundException;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests cover REST endpoints, security, validation, and error handling
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        testEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();
    }

    // ========== CREATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_ValidInput_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.badgeId", is("EMP001")));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_EmptyName_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_NullBadgeId_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidRole_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setRole("INVALID_ROLE");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_InsufficientPermissions_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateEmployee_Unauthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MalformedJSON_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // ========== GET EMPLOYEE BY ID ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong())).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong())).thenThrow(new NotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testGetEmployeeById_WorkerAccessingOwnRecord_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong())).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ========== GET ALL EMPLOYEES ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_NoFilters_Returns200() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any(), any(), any(), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("John Doe")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_WithDepartmentFilter_Returns200() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any(), eq("Warehouse"), any(), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "10")
                        .param("department", "Warehouse")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_WithStatusFilter_Returns200() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any(), any(), eq("ACTIVE"), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_EmptyResult_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(employeeService.getAllEmployees(any(), any(), any(), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_InvalidPageNumber_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_InvalidPageSize_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== UPDATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_ValidInput_Returns200() throws Exception {
        // Arrange
        EmployeeDTO updatedDTO = EmployeeDTO.builder()
                .id(1L)
                .name("Jane Doe")
                .department("Logistics")
                .build();
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class))).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Doe")));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class)))
                .thenThrow(new NotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testUpdateEmployee_InsufficientPermissions_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_EmptyRequestBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ========== DELETE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        doThrow(new NotFoundException("Employee not found")).when(employeeService).deleteEmployee(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testDeleteEmployee_HRRole_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testDeleteEmployee_InsufficientPermissions_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/invalid")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== GET EMPLOYEE BY BADGE ID ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeByBadgeId_ValidBadgeId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId(anyString())).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/badge/EMP001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("EMP001")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeByBadgeId_NonExistentBadgeId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId(anyString()))
                .thenThrow(new NotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/badge/INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeByBadgeId_EmptyBadgeId_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/badge/ ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}