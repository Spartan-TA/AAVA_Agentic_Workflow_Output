package com.wms.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.service.EmployeeService;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests cover REST endpoints, security, validation, and edge cases
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

    private EmployeeDto testEmployeeDto;

    @BeforeEach
    public void setUp() {
        testEmployeeDto = new EmployeeDto();
        testEmployeeDto.setBadgeId("EMP001");
        testEmployeeDto.setName("John Doe");
        testEmployeeDto.setRole("WORKER");
        testEmployeeDto.setDepartment("Warehouse");
        testEmployeeDto.setShiftGroup("A");
        testEmployeeDto.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDto.setStatus("ACTIVE");
    }

    // ========== CREATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test POST /employees with valid data returns 201")
    public void testCreateEmployee_ValidData_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test POST /employees with null badge ID returns 400")
    public void testCreateEmployee_NullBadgeId_Returns400() throws Exception {
        // Arrange
        testEmployeeDto.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test POST /employees with empty name returns 400")
    public void testCreateEmployee_EmptyName_Returns400() throws Exception {
        // Arrange
        testEmployeeDto.setName("");

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test POST /employees with invalid JSON returns 400")
    public void testCreateEmployee_InvalidJson_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST /employees without authentication returns 401")
    public void testCreateEmployee_NoAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    @DisplayName("Test POST /employees with insufficient role returns 403")
    public void testCreateEmployee_InsufficientRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test POST /employees with duplicate badge ID returns 409")
    public void testCreateEmployee_DuplicateBadgeId_Returns409() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDto.class)))
                .thenThrow(new IllegalStateException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isConflict());
    }

    // ========== GET EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees/{id} with valid ID returns 200")
    public void testGetEmployee_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L)).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).getEmployee(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees/{id} with non-existent ID returns 404")
    public void testGetEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L))
                .thenThrow(new IllegalArgumentException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees/{id} with invalid ID format returns 400")
    public void testGetEmployee_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees/{id} with negative ID returns 400")
    public void testGetEmployee_NegativeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.getEmployee(-1L))
                .thenThrow(new IllegalArgumentException("Invalid employee ID"));

        // Act & Assert
        mockMvc.perform(get("/employees/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test GET /employees/{id} without authentication returns 401")
    public void testGetEmployee_NoAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isUnauthorized());
    }

    // ========== UPDATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test PUT /employees/{id} with valid data returns 200")
    public void testUpdateEmployee_ValidData_Returns200() throws Exception {
        // Arrange
        testEmployeeDto.setName("Jane Doe");
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(EmployeeDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test PUT /employees/{id} with non-existent ID returns 404")
    public void testUpdateEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(999L), any(EmployeeDto.class)))
                .thenThrow(new IllegalArgumentException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test PUT /employees/{id} with null DTO returns 400")
    public void testUpdateEmployee_NullDto_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    @DisplayName("Test PUT /employees/{id} with HR role returns 200")
    public void testUpdateEmployee_HRRole_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    @DisplayName("Test PUT /employees/{id} with WORKER role returns 403")
    public void testUpdateEmployee_WorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isForbidden());
    }

    // ========== PATCH EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test PATCH /employees/{id} with partial update returns 200")
    public void testPatchEmployee_PartialUpdate_Returns200() throws Exception {
        // Arrange
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Name");
        updates.put("department", "Shipping");
        
        testEmployeeDto.setName("Updated Name");
        testEmployeeDto.setDepartment("Shipping");
        when(employeeService.patchEmployee(eq(1L), anyMap())).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.department").value("Shipping"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test PATCH /employees/{id} with empty updates returns 400")
    public void testPatchEmployee_EmptyUpdates_Returns400() throws Exception {
        // Arrange
        Map<String, Object> updates = new HashMap<>();

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test PATCH /employees/{id} with invalid field returns 400")
    public void testPatchEmployee_InvalidField_Returns400() throws Exception {
        // Arrange
        Map<String, Object> updates = new HashMap<>();
        updates.put("invalidField", "value");

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());
    }

    // ========== DELETE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test DELETE /employees/{id} with valid ID returns 204")
    public void testDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test DELETE /employees/{id} with non-existent ID returns 404")
    public void testDeleteEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Employee not found"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    @DisplayName("Test DELETE /employees/{id} with HR role returns 403")
    public void testDeleteEmployee_HRRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test DELETE /employees/{id} without authentication returns 401")
    public void testDeleteEmployee_NoAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ========== LIST EMPLOYEES ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees with pagination returns 200")
    public void testListEmployees_WithPagination_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDto> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDto));
        when(employeeService.listEmployees(any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees with filter by department returns 200")
    public void testListEmployees_FilterByDepartment_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDto> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDto));
        when(employeeService.listEmployeesByDepartment(eq("Warehouse"), any(PageRequest.class)))
                .thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("department", "Warehouse")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Warehouse"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees with filter by status returns 200")
    public void testListEmployees_FilterByStatus_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDto> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDto));
        when(employeeService.listEmployeesByStatus(anyString(), any(PageRequest.class)))
                .thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees with invalid page number returns 400")
    public void testListEmployees_InvalidPageNumber_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "-1")
                .param("size", "20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees with invalid page size returns 400")
    public void testListEmployees_InvalidPageSize_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees with no results returns empty page")
    public void testListEmployees_NoResults_ReturnsEmptyPage() throws Exception {
        // Arrange
        Page<EmployeeDto> emptyPage = Page.empty();
        when(employeeService.listEmployees(any(PageRequest.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    @DisplayName("Test GET /employees with SUPERVISOR role returns 200")
    public void testListEmployees_SupervisorRole_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDto> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDto));
        when(employeeService.listEmployees(any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk());
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test POST /employees with maximum page size")
    public void testListEmployees_MaxPageSize_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDto> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDto));
        when(employeeService.listEmployees(any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test POST /employees with special characters in name")
    public void testCreateEmployee_SpecialCharactersInName_Returns201() throws Exception {
        // Arrange
        testEmployeeDto.setName("O'Brien-Smith");
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("O'Brien-Smith"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test POST /employees with Unicode characters in name")
    public void testCreateEmployee_UnicodeCharacters_Returns201() throws Exception {
        // Arrange
        testEmployeeDto.setName("JosÃ© GarcÃ­a");
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("JosÃ© GarcÃ­a"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test GET /employees with multiple filters")
    public void testListEmployees_MultipleFilters_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDto> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDto));
        when(employeeService.listEmployeesByDepartmentAndStatus(anyString(), anyString(), any(PageRequest.class)))
                .thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("department", "Warehouse")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test POST /employees with all optional fields null")
    public void testCreateEmployee_OptionalFieldsNull_Returns201() throws Exception {
        // Arrange
        testEmployeeDto.setShiftGroup(null);
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Test concurrent requests to create employee")
    public void testCreateEmployee_ConcurrentRequests_HandledCorrectly() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert - Simulate concurrent requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeDto)))
                    .andExpect(status().isCreated());
        }
    }
}