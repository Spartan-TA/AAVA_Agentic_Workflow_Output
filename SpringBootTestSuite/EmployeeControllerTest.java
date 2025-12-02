package com.warehouse.management.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.management.employee.dto.EmployeeDTO;
import com.warehouse.management.employee.entity.Employee;
import com.warehouse.management.employee.service.EmployeeService;
import com.warehouse.management.exception.BadRequestException;
import com.warehouse.management.exception.ResourceNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests cover REST API endpoints, validation, security, and edge cases
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private Employee validEmployee;
    private EmployeeDTO validEmployeeDTO;

    @BeforeEach
    void setUp() {
        // Arrange: Create valid test employee
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setEmail("john.doe@warehouse.com");
        validEmployee.setPhone("+1234567890");
        validEmployee.setRole("WORKER");
        validEmployee.setDepartment("SHIPPING");
        validEmployee.setShiftGroup("DAY");
        validEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        validEmployee.setStatus("ACTIVE");
        validEmployee.setTenantId("TENANT001");
        validEmployee.setDeleted(false);

        // Arrange: Create valid DTO
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setId(1L);
        validEmployeeDTO.setBadgeId("EMP001");
        validEmployeeDTO.setFirstName("John");
        validEmployeeDTO.setLastName("Doe");
        validEmployeeDTO.setEmail("john.doe@warehouse.com");
        validEmployeeDTO.setPhone("+1234567890");
        validEmployeeDTO.setRole("WORKER");
        validEmployeeDTO.setDepartment("SHIPPING");
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_ValidInput_ReturnsCreated() throws Exception {
        // Arrange
        when(employeeService.create(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(employeeService, times(1)).create(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DuplicateBadgeId_ReturnsBadRequest() throws Exception {
        // Arrange
        when(employeeService.create(any(Employee.class)))
            .thenThrow(new BadRequestException("BadgeId already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).create(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MissingBadgeId_ReturnsBadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MissingFirstName_ReturnsBadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setFirstName(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(Employee.class));
    }

    @Test
    void testCreateEmployee_Unauthorized_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).create(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_InsufficientPermissions_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).create(any(Employee.class));
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_ExistingId_ReturnsOk() throws Exception {
        // Arrange
        when(employeeService.getById(1L)).thenReturn(validEmployee);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(employeeService, times(1)).getById(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_NonExistingId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.getById(999L))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getById(999L);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_InvalidId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getById(anyLong());
    }

    @Test
    void testGetEmployeeById_Unauthorized_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).getById(anyLong());
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_WithPagination_ReturnsOk() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees);
        when(employeeService.getAll(anyString(), any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"));

        verify(employeeService, times(1)).getAll(anyString(), any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_EmptyResult_ReturnsOk() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.getAll(anyString(), any(PageRequest.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(employeeService, times(1)).getAll(anyString(), any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_InvalidPageNumber_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getAll(anyString(), any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_InvalidPageSize_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getAll(anyString(), any(PageRequest.class));
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_ValidInput_ReturnsOk() throws Exception {
        // Arrange
        when(employeeService.update(anyLong(), any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).update(anyLong(), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_NonExistingId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.update(anyLong(), any(Employee.class)))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).update(anyLong(), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).update(anyLong(), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testUpdateEmployee_InsufficientPermissions_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).update(anyLong(), any(Employee.class));
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_ExistingId_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).softDelete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).softDelete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_NonExistingId_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
            .when(employeeService).softDelete(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).softDelete(999L);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testDeleteEmployee_InsufficientPermissions_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).softDelete(anyLong());
    }

    @Test
    void testDeleteEmployee_Unauthorized_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).softDelete(anyLong());
    }

    // ========== CONTENT TYPE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_UnsupportedMediaType_ReturnsUnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());

        verify(employeeService, never()).create(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MalformedJson_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(Employee.class));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MaxLengthFields_ReturnsCreated() throws Exception {
        // Arrange
        validEmployeeDTO.setFirstName("A".repeat(100));
        validEmployeeDTO.setLastName("B".repeat(100));
        when(employeeService.create(any(Employee.class))).thenReturn(validEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).create(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_MaxPageSize_ReturnsOk() throws Exception {
        // Arrange
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(validEmployee));
        when(employeeService.getAll(anyString(), any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAll(anyString(), any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_MaxLongValue_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.getById(Long.MAX_VALUE))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/" + Long.MAX_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getById(Long.MAX_VALUE);
    }
}