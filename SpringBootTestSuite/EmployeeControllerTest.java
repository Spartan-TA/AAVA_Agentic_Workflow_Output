package com.companyname.wems.employee.controller;

import com.companyname.wems.employee.dto.EmployeeRequest;
import com.companyname.wems.employee.dto.EmployeeResponse;
import com.companyname.wems.employee.entity.Employee;
import com.companyname.wems.employee.service.EmployeeService;
import com.companyname.wems.exception.DuplicateResourceException;
import com.companyname.wems.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Tests cover REST endpoints, security, validation, and HTTP responses
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeRequest validRequest;
    private EmployeeResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = EmployeeRequest.builder()
                .name("John Doe")
                .badgeId("EMP12345")
                .role(Employee.Role.WORKER)
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .build();

        validResponse = EmployeeResponse.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP12345")
                .role(Employee.Role.WORKER)
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status(Employee.Status.ACTIVE)
                .build();
    }

    // ========== CREATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees - Should create employee with valid input as ADMIN")
    void testCreateEmployee_ValidInputAsAdmin_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.badgeId", is("EMP12345")))
                .andExpect(jsonPath("$.role", is("WORKER")))
                .andExpect(jsonPath("$.department", is("Shipping")));

        verify(employeeService, times(1)).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("POST /api/v1/employees - Should create employee with valid input as HR")
    void testCreateEmployee_ValidInputAsHR_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("POST /api/v1/employees - Should return 403 when SUPERVISOR tries to create employee")
    void testCreateEmployee_AsSupervisor_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /api/v1/employees - Should return 403 when WORKER tries to create employee")
    void testCreateEmployee_AsWorker_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/employees - Should return 401 when unauthenticated")
    void testCreateEmployee_Unauthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees - Should return 400 when name is null")
    void testCreateEmployee_NullName_Returns400() throws Exception {
        // Arrange
        validRequest.setName(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasKey("name")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees - Should return 400 when name is empty")
    void testCreateEmployee_EmptyName_Returns400() throws Exception {
        // Arrange
        validRequest.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees - Should return 400 when badge ID is null")
    void testCreateEmployee_NullBadgeId_Returns400() throws Exception {
        // Arrange
        validRequest.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees - Should return 400 when badge ID has invalid format")
    void testCreateEmployee_InvalidBadgeIdFormat_Returns400() throws Exception {
        // Arrange
        validRequest.setBadgeId("invalid-badge");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees - Should return 409 when badge ID already exists")
    void testCreateEmployee_DuplicateBadgeId_Returns409() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new DuplicateResourceException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Badge ID already exists")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees - Should return 400 when role is null")
    void testCreateEmployee_NullRole_Returns400() throws Exception {
        // Arrange
        validRequest.setRole(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees - Should return 400 when hire date is in future")
    void testCreateEmployee_FutureHireDate_Returns400() throws Exception {
        // Arrange
        validRequest.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== GET EMPLOYEE BY ID ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees/{id} - Should retrieve employee by valid ID")
    void testGetEmployeeById_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.badgeId", is("EMP12345")));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("GET /api/v1/employees/{id} - Should allow HR to retrieve employee")
    void testGetEmployeeById_AsHR_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("GET /api/v1/employees/{id} - Should allow SUPERVISOR to retrieve employee")
    void testGetEmployeeById_AsSupervisor_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("GET /api/v1/employees/{id} - Should return 403 when WORKER tries to retrieve employee")
    void testGetEmployeeById_AsWorker_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees/{id} - Should return 404 when employee not found")
    void testGetEmployeeById_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Employee not found")));
    }

    // ========== LIST EMPLOYEES ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees - Should list all employees with pagination")
    void testListEmployees_WithPagination_Returns200() throws Exception {
        // Arrange
        List<EmployeeResponse> employees = Arrays.asList(validResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.listEmployees(any(), any(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("John Doe")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees - Should filter by department")
    void testListEmployees_FilterByDepartment_Returns200() throws Exception {
        // Arrange
        List<EmployeeResponse> employees = Arrays.asList(validResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees);
        when(employeeService.listEmployees(any(), eq("Shipping"), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                        .param("department", "Shipping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department", is("Shipping")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees - Should filter by status")
    void testListEmployees_FilterByStatus_Returns200() throws Exception {
        // Arrange
        List<EmployeeResponse> employees = Arrays.asList(validResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees);
        when(employeeService.listEmployees(any(), any(), eq(Employee.Status.ACTIVE))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status", is("ACTIVE")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees - Should return empty page when no employees match")
    void testListEmployees_NoMatches_ReturnsEmptyPage() throws Exception {
        // Arrange
        Page<EmployeeResponse> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.listEmployees(any(), any(), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    // ========== UPDATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/employees/{id} - Should update employee with valid input")
    void testUpdateEmployee_ValidInput_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequest.class)))
                .thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("PUT /api/v1/employees/{id} - Should allow HR to update employee")
    void testUpdateEmployee_AsHR_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequest.class)))
                .thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("PUT /api/v1/employees/{id} - Should return 403 when SUPERVISOR tries to update")
    void testUpdateEmployee_AsSupervisor_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/employees/{id} - Should return 404 when employee not found")
    void testUpdateEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    // ========== DELETE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/employees/{id} - Should soft delete employee")
    void testDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("DELETE /api/v1/employees/{id} - Should allow HR to delete employee")
    void testDeleteEmployee_AsHR_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("DELETE /api/v1/employees/{id} - Should return 403 when SUPERVISOR tries to delete")
    void testDeleteEmployee_AsSupervisor_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/employees/{id} - Should return 404 when employee not found")
    void testDeleteEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ========== SEARCH EMPLOYEES ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees/search - Should search employees by name")
    void testSearchEmployees_ByName_Returns200() throws Exception {
        // Arrange
        List<EmployeeResponse> employees = Arrays.asList(validResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees);
        when(employeeService.searchEmployees(anyString(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search")
                        .param("query", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", containsString("John")));
    }

    // ========== COUNT EMPLOYEES ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees/count/department/{department} - Should count employees by department")
    void testCountEmployeesByDepartment_Returns200() throws Exception {
        // Arrange
        when(employeeService.countEmployeesByDepartment("Shipping")).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/count/department/Shipping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(5)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees/count/status/{status} - Should count employees by status")
    void testCountEmployeesByStatus_Returns200() throws Exception {
        // Arrange
        when(employeeService.countEmployeesByStatus(Employee.Status.ACTIVE)).thenReturn(10L);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/count/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(10)));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees - Should handle malformed JSON")
    void testCreateEmployee_MalformedJson_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees - Should handle invalid page number")
    void testListEmployees_InvalidPageNumber_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/employees - Should handle invalid page size")
    void testListEmployees_InvalidPageSize_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}