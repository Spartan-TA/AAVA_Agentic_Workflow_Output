package com.warehouseems.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouseems.employee.dto.EmployeeRequestDto;
import com.warehouseems.employee.dto.EmployeeResponseDto;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for EmployeeController.
 * Tests all REST endpoints with normal cases, edge cases, and boundary conditions.
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeResponseDto testEmployeeResponse;
    private EmployeeRequestDto testEmployeeRequest;

    @BeforeEach
    void setUp() {
        testEmployeeResponse = new EmployeeResponseDto();
        testEmployeeResponse.setId(1L);
        testEmployeeResponse.setName("John Doe");
        testEmployeeResponse.setBadgeId("EMP001");
        testEmployeeResponse.setRole("WORKER");
        testEmployeeResponse.setDepartment("Shipping");
        testEmployeeResponse.setShiftGroup("DAY_SHIFT");
        testEmployeeResponse.setHireDate(LocalDate.of(2023, 1, 1));
        testEmployeeResponse.setStatus("ACTIVE");
        testEmployeeResponse.setEmail("john.doe@warehouse.com");
        testEmployeeResponse.setPhone("+1234567890");
        testEmployeeResponse.setCreatedAt(LocalDateTime.now());
        testEmployeeResponse.setUpdatedAt(LocalDateTime.now());

        testEmployeeRequest = new EmployeeRequestDto();
        testEmployeeRequest.setName("John Doe");
        testEmployeeRequest.setBadgeId("EMP001");
        testEmployeeRequest.setRole("WORKER");
        testEmployeeRequest.setDepartment("Shipping");
        testEmployeeRequest.setShiftGroup("DAY_SHIFT");
        testEmployeeRequest.setHireDate(LocalDate.of(2023, 1, 1));
        testEmployeeRequest.setStatus("ACTIVE");
        testEmployeeRequest.setEmail("john.doe@warehouse.com");
        testEmployeeRequest.setPhone("+1234567890");
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    @DisplayName("GET /api/employees - Success with valid admin role")
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_Success() throws Exception {
        Page<EmployeeResponseDto> page = new PageImpl<>(Arrays.asList(testEmployeeResponse));
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @DisplayName("GET /api/employees - Success with HR role")
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_HRRole_Success() throws Exception {
        Page<EmployeeResponseDto> page = new PageImpl<>(Arrays.asList(testEmployeeResponse));
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/employees - Success with filters")
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_WithFilters_Success() throws Exception {
        Page<EmployeeResponseDto> page = new PageImpl<>(Arrays.asList(testEmployeeResponse));
        when(employeeService.getEmployeesByFilters(anyString(), anyString(), anyString(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/employees")
                .param("department", "Shipping")
                .param("role", "WORKER")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Shipping"));

        verify(employeeService, times(1)).getEmployeesByFilters(eq("Shipping"), eq("WORKER"), eq("ACTIVE"), any());
    }

    @Test
    @DisplayName("GET /api/employees - Empty result")
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_EmptyResult() throws Exception {
        Page<EmployeeResponseDto> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("GET /api/employees - Forbidden for WORKER role")
    @WithMockUser(roles = "WORKER")
    void testGetAllEmployees_WorkerRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/employees - Unauthorized without authentication")
    void testGetAllEmployees_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/employees - Pagination parameters")
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_WithPagination() throws Exception {
        Page<EmployeeResponseDto> page = new PageImpl<>(Arrays.asList(testEmployeeResponse), 
                PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    @DisplayName("GET /api/employees/{id} - Success")
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_Success() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(testEmployeeResponse));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Not found")
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(999L);
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Invalid ID format")
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_InvalidIdFormat() throws Exception {
        mockMvc.perform(get("/api/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Negative ID")
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NegativeId() throws Exception {
        when(employeeService.getEmployeeById(-1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Zero ID")
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_ZeroId() throws Exception {
        when(employeeService.getEmployeeById(0L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Worker can access")
    @WithMockUser(roles = "WORKER")
    void testGetEmployeeById_WorkerRole_Success() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(testEmployeeResponse));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk());
    }

    // ==================== GET EMPLOYEE BY BADGE ID TESTS ====================

    @Test
    @DisplayName("GET /api/employees/badge/{badgeId} - Success")
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeByBadgeId_Success() throws Exception {
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(Optional.of(testEmployeeResponse));

        mockMvc.perform(get("/api/employees/badge/EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).getEmployeeByBadgeId("EMP001");
    }

    @Test
    @DisplayName("GET /api/employees/badge/{badgeId} - Not found")
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeByBadgeId_NotFound() throws Exception {
        when(employeeService.getEmployeeByBadgeId("INVALID")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/badge/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/employees/badge/{badgeId} - Empty badge ID")
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeByBadgeId_EmptyBadgeId() throws Exception {
        mockMvc.perform(get("/api/employees/badge/"))
                .andExpect(status().isNotFound()); // Spring returns 404 for empty path variable
    }

    @Test
    @DisplayName("GET /api/employees/badge/{badgeId} - Special characters in badge ID")
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeByBadgeId_SpecialCharacters() throws Exception {
        when(employeeService.getEmployeeByBadgeId("EMP@001")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/badge/EMP@001"))
                .andExpect(status().isNotFound());
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("POST /api/employees - Success")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_Success() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                .thenReturn(testEmployeeResponse);

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/employees - Success with HR role")
    @WithMockUser(roles = "HR")
    void testCreateEmployee_HRRole_Success() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                .thenReturn(testEmployeeResponse);

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/employees - Forbidden for SUPERVISOR role")
    @WithMockUser(roles = "SUPERVISOR")
    void testCreateEmployee_SupervisorRole_Forbidden() throws Exception {
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/employees - Validation error for missing name")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MissingName_ValidationError() throws Exception {
        testEmployeeRequest.setName(null);

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees - Validation error for empty name")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_EmptyName_ValidationError() throws Exception {
        testEmployeeRequest.setName("");

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees - Validation error for missing badge ID")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MissingBadgeId_ValidationError() throws Exception {
        testEmployeeRequest.setBadgeId(null);

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees - Validation error for invalid badge ID format")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidBadgeIdFormat_ValidationError() throws Exception {
        testEmployeeRequest.setBadgeId("emp"); // Too short

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees - Validation error for invalid email")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidEmail_ValidationError() throws Exception {
        testEmployeeRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees - Validation error for invalid phone")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidPhone_ValidationError() throws Exception {
        testEmployeeRequest.setPhone("invalid");

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees - Duplicate badge ID")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DuplicateBadgeId_BadRequest() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees - Null hire date")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_NullHireDate_ValidationError() throws Exception {
        testEmployeeRequest.setHireDate(null);

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees - Future hire date")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_FutureHireDate_Success() throws Exception {
        testEmployeeRequest.setHireDate(LocalDate.now().plusDays(30));
        when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                .thenReturn(testEmployeeResponse);

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isCreated());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("PUT /api/employees/{id} - Success")
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_Success() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequestDto.class)))
                .thenReturn(Optional.of(testEmployeeResponse));

        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(EmployeeRequestDto.class));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Not found")
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_NotFound() throws Exception {
        when(employeeService.updateEmployee(eq(999L), any(EmployeeRequestDto.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Validation error")
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_ValidationError() throws Exception {
        testEmployeeRequest.setName("");

        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Forbidden for WORKER role")
    @WithMockUser(roles = "WORKER")
    void testUpdateEmployee_WorkerRole_Forbidden() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("DELETE /api/employees/{id} - Success")
    @WithMockUser(roles = "ADMIN")
    void testSoftDeleteEmployee_Success() throws Exception {
        when(employeeService.softDeleteEmployee(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).softDeleteEmployee(1L);
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Not found")
    @WithMockUser(roles = "ADMIN")
    void testSoftDeleteEmployee_NotFound() throws Exception {
        when(employeeService.softDeleteEmployee(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Forbidden for HR role")
    @WithMockUser(roles = "HR")
    void testSoftDeleteEmployee_HRRole_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Forbidden for SUPERVISOR role")
    @WithMockUser(roles = "SUPERVISOR")
    void testSoftDeleteEmployee_SupervisorRole_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Forbidden for WORKER role")
    @WithMockUser(roles = "WORKER")
    void testSoftDeleteEmployee_WorkerRole_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}