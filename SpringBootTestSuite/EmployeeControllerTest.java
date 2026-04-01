package com.warehouse.controller;

import com.warehouse.entity.Employee;
import com.warehouse.service.EmployeeService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive JUnit test suite for EmployeeController.
 * Tests cover REST API endpoints, HTTP status codes, and security.
 * 
 * @author Warehouse EMS Test Team
 * @version 1.0.0
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController REST API Tests")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ========== CREATE ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test POST /employees with valid data returns 201 Created")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithValidData() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.role").value("WORKER"));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Test POST /employees with null name returns 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithNullName() throws Exception {
        // Arrange
        testEmployee.setName(null);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Test POST /employees with empty name returns 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithEmptyName() throws Exception {
        // Arrange
        testEmployee.setName("");

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Test POST /employees with duplicate badge ID returns 409 Conflict")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithDuplicateBadgeId() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isConflict());

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Test POST /employees without authentication returns 401 Unauthorized")
    public void testCreateEmployeeWithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Test POST /employees with WORKER role returns 403 Forbidden")
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployeeWithWorkerRole() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    // ========== READ ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test GET /employees/{id} with valid ID returns 200 OK")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeByIdWithValidId() throws Exception {
        // Arrange
        when(employeeService.findEmployeeById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).findEmployeeById(1L);
    }

    @Test
    @DisplayName("Test GET /employees/{id} with non-existent ID returns 404 Not Found")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeByIdWithNonExistentId() throws Exception {
        // Arrange
        when(employeeService.findEmployeeById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).findEmployeeById(999L);
    }

    @Test
    @DisplayName("Test GET /employees with pagination returns 200 OK")
    @WithMockUser(roles = "HR")
    public void testGetAllEmployeesWithPagination() throws Exception {
        // Arrange
        Employee employee2 = Employee.builder()
                .id(2L)
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .build();

        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee, employee2));
        when(employeeService.listAllEmployees(any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[1].name").value("Jane Smith"));

        verify(employeeService, times(1)).listAllEmployees(any(PageRequest.class));
    }

    @Test
    @DisplayName("Test GET /employees returns empty page when no employees")
    @WithMockUser(roles = "HR")
    public void testGetAllEmployeesReturnsEmptyPage() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.listAllEmployees(any(PageRequest.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));

        verify(employeeService, times(1)).listAllEmployees(any(PageRequest.class));
    }

    @Test
    @DisplayName("Test GET /employees with invalid page parameter returns 400 Bad Request")
    @WithMockUser(roles = "HR")
    public void testGetAllEmployeesWithInvalidPageParameter() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).listAllEmployees(any(PageRequest.class));
    }

    @Test
    @DisplayName("Test GET /employees with invalid size parameter returns 400 Bad Request")
    @WithMockUser(roles = "HR")
    public void testGetAllEmployeesWithInvalidSizeParameter() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).listAllEmployees(any(PageRequest.class));
    }

    // ========== UPDATE ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test PUT /employees/{id} with valid data returns 200 OK")
    @WithMockUser(roles = "HR")
    public void testUpdateEmployeeWithValidData() throws Exception {
        // Arrange
        testEmployee.setName("John Updated");
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    @DisplayName("Test PUT /employees/{id} with non-existent ID returns 404 Not Found")
    @WithMockUser(roles = "HR")
    public void testUpdateEmployeeWithNonExistentId() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(Employee.class)))
                .thenThrow(new IllegalArgumentException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    @DisplayName("Test PATCH /employees/{id} with partial data returns 200 OK")
    @WithMockUser(roles = "HR")
    public void testPartialUpdateEmployee() throws Exception {
        // Arrange
        testEmployee.setDepartment("Receiving");
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"department":"Receiving"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Receiving"));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    @DisplayName("Test PUT /employees/{id} with invalid data returns 400 Bad Request")
    @WithMockUser(roles = "HR")
    public void testUpdateEmployeeWithInvalidData() throws Exception {
        // Arrange
        testEmployee.setName("");

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).updateEmployee(anyLong(), any(Employee.class));
    }

    // ========== DELETE ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test DELETE /employees/{id} with valid ID returns 204 No Content")
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployeeWithValidId() throws Exception {
        // Arrange
        doNothing().when(employeeService).softDeleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).softDeleteEmployee(1L);
    }

    @Test
    @DisplayName("Test DELETE /employees/{id} with non-existent ID returns 404 Not Found")
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployeeWithNonExistentId() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Employee not found"))
                .when(employeeService).softDeleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).softDeleteEmployee(999L);
    }

    @Test
    @DisplayName("Test DELETE /employees/{id} with HR role returns 403 Forbidden")
    @WithMockUser(roles = "HR")
    public void testDeleteEmployeeWithHRRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).softDeleteEmployee(anyLong());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test POST /employees with malformed JSON returns 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithMalformedJSON() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Test GET /employees/{id} with invalid ID format returns 400 Bad Request")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeByIdWithInvalidIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/invalid"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).findEmployeeById(anyLong());
    }

    @Test
    @DisplayName("Test POST /employees with missing required fields returns 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithMissingRequiredFields() throws Exception {
        // Arrange
        Employee incompleteEmployee = Employee.builder().name("John Doe").build();

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteEmployee)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Test GET /employees with very large page size returns 400 Bad Request")
    @WithMockUser(roles = "HR")
    public void testGetAllEmployeesWithVeryLargePageSize() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10000"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).listAllEmployees(any(PageRequest.class));
    }

    @Test
    @DisplayName("Test POST /employees with special characters in name")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithSpecialCharactersInName() throws Exception {
        // Arrange
        testEmployee.setName("O'Brien-Smith, Jr.");
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("O'Brien-Smith, Jr."));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @DisplayName("Test GET /employees with filter parameter")
    @WithMockUser(roles = "HR")
    public void testGetAllEmployeesWithFilter() throws Exception {
        // Arrange
        Page<Employee> filteredPage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.listAllEmployees(any(PageRequest.class))).thenReturn(filteredPage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10")
                .param("department", "Shipping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(employeeService, times(1)).listAllEmployees(any(PageRequest.class));
    }

    @Test
    @DisplayName("Test OPTIONS /employees returns allowed methods")
    @WithMockUser(roles = "HR")
    public void testOptionsEmployeesEndpoint() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/employees"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Allow"));
    }
}
