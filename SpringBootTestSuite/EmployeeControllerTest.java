package com.warehouse.ems.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.employee.dto.EmployeeDTO;
import com.warehouse.ems.employee.service.EmployeeService;
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

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests cover REST endpoints, validation, authorization, and HTTP status codes
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Tests")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    public void setUp() {
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Morning");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== CREATE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/v1/employees with valid data returns 201")
    public void testCreateEmployee_ValidData_Returns201() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/v1/employees with missing required fields returns 400")
    public void testCreateEmployee_MissingRequiredFields_Returns400() throws Exception {
        // Arrange
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setName("John Doe");
        // Missing badgeId, role, and hireDate

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/v1/employees with duplicate badge ID returns 400")
    public void testCreateEmployee_DuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDTO.class)))
            .thenThrow(new IllegalArgumentException("Badge ID already exists: EMP001"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/v1/employees with empty badge ID returns 400")
    public void testCreateEmployee_EmptyBadgeId_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/v1/employees with null name returns 400")
    public void testCreateEmployee_NullName_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST /api/v1/employees without authentication returns 401")
    public void testCreateEmployee_NoAuthentication_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test POST /api/v1/employees with insufficient role returns 403")
    public void testCreateEmployee_InsufficientRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());
    }

    // ========== READ ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/v1/employees returns 200 with employee list")
    public void testListEmployees_NoFilters_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.list(any(), any(), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/v1/employees with status filter returns 200")
    public void testListEmployees_WithStatusFilter_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.list(eq("ACTIVE"), any(), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/v1/employees with department filter returns 200")
    public void testListEmployees_WithDepartmentFilter_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.list(any(), eq("Warehouse"), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("department", "Warehouse")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Warehouse"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/v1/employees with empty result returns 200")
    public void testListEmployees_EmptyResult_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.list(any(), any(), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/v1/employees/{id} with valid ID returns 200")
    public void testGetEmployeeById_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/v1/employees/{id} with non-existent ID returns 404")
    public void testGetEmployeeById_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getById(999L))
            .thenThrow(new EntityNotFoundException("Employee not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/v1/employees/{id} with invalid ID format returns 400")
    public void testGetEmployeeById_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    // ========== UPDATE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/v1/employees/{id} with valid data returns 200")
    public void testUpdateEmployee_ValidData_Returns200() throws Exception {
        // Arrange
        EmployeeDTO updatedDTO = new EmployeeDTO();
        updatedDTO.setId(1L);
        updatedDTO.setBadgeId("EMP001");
        updatedDTO.setName("Jane Doe");
        updatedDTO.setRole("SUPERVISOR");
        updatedDTO.setDepartment("Warehouse");
        updatedDTO.setShiftGroup("Morning");
        updatedDTO.setHireDate(LocalDate.of(2023, 1, 15));
        updatedDTO.setStatus("ACTIVE");
        
        when(employeeService.update(eq(1L), any(EmployeeDTO.class))).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/v1/employees/{id} with non-existent ID returns 404")
    public void testUpdateEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.update(eq(999L), any(EmployeeDTO.class)))
            .thenThrow(new EntityNotFoundException("Employee not found with id: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/v1/employees/{id} with invalid data returns 400")
    public void testUpdateEmployee_InvalidData_Returns400() throws Exception {
        // Arrange
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setBadgeId("");
        invalidDTO.setName("");

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/v1/employees/{id} with duplicate badge ID returns 400")
    public void testUpdateEmployee_DuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.update(eq(1L), any(EmployeeDTO.class)))
            .thenThrow(new IllegalArgumentException("Badge ID already exists: EMP002"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    // ========== DELETE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test DELETE /api/v1/employees/{id} with valid ID returns 204")
    public void testDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).delete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test DELETE /api/v1/employees/{id} with non-existent ID returns 404")
    public void testDeleteEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Employee not found with id: 999"))
            .when(employeeService).delete(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test DELETE /api/v1/employees/{id} with insufficient role returns 403")
    public void testDeleteEmployee_InsufficientRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/v1/employees with large page size returns 200")
    public void testListEmployees_LargePageSize_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.list(any(), any(), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/v1/employees with negative page number returns 400")
    public void testListEmployees_NegativePageNumber_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/v1/employees with maximum length fields returns 201")
    public void testCreateEmployee_MaxLengthFields_Returns201() throws Exception {
        // Arrange
        testEmployeeDTO.setName("A".repeat(128));
        testEmployeeDTO.setBadgeId("B".repeat(32));
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/v1/employees with special characters in name returns 201")
    public void testCreateEmployee_SpecialCharactersInName_Returns201() throws Exception {
        // Arrange
        testEmployeeDTO.setName("O'Brien-Smith Jr.");
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }
}