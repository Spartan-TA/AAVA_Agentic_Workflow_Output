package com.company.warehouse.api.controller;

import com.company.warehouse.api.dto.EmployeeDTO;
import com.company.warehouse.core.service.EmployeeService;
import com.company.warehouse.api.exception.ResourceNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive unit tests for EmployeeController.
 * Tests cover REST endpoints, security, validation, and edge cases.
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

    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        testEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2023, 1, 15))
                .email("john.doe@company.com")
                .phone("+1234567890")
                .status("ACTIVE")
                .build();
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create employee with valid data as ADMIN")
    void testCreateEmployee_ValidDataAsAdmin_Success() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.department").value("Shipping"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should create employee with valid data as HR")
    void testCreateEmployee_ValidDataAsHR_Success() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 403 when WORKER tries to create employee")
    void testCreateEmployee_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user tries to create employee")
    void testCreateEmployee_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when creating employee with null name")
    void testCreateEmployee_NullName_BadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when creating employee with empty badgeId")
    void testCreateEmployee_EmptyBadgeId_BadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when creating employee with invalid email")
    void testCreateEmployee_InvalidEmail_BadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get employee by ID as ADMIN")
    void testGetEmployeeById_AsAdmin_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Should get employee by ID as SUPERVISOR")
    void testGetEmployeeById_AsSupervisor_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when employee not found")
    void testGetEmployeeById_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L)).thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when getting employee with invalid ID format")
    void testGetEmployeeById_InvalidIdFormat_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getEmployeeById(anyLong());
    }

    // ========== LIST EMPLOYEES TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should list all employees with pagination")
    void testListEmployees_WithPagination_Success() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.listEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(employeeService, times(1)).listEmployees(any());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should filter employees by department")
    void testListEmployees_FilterByDepartment_Success() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        when(employeeService.listEmployeesByDepartment("Shipping")).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("department", "Shipping")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Shipping"));

        verify(employeeService, times(1)).listEmployeesByDepartment("Shipping");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return empty list when no employees found")
    void testListEmployees_NoEmployees_ReturnsEmptyList() throws Exception {
        // Arrange
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(employeeService.listEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(employeeService, times(1)).listEmployees(any());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update employee as ADMIN")
    void testUpdateEmployee_AsAdmin_Success() throws Exception {
        // Arrange
        testEmployeeDTO.setName("Jane Doe");
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 403 when WORKER tries to update employee")
    void testUpdateEmployee_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when updating non-existent employee")
    void testUpdateEmployee_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete employee as ADMIN")
    void testDeleteEmployee_AsAdmin_Success() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 403 when HR tries to delete employee")
    void testDeleteEmployee_AsHR_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when deleting non-existent employee")
    void testDeleteEmployee_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found")).when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployee(999L);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle large page size gracefully")
    void testListEmployees_LargePageSize_Success() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 1000), 0);
        when(employeeService.listEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "1000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).listEmployees(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle negative page number gracefully")
    void testListEmployees_NegativePageNumber_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).listEmployees(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle malformed JSON gracefully")
    void testCreateEmployee_MalformedJSON_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle missing Content-Type header")
    void testCreateEmployee_MissingContentType_UnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnsupportedMediaType());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }
}