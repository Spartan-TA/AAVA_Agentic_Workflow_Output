package com.warehouse.ems.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive controller tests for Employee
 * Tests cover: REST endpoints, validation, security, error handling
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create employee successfully")
    void testCreateEmployee() throws Exception {
        // Arrange
        Employee employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("B12345")
                .role("Operator")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 15))
                .active(true)
                .deleted(false)
                .build();

        when(employeeService.create(any(Employee.class))).thenReturn(employee);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("B12345"))
                .andExpect(jsonPath("$.department").value("Logistics"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when creating employee with invalid data")
    void testCreateEmployeeInvalidData() throws Exception {
        // Arrange
        Employee invalidEmployee = Employee.builder().build(); // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should get all employees")
    void testGetEmployees() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(
            Employee.builder().id(1L).name("John Doe").badgeId("B123").build(),
            Employee.builder().id(2L).name("Jane Smith").badgeId("B456").build()
        );
        when(employeeService.list()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return empty list when no employees exist")
    void testGetEmployeesEmpty() throws Exception {
        // Arrange
        when(employeeService.list()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get employee by ID")
    void testGetEmployeeById() throws Exception {
        // Arrange
        Employee employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("B12345")
                .build();
        when(employeeService.getById(1L)).thenReturn(Optional.of(employee));

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("B12345"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when employee not found")
    void testGetEmployeeByIdNotFound() throws Exception {
        // Arrange
        when(employeeService.getById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update employee successfully")
    void testUpdateEmployee() throws Exception {
        // Arrange
        Employee updated = Employee.builder()
                .id(1L)
                .name("Jane Doe")
                .badgeId("B12345")
                .build();
        when(employeeService.update(eq(1L), any(Employee.class))).thenReturn(updated);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when updating non-existent employee")
    void testUpdateEmployeeNotFound() throws Exception {
        // Arrange
        Employee employee = Employee.builder().id(999L).name("Test").build();
        when(employeeService.update(eq(999L), any(Employee.class)))
                .thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete employee successfully")
    void testDeleteEmployee() throws Exception {
        // Arrange
        doNothing().when(employeeService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when deleting non-existent employee")
    void testDeleteEmployeeNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Employee not found"))
                .when(employeeService).delete(999L);

        // Act & Assert
        mockMvc.perform(delete("/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Should filter employees by department")
    void testFilterEmployees() throws Exception {
        // Arrange
        List<Employee> filtered = List.of(
            Employee.builder().id(1L).name("John").department("Logistics").build()
        );
        when(employeeService.filter("Logistics")).thenReturn(filtered);

        // Act & Assert
        mockMvc.perform(get("/employees/filter")
                .param("department", "Logistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].department").value("Logistics"));
    }

    @Test
    @DisplayName("Should return 401 when accessing without authentication")
    void testUnauthorizedAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 403 when worker tries to create employee")
    void testForbiddenAccess() throws Exception {
        // Arrange
        Employee employee = Employee.builder().name("Test").build();

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should patch employee successfully")
    void testPatchEmployee() throws Exception {
        // Arrange
        Employee patched = Employee.builder().id(1L).name("Updated Name").build();
        when(employeeService.patch(eq(1L), any())).thenReturn(patched);

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Updated Name"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should search employees by badge ID")
    void testSearchByBadgeId() throws Exception {
        // Arrange
        Employee employee = Employee.builder().id(1L).badgeId("B12345").build();
        when(employeeService.findByBadgeId("B12345")).thenReturn(Optional.of(employee));

        // Act & Assert
        mockMvc.perform(get("/employees/search")
                .param("badgeId", "B12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B12345"));
    }
}