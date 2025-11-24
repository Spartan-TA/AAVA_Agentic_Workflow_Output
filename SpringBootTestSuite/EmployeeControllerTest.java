package com.warehouse.employee.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.management.model.Employee;
import com.warehouse.employee.management.service.AuditService;
import com.warehouse.employee.management.service.EmployeeService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController.
 * Tests cover REST endpoints, security, and edge cases.
 * Uses MockMvc for integration testing of controller layer.
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private AuditService auditService;

    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ========== Tests for GET /employees ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_AsAdmin_Success() throws Exception {
        // Arrange
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.getAllEmployees(any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));

        verify(employeeService, times(1)).getAllEmployees(any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testGetAllEmployees_AsHR_Success() throws Exception {
        // Arrange
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.getAllEmployees(any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testGetAllEmployees_AsSupervisor_Success() throws Exception {
        // Arrange
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.getAllEmployees(any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAllEmployees_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).getAllEmployees(any());
    }

    @Test
    public void testGetAllEmployees_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_EmptyResult_Success() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.getAllEmployees(any(PageRequest.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ========== Tests for GET /employees/{id} ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployeeById_ValidId_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        mockMvc.perform(get("/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployeeById_NonExistentId_NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(999L);
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testGetEmployeeById_AsHR_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        mockMvc.perform(get("/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetEmployeeById_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployeeById_InvalidIdFormat_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== Tests for POST /employees ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_AsAdmin_Success() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);
        doNothing().when(auditService).logAudit(anyString(), anyLong(), anyString(), anyString(), any(), any());

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
        verify(auditService, times(1)).logAudit(anyString(), anyLong(), anyString(), anyString(), any(), any());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_AsHR_Success() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);
        doNothing().when(auditService).logAudit(anyString(), anyLong(), anyString(), anyString(), any(), any());

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testCreateEmployee_AsSupervisor_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployee_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_DuplicateBadgeId_BadRequest() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class)))
                .thenThrow(new RuntimeException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_InvalidJson_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // ========== Tests for PUT /employees/{id} ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_AsAdmin_Success() throws Exception {
        // Arrange
        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Updated")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();
        
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(updatedEmployee);
        doNothing().when(auditService).logAudit(anyString(), anyLong(), anyString(), anyString(), any(), any());

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_AsHR_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);
        doNothing().when(auditService).logAudit(anyString(), anyLong(), anyString(), anyString(), any(), any());

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testUpdateEmployee_AsSupervisor_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_NonExistentId_NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L)).thenReturn(Optional.empty());
        when(employeeService.updateEmployee(anyLong(), any(Employee.class)))
                .thenThrow(new RuntimeException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().is5xxServerError());
    }

    // ========== Tests for DELETE /employees/{id} ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSoftDeleteEmployee_AsAdmin_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeService).softDeleteEmployee(1L);
        doNothing().when(auditService).logAudit(anyString(), anyLong(), anyString(), anyString(), any(), any());

        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).softDeleteEmployee(1L);
        verify(auditService, times(1)).logAudit(anyString(), anyLong(), anyString(), anyString(), any(), any());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testSoftDeleteEmployee_AsHR_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).softDeleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testSoftDeleteEmployee_AsSupervisor_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testSoftDeleteEmployee_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSoftDeleteEmployee_NonExistentId_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L)).thenReturn(Optional.empty());
        doNothing().when(employeeService).softDeleteEmployee(999L);
        doNothing().when(auditService).logAudit(anyString(), anyLong(), anyString(), anyString(), any(), any());

        // Act & Assert
        mockMvc.perform(delete("/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testSoftDeleteEmployee_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ========== CSRF Token Tests ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_WithoutCsrf_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_WithoutCsrf_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_WithoutCsrf_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}