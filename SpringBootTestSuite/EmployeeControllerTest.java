package com.warehouse.management.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.management.common.ApiResponse;
import com.warehouse.management.common.exceptions.BusinessException;
import com.warehouse.management.common.exceptions.ResourceNotFoundException;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests cover REST API endpoints, security, validation, and error handling
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeRequest testRequest;
    private EmployeeResponse testResponse;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        // Setup test request DTO
        testRequest = new EmployeeRequest();
        testRequest.setBadgeId("EMP001");
        testRequest.setFirstName("John");
        testRequest.setLastName("Doe");
        testRequest.setEmail("john.doe@warehouse.com");
        testRequest.setRole(EmployeeRole.WORKER);
        testRequest.setDepartment("Warehouse");
        testRequest.setHireDate(LocalDate.now());
        
        // Setup test response DTO
        testResponse = new EmployeeResponse();
        testResponse.setId(testId);
        testResponse.setBadgeId("EMP001");
        testResponse.setFirstName("John");
        testResponse.setLastName("Doe");
        testResponse.setEmail("john.doe@warehouse.com");
        testResponse.setRole(EmployeeRole.WORKER);
        testResponse.setStatus(EmployeeStatus.ACTIVE);
        testResponse.setDepartment("Warehouse");
    }

    // ========== CREATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_ValidInput_Returns201Created() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@warehouse.com"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_AsHR_Returns201Created() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_AsWorker_Returns403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    void testCreateEmployee_Unauthenticated_Returns401Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidInput_Returns400BadRequest() throws Exception {
        // Arrange
        testRequest.setBadgeId(""); // Invalid empty badge ID

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DuplicateBadgeId_Returns400BadRequest() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new BusinessException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Badge ID already exists")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MissingRequiredFields_Returns400BadRequest() throws Exception {
        // Arrange
        testRequest.setFirstName(null);
        testRequest.setLastName(null);
        testRequest.setEmail(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== GET EMPLOYEE BY ID ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_ValidId_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(testId)).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testId.toString()))
                .andExpect(jsonPath("$.data.badgeId").value("EMP001"));

        verify(employeeService, times(1)).getEmployeeById(testId);
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testGetEmployeeById_AsWorker_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(testId)).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getEmployeeById(testId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NonExistentId_Returns404NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(employeeService.getEmployeeById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Employee not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_InvalidUUIDFormat_Returns400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== UPDATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_ValidInput_Returns200OK() throws Exception {
        // Arrange
        EmployeeRequest updateRequest = new EmployeeRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@warehouse.com");
        updateRequest.setDepartment("Logistics");
        
        EmployeeResponse updatedResponse = new EmployeeResponse();
        updatedResponse.setId(testId);
        updatedResponse.setFirstName("Jane");
        updatedResponse.setLastName("Smith");
        
        when(employeeService.updateEmployee(eq(testId), any(EmployeeRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", testId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Smith"));

        verify(employeeService, times(1)).updateEmployee(eq(testId), any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_AsHR_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(testId), any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", testId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).updateEmployee(eq(testId), any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testUpdateEmployee_AsWorker_Returns403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", testId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).updateEmployee(any(UUID.class), any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_NonExistentId_Returns404NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(employeeService.updateEmployee(eq(nonExistentId), any(EmployeeRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", nonExistentId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isNotFound());
    }

    // ========== DELETE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_ValidId_Returns204NoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(testId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", testId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(testId);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testDeleteEmployee_AsHR_Returns403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", testId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_NonExistentId_Returns404NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Employee not found"))
                .when(employeeService).deleteEmployee(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", nonExistentId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ========== GET ALL EMPLOYEES ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_WithPagination_Returns200OK() throws Exception {
        // Arrange
        Page<EmployeeResponse> page = new PageImpl<>(Arrays.asList(testResponse), PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testGetAllEmployees_AsSupervisor_Returns200OK() throws Exception {
        // Arrange
        Page<EmployeeResponse> page = new PageImpl<>(Arrays.asList(testResponse), PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_EmptyResult_Returns200OK() throws Exception {
        // Arrange
        Page<EmployeeResponse> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_InvalidPageNumber_Returns400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_InvalidPageSize_Returns400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== SEARCH EMPLOYEES ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchEmployeesByDepartment_ValidDepartment_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.searchByDepartment("Warehouse")).thenReturn(Arrays.asList(testResponse));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search")
                .param("department", "Warehouse")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)));

        verify(employeeService, times(1)).searchByDepartment("Warehouse");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchEmployeesByStatus_ValidStatus_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.searchByStatus(EmployeeStatus.ACTIVE)).thenReturn(Arrays.asList(testResponse));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search")
                .param("status", "ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(employeeService, times(1)).searchByStatus(EmployeeStatus.ACTIVE);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchEmployees_NoResults_Returns200OK() throws Exception {
        // Arrange
        when(employeeService.searchByDepartment("NonExistent")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search")
                .param("department", "NonExistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_LargePayload_Returns201Created() throws Exception {
        // Arrange
        String largeString = "A".repeat(1000);
        testRequest.setFirstName(largeString);
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_SpecialCharacters_Returns201Created() throws Exception {
        // Arrange
        testRequest.setFirstName("JosÃ©");
        testRequest.setLastName("O'Brien-MÃ¼ller");
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_LargePageSize_Returns200OK() throws Exception {
        // Arrange
        Page<EmployeeResponse> page = new PageImpl<>(Arrays.asList(testResponse), PageRequest.of(0, 1000), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}