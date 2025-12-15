package com.warehouse.ems.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.employee.dto.EmployeeRequestDTO;
import com.warehouse.ems.employee.dto.EmployeeResponseDTO;
import com.warehouse.ems.employee.service.EmployeeService;
import com.warehouse.ems.exception.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test class for EmployeeController.
 * Tests all REST endpoints using MockMvc, including security, validation, and error handling.
 * Uses @WebMvcTest for controller layer testing with mocked service layer.
 *
 * @author Automation Test Engineer
 * @version 1.0
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeRequestDTO testRequestDTO;
    private EmployeeResponseDTO testResponseDTO;

    /**
     * Setup method to initialize test data before each test.
     */
    @BeforeEach
    public void setUp() {
        // Arrange - Create test request DTO
        testRequestDTO = EmployeeRequestDTO.builder()
                .name("John Doe")
                .badgeId("EMP001")
                .email("john.doe@warehouse.com")
                .phone("+1-555-0100")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        // Arrange - Create test response DTO
        testResponseDTO = EmployeeResponseDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .email("john.doe@warehouse.com")
                .phone("+1-555-0100")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("admin")
                .updatedBy("admin")
                .build();
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    /**
     * Test creating an employee with valid input and HR role.
     * Expected: 201 Created status with employee data.
     */
    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_WithValidInput_Returns201Created() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(testResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.email", is("john.doe@warehouse.com")));

        verify(employeeService, times(1)).createEmployee(any(EmployeeRequestDTO.class));
    }

    /**
     * Test creating an employee with invalid input (missing required fields).
     * Expected: 400 Bad Request status.
     */
    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_WithInvalidInput_Returns400BadRequest() throws Exception {
        // Arrange - Create invalid request with missing name
        testRequestDTO.setName(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    /**
     * Test creating an employee with missing required fields.
     * Expected: 400 Bad Request status with validation errors.
     */
    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_WithMissingRequiredFields_Returns400BadRequest() throws Exception {
        // Arrange - Create request with multiple missing fields
        EmployeeRequestDTO invalidRequest = EmployeeRequestDTO.builder().build();

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    /**
     * Test creating an employee without authorization (no HR/ADMIN role).
     * Expected: 403 Forbidden status.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployee_WithoutAuthorization_Returns403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    /**
     * Test creating an employee with invalid email format.
     * Expected: 400 Bad Request status.
     */
    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_WithInvalidEmail_Returns400BadRequest() throws Exception {
        // Arrange
        testRequestDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    /**
     * Test retrieving all employees with pagination.
     * Expected: 200 OK status with paged results.
     */
    @Test
    @WithMockUser
    public void testGetAllEmployees_ReturnsPagedResults_Returns200OK() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(testResponseDTO);
        Page<EmployeeResponseDTO> page = new PageImpl<>(employees, PageRequest.of(0, 10), employees.size());
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("John Doe")));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    /**
     * Test retrieving all employees when database is empty.
     * Expected: 200 OK status with empty page.
     */
    @Test
    @WithMockUser
    public void testGetAllEmployees_WithEmptyDatabase_Returns200OK() throws Exception {
        // Arrange
        Page<EmployeeResponseDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    /**
     * Test retrieving an employee by valid ID.
     * Expected: 200 OK status with employee data.
     */
    @Test
    @WithMockUser
    public void testGetEmployeeById_WithValidId_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    /**
     * Test retrieving an employee with invalid/non-existent ID.
     * Expected: 404 Not Found status.
     */
    @Test
    @WithMockUser
    public void testGetEmployeeById_WithInvalidId_Returns404NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(999L);
    }

    // ==================== GET EMPLOYEE BY BADGE ID TESTS ====================

    /**
     * Test retrieving an employee by valid badgeId.
     * Expected: 200 OK status with employee data.
     */
    @Test
    @WithMockUser
    public void testGetEmployeeByBadgeId_WithValidBadgeId_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(testResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/badge/EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("EMP001")));

        verify(employeeService, times(1)).getEmployeeByBadgeId("EMP001");
    }

    /**
     * Test retrieving an employee with invalid badgeId.
     * Expected: 404 Not Found status.
     */
    @Test
    @WithMockUser
    public void testGetEmployeeByBadgeId_WithInvalidBadgeId_Returns404NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId("INVALID"))
                .thenThrow(new ResourceNotFoundException("Employee", "badgeId", "INVALID"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/badge/INVALID"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeByBadgeId("INVALID");
    }

    // ==================== SEARCH EMPLOYEES TESTS ====================

    /**
     * Test searching employees with filters.
     * Expected: 200 OK status with filtered results.
     */
    @Test
    @WithMockUser
    public void testSearchEmployees_WithFilters_Returns200OK() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(testResponseDTO);
        when(employeeService.searchEmployees(anyString(), anyString(), anyString())).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/employees/search")
                        .param("name", "John")
                        .param("department", "Shipping")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")));

        verify(employeeService, times(1)).searchEmployees("John", "Shipping", "ACTIVE");
    }

    /**
     * Test searching employees with no results.
     * Expected: 200 OK status with empty list.
     */
    @Test
    @WithMockUser
    public void testSearchEmployees_WithNoResults_ReturnsEmptyList_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.searchEmployees(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/employees/search")
                        .param("name", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(employeeService, times(1)).searchEmployees(anyString(), any(), any());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    /**
     * Test updating an employee with valid input and HR role.
     * Expected: 200 OK status with updated employee data.
     */
    @Test
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_WithValidInput_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                .thenReturn(testResponseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeRequestDTO.class));
    }

    /**
     * Test updating an employee with invalid ID.
     * Expected: 404 Not Found status.
     */
    @Test
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_WithInvalidId_Returns404NotFound() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeRequestDTO.class));
    }

    /**
     * Test updating an employee with invalid input.
     * Expected: 400 Bad Request status.
     */
    @Test
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_WithInvalidInput_Returns400BadRequest() throws Exception {
        // Arrange
        testRequestDTO.setName(null);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeRequestDTO.class));
    }

    /**
     * Test updating an employee without authorization.
     * Expected: 403 Forbidden status.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    public void testUpdateEmployee_WithoutAuthorization_Returns403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeRequestDTO.class));
    }

    // ==================== UPDATE EMPLOYEE STATUS TESTS ====================

    /**
     * Test updating employee status with valid status and SUPERVISOR role.
     * Expected: 200 OK status.
     */
    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testUpdateEmployeeStatus_WithValidStatus_Returns200OK() throws Exception {
        // Arrange
        doNothing().when(employeeService).updateEmployeeStatus(anyLong(), anyString());

        // Act & Assert
        mockMvc.perform(patch("/api/employees/1/status")
                        .with(csrf())
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).updateEmployeeStatus(1L, "INACTIVE");
    }

    /**
     * Test updating employee status with invalid status.
     * Expected: 400 Bad Request status.
     */
    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testUpdateEmployeeStatus_WithInvalidStatus_Returns400BadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid status"))
                .when(employeeService).updateEmployeeStatus(anyLong(), anyString());

        // Act & Assert
        mockMvc.perform(patch("/api/employees/1/status")
                        .with(csrf())
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).updateEmployeeStatus(anyLong(), anyString());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    /**
     * Test deleting an employee with valid ID and ADMIN role.
     * Expected: 204 No Content status.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_WithValidId_Returns204NoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    /**
     * Test deleting an employee with invalid ID.
     * Expected: 404 Not Found status.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_WithInvalidId_Returns404NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee", "id", 999L))
                .when(employeeService).deleteEmployee(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployee(999L);
    }

    /**
     * Test deleting an employee without ADMIN role.
     * Expected: 403 Forbidden status.
     */
    @Test
    @WithMockUser(roles = "HR")
    public void testDeleteEmployee_WithoutAdminRole_Returns403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }
}