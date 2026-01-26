package com.wms.ems.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.ems.employee.dto.EmployeeRequestDTO;
import com.wms.ems.employee.dto.EmployeeResponseDTO;
import com.wms.ems.employee.service.EmployeeService;
import com.wms.ems.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Covers: REST endpoints, security, validation, error handling
 * Epic: E02 - Employee Master Data (CRUD), E03 - RBAC
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeRequestDTO validRequest;
    private EmployeeResponseDTO validResponse;

    @BeforeEach
    public void setUp() {
        validRequest = new EmployeeRequestDTO();
        validRequest.setBadgeId("EMP001");
        validRequest.setName("John Doe");
        validRequest.setRole("WORKER");
        validRequest.setDepartment("Warehouse");
        validRequest.setShiftGroup("Day Shift");
        validRequest.setHireDate(LocalDate.of(2024, 1, 1));

        validResponse = new EmployeeResponseDTO();
        validResponse.setId(1L);
        validResponse.setBadgeId("EMP001");
        validResponse.setName("John Doe");
        validResponse.setRole("WORKER");
        validResponse.setDepartment("Warehouse");
        validResponse.setShiftGroup("Day Shift");
        validResponse.setHireDate(LocalDate.of(2024, 1, 1));
        validResponse.setStatus("ACTIVE");
    }

    // ========== POST /employees TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_ValidInput_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_AsHR_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployee_AsWorker_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateEmployee_Unauthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_InvalidInput_Returns400() throws Exception {
        // Arrange
        validRequest.setName(""); // Invalid: empty name

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_NullBadgeId_Returns400() throws Exception {
        // Arrange
        validRequest.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_DuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Badge ID already exists"));
    }

    // ========== GET /employees/{id} TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployeeById_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetEmployeeById_AsWorker_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployeeById_InvalidId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    public void testGetEmployeeById_Unauthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET /employees TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_ValidPageable_Returns200() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(validResponse);
        Page<EmployeeResponseDTO> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testGetAllEmployees_AsSupervisor_Returns200() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(validResponse);
        Page<EmployeeResponseDTO> page = new PageImpl<>(employees);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAllEmployees_AsWorker_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_WithFilters_Returns200() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(validResponse);
        Page<EmployeeResponseDTO> page = new PageImpl<>(employees);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("department", "Warehouse")
                .param("role", "WORKER"))
                .andExpect(status().isOk());
    }

    // ========== PUT /employees/{id} TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_ValidInput_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                .thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_AsHR_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                .thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testUpdateEmployee_AsWorker_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_InvalidId_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    // ========== DELETE /employees/{id} TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_ValidId_Returns204() throws Exception {
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
    public void testDeleteEmployee_AsHR_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_InvalidId_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ========== SEARCH ENDPOINTS TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSearchByDepartment_ValidDepartment_Returns200() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(validResponse);
        when(employeeService.searchEmployeesByDepartment("Warehouse")).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/employees/search")
                .param("department", "Warehouse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Warehouse"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSearchByRole_ValidRole_Returns200() throws Exception {
        // Arrange
        List<EmployeeResponseDTO> employees = Arrays.asList(validResponse);
        when(employeeService.searchEmployeesByRole("WORKER")).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/employees/search")
                .param("role", "WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("WORKER"));
    }

    // ========== VALIDATION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_NameTooLong_Returns400() throws Exception {
        // Arrange
        validRequest.setName("A".repeat(256)); // Exceeds max length

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_InvalidEmail_Returns400() throws Exception {
        // Arrange
        validRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_FutureHireDate_Returns400() throws Exception {
        // Arrange
        validRequest.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== CONTENT TYPE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_InvalidContentType_Returns415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_MalformedJSON_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }
}