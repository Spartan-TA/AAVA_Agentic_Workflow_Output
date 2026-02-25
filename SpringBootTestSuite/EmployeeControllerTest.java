package com.warehouse.employee.management.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.management.application.dto.CreateEmployeeRequest;
import com.warehouse.employee.management.application.dto.UpdateEmployeeRequest;
import com.warehouse.employee.management.application.dto.EmployeeResponse;
import com.warehouse.employee.management.application.service.EmployeeService;
import com.warehouse.employee.management.domain.employee.EmployeeStatus;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for EmployeeController
 * Tests cover:
 * - All REST endpoints (GET, POST, PUT, PATCH, DELETE)
 * - Request validation
 * - Response formatting
 * - Security/authorization
 * - Error handling
 * - Edge cases and boundary conditions
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController Integration Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeResponse employeeResponse;
    private CreateEmployeeRequest createRequest;
    private UpdateEmployeeRequest updateRequest;
    private UUID testEmployeeId;

    @BeforeEach
    void setUp() {
        testEmployeeId = UUID.randomUUID();

        // Setup employee response
        employeeResponse = new EmployeeResponse();
        employeeResponse.setId(testEmployeeId);
        employeeResponse.setBadgeId("EMP001");
        employeeResponse.setFirstName("John");
        employeeResponse.setLastName("Doe");
        employeeResponse.setEmail("john.doe@warehouse.com");
        employeeResponse.setPhoneNumber("+1-555-0100");
        employeeResponse.setDateOfBirth(LocalDate.of(1990, 1, 15));
        employeeResponse.setHireDate(LocalDate.of(2020, 3, 1));
        employeeResponse.setStatus(EmployeeStatus.ACTIVE);

        // Setup create request
        createRequest = new CreateEmployeeRequest();
        createRequest.setBadgeId("EMP001");
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john.doe@warehouse.com");
        createRequest.setPhoneNumber("+1-555-0100");
        createRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));
        createRequest.setHireDate(LocalDate.of(2020, 3, 1));

        // Setup update request
        updateRequest = new UpdateEmployeeRequest();
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("john.smith@warehouse.com");
        updateRequest.setPhoneNumber("+1-555-0200");
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - Normal Case - Should Create Employee")
    void testCreateEmployee_NormalCase_Success() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testEmployeeId.toString()))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@warehouse.com"));

        verify(employeeService).createEmployee(any(CreateEmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("POST /employees - HR Role - Should Create Employee")
    void testCreateEmployee_HRRole_Success() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /employees - Worker Role - Should Return Forbidden")
    void testCreateEmployee_WorkerRole_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(CreateEmployeeRequest.class));
    }

    @Test
    @DisplayName("POST /employees - Unauthenticated - Should Return Unauthorized")
    void testCreateEmployee_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - Invalid Email - Should Return Bad Request")
    void testCreateEmployee_InvalidEmail_BadRequest() throws Exception {
        // Arrange
        createRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - Missing Required Fields - Should Return Bad Request")
    void testCreateEmployee_MissingFields_BadRequest() throws Exception {
        // Arrange
        createRequest.setBadgeId(null);
        createRequest.setFirstName(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - Empty Badge ID - Should Return Bad Request")
    void testCreateEmployee_EmptyBadgeId_BadRequest() throws Exception {
        // Arrange
        createRequest.setBadgeId("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees/{id} - Normal Case - Should Return Employee")
    void testGetEmployeeById_NormalCase_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(testEmployeeId)).thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", testEmployeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEmployeeId.toString()))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService).getEmployeeById(testEmployeeId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees/{id} - Not Found - Should Return Not Found")
    void testGetEmployeeById_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(testEmployeeId)).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", testEmployeeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("GET /employees/{id} - Worker Role - Should Return Forbidden for Other Employee")
    void testGetEmployeeById_WorkerRole_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", testEmployeeId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees - Normal Case - Should Return Page of Employees")
    void testGetAllEmployees_NormalCase_Success() throws Exception {
        // Arrange
        List<EmployeeResponse> employees = Arrays.asList(employeeResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees - Empty Result - Should Return Empty Page")
    void testGetAllEmployees_EmptyResult_Success() throws Exception {
        // Arrange
        Page<EmployeeResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees/badge/{badgeId} - Normal Case - Should Return Employee")
    void testGetEmployeeByBadgeId_NormalCase_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/badge/{badgeId}", "EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees/search - Normal Case - Should Return Matching Employees")
    void testSearchEmployees_NormalCase_Success() throws Exception {
        // Arrange
        List<EmployeeResponse> employees = Arrays.asList(employeeResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.searchEmployees(eq("John"), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search")
                .param("searchTerm", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /employees/{id} - Normal Case - Should Update Employee")
    void testUpdateEmployee_NormalCase_Success() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(testEmployeeId), any(UpdateEmployeeRequest.class)))
                .thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", testEmployeeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEmployeeId.toString()));

        verify(employeeService).updateEmployee(eq(testEmployeeId), any(UpdateEmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("PUT /employees/{id} - HR Role - Should Update Employee")
    void testUpdateEmployee_HRRole_Success() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(testEmployeeId), any(UpdateEmployeeRequest.class)))
                .thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", testEmployeeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("PUT /employees/{id} - Worker Role - Should Return Forbidden")
    void testUpdateEmployee_WorkerRole_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", testEmployeeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /employees/{id} - Not Found - Should Return Not Found")
    void testUpdateEmployee_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(testEmployeeId), any(UpdateEmployeeRequest.class)))
                .thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", testEmployeeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /employees/{id} - Normal Case - Should Partially Update Employee")
    void testPatchEmployee_NormalCase_Success() throws Exception {
        // Arrange
        UpdateEmployeeRequest partialUpdate = new UpdateEmployeeRequest();
        partialUpdate.setFirstName("UpdatedName");
        when(employeeService.patchEmployee(eq(testEmployeeId), any(UpdateEmployeeRequest.class)))
                .thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/employees/{id}", testEmployeeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /employees/{id} - Normal Case - Should Soft Delete Employee")
    void testDeleteEmployee_NormalCase_Success() throws Exception {
        // Arrange
        doNothing().when(employeeService).softDeleteEmployee(testEmployeeId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", testEmployeeId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService).softDeleteEmployee(testEmployeeId);
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("DELETE /employees/{id} - HR Role - Should Return Forbidden")
    void testDeleteEmployee_HRRole_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", testEmployeeId)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /employees/{id} - Not Found - Should Return Not Found")
    void testDeleteEmployee_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(employeeService).softDeleteEmployee(testEmployeeId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", testEmployeeId)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees/{id}/restore - Normal Case - Should Restore Employee")
    void testRestoreEmployee_NormalCase_Success() throws Exception {
        // Arrange
        when(employeeService.restoreEmployee(testEmployeeId)).thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees/{id}/restore", testEmployeeId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEmployeeId.toString()));
    }

    // ==================== FILTER TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees/filter - With Department - Should Return Filtered Results")
    void testFilterEmployees_WithDepartment_Success() throws Exception {
        // Arrange
        UUID departmentId = UUID.randomUUID();
        List<EmployeeResponse> employees = Arrays.asList(employeeResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.filterEmployees(isNull(), eq(departmentId), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/filter")
                .param("departmentId", departmentId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees/filter - With Status - Should Return Filtered Results")
    void testFilterEmployees_WithStatus_Success() throws Exception {
        // Arrange
        List<EmployeeResponse> employees = Arrays.asList(employeeResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.filterEmployees(isNull(), isNull(), isNull(), eq(EmployeeStatus.ACTIVE), isNull(), isNull(), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/filter")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees/filter - Multiple Criteria - Should Return Filtered Results")
    void testFilterEmployees_MultipleCriteria_Success() throws Exception {
        // Arrange
        UUID departmentId = UUID.randomUUID();
        List<EmployeeResponse> employees = Arrays.asList(employeeResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.filterEmployees(eq("John"), eq(departmentId), isNull(), eq(EmployeeStatus.ACTIVE), isNull(), isNull(), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/filter")
                .param("searchTerm", "John")
                .param("departmentId", departmentId.toString())
                .param("status", "ACTIVE"))
                .andExpect(status().isOk());
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees - Large Page Size - Should Handle Correctly")
    void testGetAllEmployees_LargePageSize_Success() throws Exception {
        // Arrange
        Page<EmployeeResponse> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 1000), 0);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("size", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees - Page Beyond Total - Should Return Empty Page")
    void testGetAllEmployees_PageBeyondTotal_ReturnsEmpty() throws Exception {
        // Arrange
        Page<EmployeeResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(100, 10), 0);
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees/search - Empty Search Term - Should Return All")
    void testSearchEmployees_EmptySearchTerm_ReturnsAll() throws Exception {
        // Arrange
        List<EmployeeResponse> employees = Arrays.asList(employeeResponse);
        Page<EmployeeResponse> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.searchEmployees(eq(""), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search")
                .param("searchTerm", ""))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /employees/search - Special Characters - Should Handle Correctly")
    void testSearchEmployees_SpecialCharacters_Success() throws Exception {
        // Arrange
        Page<EmployeeResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(employeeService.searchEmployees(eq("O'Brien"), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search")
                .param("searchTerm", "O'Brien"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - Maximum Field Lengths - Should Create Successfully")
    void testCreateEmployee_MaxFieldLengths_Success() throws Exception {
        // Arrange
        createRequest.setBadgeId("A".repeat(50));
        createRequest.setFirstName("B".repeat(100));
        createRequest.setLastName("C".repeat(100));
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }
}