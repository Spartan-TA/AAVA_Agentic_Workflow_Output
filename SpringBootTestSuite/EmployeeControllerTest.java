package com.warehouse.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Comprehensive unit tests for EmployeeController.
 * Tests cover all REST endpoints, security, authentication, and HTTP status codes.
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Tests")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private Employee testEmployee;
    private LocalDate testHireDate;

    @BeforeEach
    public void setUp() {
        testHireDate = LocalDate.of(2023, 1, 15);
        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(testHireDate)
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ========== GET ALL EMPLOYEES - NORMAL CASES ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees with ADMIN role returns 200")
    public void testGetAllEmployees_WithAdminRole_Returns200() throws Exception {
        // Arrange
        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[0].badgeId").value("BADGE001"));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test GET /api/employees with HR role returns 200")
    public void testGetAllEmployees_WithHRRole_Returns200() throws Exception {
        // Arrange
        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test GET /api/employees with SUPERVISOR role returns 200")
    public void testGetAllEmployees_WithSupervisorRole_Returns200() throws Exception {
        // Arrange
        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test GET /api/employees with WORKER role returns 403")
    public void testGetAllEmployees_WithWorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).getAllEmployees(any());
    }

    @Test
    @DisplayName("Test GET /api/employees without authentication returns 401")
    public void testGetAllEmployees_WithoutAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees with empty result returns 200")
    public void testGetAllEmployees_WithEmptyResult_Returns200() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ========== GET EMPLOYEE BY ID - NORMAL CASES ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} with existing ID returns 200")
    public void testGetEmployee_WithExistingId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L)).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("BADGE001"));

        verify(employeeService, times(1)).getEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test GET /api/employees/{id} with WORKER role returns 200")
    public void testGetEmployee_WithWorkerRole_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L)).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} with non-existing ID returns 404")
    public void testGetEmployee_WithNonExistingId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployee(999L);
    }

    @Test
    @DisplayName("Test GET /api/employees/{id} without authentication returns 401")
    public void testGetEmployee_WithoutAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).getEmployee(anyLong());
    }

    // ========== CREATE EMPLOYEE - NORMAL CASES ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees with ADMIN role returns 201")
    public void testCreateEmployee_WithAdminRole_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test POST /api/employees with HR role returns 201")
    public void testCreateEmployee_WithHRRole_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test POST /api/employees with SUPERVISOR role returns 403")
    public void testCreateEmployee_WithSupervisorRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test POST /api/employees with WORKER role returns 403")
    public void testCreateEmployee_WithWorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees with duplicate badgeId returns 400")
    public void testCreateEmployee_WithDuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Test POST /api/employees without authentication returns 401")
    public void testCreateEmployee_WithoutAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    // ========== UPDATE EMPLOYEE - NORMAL CASES ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/employees/{id} with ADMIN role returns 200")
    public void testUpdateEmployee_WithAdminRole_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test PUT /api/employees/{id} with HR role returns 200")
    public void testUpdateEmployee_WithHRRole_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test PUT /api/employees/{id} with SUPERVISOR role returns 403")
    public void testUpdateEmployee_WithSupervisorRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/employees/{id} with non-existing ID returns 404")
    public void testUpdateEmployee_WithNonExistingId_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(999L), any(Employee.class)))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(eq(999L), any(Employee.class));
    }

    @Test
    @DisplayName("Test PUT /api/employees/{id} without authentication returns 401")
    public void testUpdateEmployee_WithoutAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).updateEmployee(anyLong(), any(Employee.class));
    }

    // ========== DELETE EMPLOYEE - NORMAL CASES ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test DELETE /api/employees/{id} with ADMIN role returns 204")
    public void testDeleteEmployee_WithAdminRole_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test DELETE /api/employees/{id} with HR role returns 204")
    public void testDeleteEmployee_WithHRRole_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test DELETE /api/employees/{id} with SUPERVISOR role returns 403")
    public void testDeleteEmployee_WithSupervisorRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test DELETE /api/employees/{id} with WORKER role returns 403")
    public void testDeleteEmployee_WithWorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test DELETE /api/employees/{id} with non-existing ID returns 404")
    public void testDeleteEmployee_WithNonExistingId_Returns404() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Employee not found")).when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployee(999L);
    }

    @Test
    @DisplayName("Test DELETE /api/employees/{id} without authentication returns 401")
    public void testDeleteEmployee_WithoutAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    // ========== EDGE CASES ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees with invalid JSON returns 400")
    public void testCreateEmployee_WithInvalidJson_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees with empty body returns 400")
    public void testCreateEmployee_WithEmptyBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} with invalid ID format returns 400")
    public void testGetEmployee_WithInvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/employees/{id} with mismatched ID in body")
    public void testUpdateEmployee_WithMismatchedId_UpdatesCorrectly() throws Exception {
        // Arrange
        Employee mismatchedEmployee = Employee.builder()
                .id(999L) // Different from path parameter
                .name("Updated Name")
                .badgeId("BADGE001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(testHireDate)
                .status("ACTIVE")
                .deleted(false)
                .build();

        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mismatchedEmployee)))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(Employee.class));
    }

    // ========== BOUNDARY CONDITIONS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} with zero ID")
    public void testGetEmployee_WithZeroId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(0L)).thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} with negative ID")
    public void testGetEmployee_WithNegativeId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(-1L)).thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} with maximum Long value")
    public void testGetEmployee_WithMaxLongValue_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(Long.MAX_VALUE))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/" + Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees with large page size")
    public void testGetAllEmployees_WithLargePageSize_Returns200() throws Exception {
        // Arrange
        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees with page size 1")
    public void testGetAllEmployees_WithPageSize1_Returns200() throws Exception {
        // Arrange
        Page<Employee> page = new PageImpl<>(Arrays.asList(testEmployee), PageRequest.of(0, 1), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees with very long name")
    public void testCreateEmployee_WithVeryLongName_Returns201() throws Exception {
        // Arrange
        String longName = "A".repeat(500);
        testEmployee.setName(longName);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(longName));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees with special characters in name")
    public void testCreateEmployee_WithSpecialCharactersInName_Returns201() throws Exception {
        // Arrange
        testEmployee.setName("John O'Brien-Smith Jr.");
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John O'Brien-Smith Jr."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees with future hire date")
    public void testCreateEmployee_WithFutureHireDate_Returns201() throws Exception {
        // Arrange
        testEmployee.setHireDate(LocalDate.now().plusYears(1));
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees with very old hire date")
    public void testCreateEmployee_WithVeryOldHireDate_Returns201() throws Exception {
        // Arrange
        testEmployee.setHireDate(LocalDate.of(1950, 1, 1));
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isCreated());
    }
}
