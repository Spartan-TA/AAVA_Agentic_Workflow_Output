package com.warehouse.employeemgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employeemgmt.domain.Employee;
import com.warehouse.employeemgmt.domain.EmployeeRole;
import com.warehouse.employeemgmt.domain.EmployeeStatus;
import com.warehouse.employeemgmt.dto.EmployeeDTO;
import com.warehouse.employeemgmt.exception.ResourceNotFoundException;
import com.warehouse.employeemgmt.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests all REST API endpoints with security, validation, and HTTP status codes
 * 
 * Test Coverage:
 * - All HTTP methods (GET, POST, PUT, PATCH, DELETE)
 * - Request validation
 * - Response status codes (200, 201, 204, 400, 401, 403, 404)
 * - JSON serialization/deserialization
 * - Pagination parameters
 * - Security/authorization
 * - Error responses
 * - Content negotiation
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Test Suite")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    public void setUp() {
        // Arrange - Setup test data
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole(EmployeeRole.WORKER);
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDeleted(false);

        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Morning");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
    }

    // ==================== CREATE EMPLOYEE TESTS (POST) ====================

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with valid input returns 201")
    public void testCreateEmployee_ValidInput_Returns201() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.role", is("WORKER")))
                .andExpect(header().exists("Location"));

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with null name returns 400")
    public void testCreateEmployee_NullName_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("name"))));

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with empty name returns 400")
    public void testCreateEmployee_EmptyName_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with null badge ID returns 400")
    public void testCreateEmployee_NullBadgeId_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("badgeId"))));

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with duplicate badge ID returns 400")
    public void testCreateEmployee_DuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Badge ID")));

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with invalid role returns 400")
    public void testCreateEmployee_InvalidRole_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setRole("INVALID_ROLE");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with future hire date returns 400")
    public void testCreateEmployee_FutureHireDate_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("hireDate"))));

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    @DisplayName("POST /api/v1/employees - Create employee with WORKER role returns 403")
    public void testCreateEmployee_WorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/employees - Create employee without authentication returns 401")
    public void testCreateEmployee_NoAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with invalid JSON returns 400")
    public void testCreateEmployee_InvalidJson_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    @DisplayName("GET /api/v1/employees/{id} - Get employee by valid ID returns 200")
    public void testGetEmployeeById_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getById(anyLong())).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.badgeId", is("EMP001")));

        verify(employeeService, times(1)).getById(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees/{id} - Get employee by non-existent ID returns 404")
    public void testGetEmployeeById_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));

        verify(employeeService, times(1)).getById(999L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees/{id} - Get employee with invalid ID format returns 400")
    public void testGetEmployeeById_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getById(anyLong());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees/{id} - Get employee with negative ID returns 400")
    public void testGetEmployeeById_NegativeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.getById(anyLong()))
                .thenThrow(new IllegalArgumentException("ID must be positive"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).getById(-1L);
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees - Get all employees with pagination returns 200")
    public void testGetAllEmployees_WithPagination_Returns200() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("John Doe")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees - Get all employees with default pagination returns 200")
    public void testGetAllEmployees_DefaultPagination_Returns200() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees - Get all employees with invalid page number returns 400")
    public void testGetAllEmployees_InvalidPageNumber_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees - Get all employees with invalid page size returns 400")
    public void testGetAllEmployees_InvalidPageSize_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees - Get all employees empty result returns 200")
    public void testGetAllEmployees_EmptyResult_Returns200() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    // ==================== UPDATE EMPLOYEE TESTS (PUT) ====================

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("PUT /api/v1/employees/{id} - Update employee with valid input returns 200")
    public void testUpdateEmployee_ValidInput_Returns200() throws Exception {
        // Arrange
        testEmployee.setName("Jane Doe");
        when(employeeService.update(anyLong(), any(EmployeeDTO.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Jane Doe")));

        verify(employeeService, times(1)).update(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("PUT /api/v1/employees/{id} - Update employee with non-existent ID returns 404")
    public void testUpdateEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.update(anyLong(), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).update(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("PUT /api/v1/employees/{id} - Update employee with invalid data returns 400")
    public void testUpdateEmployee_InvalidData_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setName("");

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).update(anyLong(), any(EmployeeDTO.class));
    }

    // ==================== PARTIAL UPDATE EMPLOYEE TESTS (PATCH) ====================

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("PATCH /api/v1/employees/{id} - Partial update employee returns 200")
    public void testPartialUpdateEmployee_ValidInput_Returns200() throws Exception {
        // Arrange
        testEmployee.setName("Updated Name");
        when(employeeService.partialUpdate(anyLong(), any(EmployeeDTO.class))).thenReturn(testEmployee);
        EmployeeDTO partialDTO = new EmployeeDTO();
        partialDTO.setName("Updated Name");

        // Act & Assert
        mockMvc.perform(patch("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));

        verify(employeeService, times(1)).partialUpdate(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("PATCH /api/v1/employees/{id} - Partial update with non-existent ID returns 404")
    public void testPartialUpdateEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.partialUpdate(anyLong(), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));
        EmployeeDTO partialDTO = new EmployeeDTO();
        partialDTO.setName("Updated Name");

        // Act & Assert
        mockMvc.perform(patch("/api/v1/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialDTO)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).partialUpdate(anyLong(), any(EmployeeDTO.class));
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/employees/{id} - Soft delete employee returns 204")
    public void testSoftDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).softDelete(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).softDelete(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/employees/{id} - Delete non-existent employee returns 404")
    public void testSoftDeleteEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
                .when(employeeService).softDelete(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).softDelete(999L);
    }

    @Test
    @WithMockUser(roles = {"HR"})
    @DisplayName("DELETE /api/v1/employees/{id} - Delete with HR role returns 403")
    public void testSoftDeleteEmployee_HRRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).softDelete(anyLong());
    }

    // ==================== FILTER/SEARCH TESTS ====================

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees/search - Search employees by name returns 200")
    public void testSearchEmployees_ByName_Returns200() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeService.searchByName(anyString(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search")
                .param("name", "John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", containsString("John")));

        verify(employeeService, times(1)).searchByName(anyString(), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees/filter - Filter employees by department returns 200")
    public void testFilterEmployees_ByDepartment_Returns200() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeService.filterByDepartment(anyString(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/filter")
                .param("department", "Warehouse")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].department", is("Warehouse")));

        verify(employeeService, times(1)).filterByDepartment(anyString(), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees/filter - Filter employees by role returns 200")
    public void testFilterEmployees_ByRole_Returns200() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeService.filterByRole(any(EmployeeRole.class), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/filter")
                .param("role", "WORKER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(employeeService, times(1)).filterByRole(any(EmployeeRole.class), any());
    }

    // ==================== CONTENT NEGOTIATION TESTS ====================

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees/{id} - Accept JSON returns JSON")
    public void testGetEmployee_AcceptJson_ReturnsJson() throws Exception {
        // Arrange
        when(employeeService.getById(anyLong())).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(employeeService, times(1)).getById(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Unsupported media type returns 415")
    public void testCreateEmployee_UnsupportedMediaType_Returns415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_XML)
                .content("<employee></employee>"))
                .andExpect(status().isUnsupportedMediaType());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with very long name (boundary)")
    public void testCreateEmployee_VeryLongName_Returns201() throws Exception {
        // Arrange
        String longName = "A".repeat(255);
        testEmployeeDTO.setName(longName);
        testEmployee.setName(longName);
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("POST /api/v1/employees - Create employee with special characters in name")
    public void testCreateEmployee_SpecialCharactersInName_Returns201() throws Exception {
        // Arrange
        testEmployeeDTO.setName("O'Brien-Smith");
        testEmployee.setName("O'Brien-Smith");
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("O'Brien-Smith")));

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees - Large page size (1000) returns 200")
    public void testGetAllEmployees_LargePageSize_Returns200() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("GET /api/v1/employees - Minimum page size (1) returns 200")
    public void testGetAllEmployees_MinimumPageSize_Returns200() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees(any());
    }
}