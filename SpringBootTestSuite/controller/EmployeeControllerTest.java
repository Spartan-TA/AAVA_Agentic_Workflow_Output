package com.warehouse.employee.controller;

import com.warehouse.employee.entity.Employee;
import com.warehouse.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityNotFoundException;

/**
 * Comprehensive JUnit test class for EmployeeController
 * Tests all REST endpoints with security, validation, and edge cases
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.of(2024, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_ExistingEmployee_ReturnsOk() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        
        // Act & Assert
        mockMvc.perform(get("/employees/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.badgeId").value("EMP001"))
            .andExpect(jsonPath("$.role").value("WORKER"));
        
        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NonExistingEmployee_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
            .thenThrow(new EntityNotFoundException("Employee not found"));
        
        // Act & Assert
        mockMvc.perform(get("/employees/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetEmployeeById_Unauthorized_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testGetEmployeeById_InsufficientPermissions_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_ValidData_ReturnsCreated() throws Exception {
        // Arrange
        Employee newEmployee = new Employee();
        newEmployee.setName("Jane Smith");
        newEmployee.setBadgeId("EMP002");
        newEmployee.setRole("WORKER");
        newEmployee.setDepartment("Warehouse");
        
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(newEmployee);
        
        String json = objectMapper.writeValueAsString(newEmployee);
        
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Jane Smith"))
            .andExpect(jsonPath("$.badgeId").value("EMP002"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidData_ReturnsBadRequest() throws Exception {
        // Arrange
        Employee invalidEmployee = new Employee();
        // Missing required fields
        
        String json = objectMapper.writeValueAsString(invalidEmployee);
        
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DuplicateBadgeId_ReturnsBadRequest() throws Exception {
        // Arrange
        Employee newEmployee = new Employee();
        newEmployee.setBadgeId("EMP001");
        
        when(employeeService.createEmployee(any(Employee.class)))
            .thenThrow(new IllegalArgumentException("Badge ID already exists"));
        
        String json = objectMapper.writeValueAsString(newEmployee);
        
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_ValidData_ReturnsOk() throws Exception {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("John Updated");
        updatedEmployee.setDepartment("Logistics");
        
        when(employeeService.updateEmployee(eq(1L), any(Employee.class)))
            .thenReturn(testEmployee);
        
        String json = objectMapper.writeValueAsString(updatedEmployee);
        
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_NonExistingEmployee_ReturnsNotFound() throws Exception {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("John Updated");
        
        when(employeeService.updateEmployee(eq(999L), any(Employee.class)))
            .thenThrow(new EntityNotFoundException("Employee not found"));
        
        String json = objectMapper.writeValueAsString(updatedEmployee);
        
        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_ExistingEmployee_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).softDeleteEmployee(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
            .andExpect(status().isNoContent());
        
        verify(employeeService, times(1)).softDeleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_NonExistingEmployee_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Employee not found"))
            .when(employeeService).softDeleteEmployee(999L);
        
        // Act & Assert
        mockMvc.perform(delete("/employees/999")
                .with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testGetAllEmployees_WithPagination_ReturnsOk() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        
        when(employeeService.getAllEmployees(any())).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("John Doe"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeByBadgeId_ExistingBadge_ReturnsOk() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(testEmployee);
        
        // Act & Assert
        mockMvc.perform(get("/employees/badge/EMP001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeesByDepartment_ReturnsOk() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeService.getEmployeesByDepartment("Warehouse")).thenReturn(employees);
        
        // Act & Assert
        mockMvc.perform(get("/employees/department/Warehouse"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].department").value("Warehouse"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeesByRole_ReturnsOk() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeService.getEmployeesByRole("WORKER")).thenReturn(employees);
        
        // Act & Assert
        mockMvc.perform(get("/employees/role/WORKER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].role").value("WORKER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_EmptyName_ReturnsBadRequest() throws Exception {
        // Arrange
        Employee invalidEmployee = new Employee();
        invalidEmployee.setName("");
        invalidEmployee.setBadgeId("EMP002");
        
        String json = objectMapper.writeValueAsString(invalidEmployee);
        
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_NullBadgeId_ReturnsBadRequest() throws Exception {
        // Arrange
        Employee invalidEmployee = new Employee();
        invalidEmployee.setName("Valid Name");
        invalidEmployee.setBadgeId(null);
        
        String json = objectMapper.writeValueAsString(invalidEmployee);
        
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidJsonFormat_ReturnsBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{invalid json}";
        
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}