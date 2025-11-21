package com.warehouse.ems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.dto.EmployeeDTO;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.entity.Role;
import com.warehouse.ems.exception.DuplicateBadgeIdException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.service.EmployeeService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests cover REST API endpoints, HTTP status codes, and request/response handling
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;
    private Role testRole;

    @BeforeEach
    public void setUp() {
        // Arrange - Set up test data
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("WORKER");

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setRole(testRole);
        testEmployee.setDepartment("Logistics");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");

        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setFirstName("John");
        testEmployeeDTO.setLastName("Doe");
        testEmployeeDTO.setEmail("john.doe@warehouse.com");
        testEmployeeDTO.setRoleId(1L);
        testEmployeeDTO.setDepartment("Logistics");
        testEmployeeDTO.setShiftGroup("A");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_ValidRequest_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@warehouse.com")));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_InvalidRequest_Returns400() throws Exception {
        // Arrange - Create invalid DTO with missing required fields
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setBadgeId(""); // Empty badgeId

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_DuplicateBadgeId_Returns409() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
                .thenThrow(new DuplicateBadgeIdException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_NullRequestBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_MalformedJSON_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    public void testGetEmployee_ExistingId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.firstName", is("John")));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetEmployee_NonExistingId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetEmployee_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetEmployee_NegativeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(-1L))
                .thenThrow(new IllegalArgumentException("Invalid ID"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllEmployees_WithPagination_Returns200() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].badgeId", is("EMP001")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllEmployees_EmptyResult_Returns200() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllEmployees_InvalidPageNumber_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllEmployees_InvalidPageSize_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_ValidRequest_Returns200() throws Exception {
        // Arrange
        testEmployeeDTO.setFirstName("Jane");
        testEmployee.setFirstName("Jane");
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Jane")));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_NonExistingId_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_InvalidRequest_Returns400() throws Exception {
        // Arrange
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setBadgeId("");

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_NullRequestBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_ExistingId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_NonExistingId_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/invalid")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== SEARCH AND FILTER TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    public void testSearchEmployeesByDepartment_ValidDepartment_Returns200() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeService.findByDepartment("Logistics")).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/employees/search")
                .param("department", "Logistics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].department", is("Logistics")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testSearchEmployeesByBadgeId_ValidBadgeId_Returns200() throws Exception {
        // Arrange
        when(employeeService.findByBadgeId("EMP001")).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(get("/api/employees/badge/EMP001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("EMP001")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testSearchEmployeesByBadgeId_NonExistingBadgeId_Returns404() throws Exception {
        // Arrange
        when(employeeService.findByBadgeId("INVALID"))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/badge/INVALID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ========== AUTHORIZATION TESTS ==========

    @Test
    public void testCreateEmployee_Unauthorized_Returns401() throws Exception {
        // Act & Assert - No @WithMockUser annotation
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testCreateEmployee_Forbidden_Returns403() throws Exception {
        // Act & Assert - USER role doesn't have permission to create
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testDeleteEmployee_Forbidden_Returns403() throws Exception {
        // Act & Assert - USER role doesn't have permission to delete
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}