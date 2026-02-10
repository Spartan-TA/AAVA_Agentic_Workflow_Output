package com.company.warehousemgmt.controller;

import com.company.warehousemgmt.dto.EmployeeDTO;
import com.company.warehousemgmt.exception.NotFoundException;
import com.company.warehousemgmt.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Tests cover REST endpoints, security, validation, and error handling
 */
@WebMvcTest(EmployeeController.class)
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
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("A");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== GET /api/employees Tests ==========

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_WithValidRequest_ReturnsPageOfEmployees() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAll(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(employeeService, times(1)).getAll(any());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_WithEmptyResult_ReturnsEmptyPage() throws Exception {
        // Arrange
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(employeeService.getAll(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(employeeService, times(1)).getAll(any());
    }

    @Test
    void testGetAllEmployees_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).getAll(any());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testGetAllEmployees_WithInsufficientRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).getAll(any());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_WithInvalidPageNumber_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getAll(any());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_WithInvalidPageSize_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getAll(any());
    }

    // ========== GET /api/employees/{id} Tests ==========

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_WithValidId_ReturnsEmployee() throws Exception {
        // Arrange
        when(employeeService.getById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.role").value("WORKER"));

        verify(employeeService, times(1)).getById(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.getById(999L)).thenThrow(new NotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getById(999L);
    }

    @Test
    void testGetEmployeeById_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).getById(anyLong());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_WithInvalidIdFormat_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getById(anyLong());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_WithNegativeId_ReturnsBadRequest() throws Exception {
        // Arrange
        when(employeeService.getById(-1L)).thenThrow(new IllegalArgumentException("Invalid ID"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/-1"))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).getById(-1L);
    }

    // ========== POST /api/employees Tests ==========

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithValidData_ReturnsCreatedEmployee() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithNullBadgeId_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithEmptyBadgeId_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithNullName_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithDuplicateBadgeId_ReturnsConflict() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    @Test
    void testCreateEmployee_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_WithInsufficientRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithInvalidJSON_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithFutureHireDate_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));
        when(employeeService.create(any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Hire date cannot be in the future"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    // ========== PUT /api/employees/{id} Tests ==========

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_WithValidData_ReturnsUpdatedEmployee() throws Exception {
        // Arrange
        testEmployeeDTO.setName("Jane Doe");
        when(employeeService.update(eq(1L), any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));

        verify(employeeService, times(1)).update(eq(1L), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.update(eq(999L), any(EmployeeDTO.class)))
                .thenThrow(new NotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).update(eq(999L), any(EmployeeDTO.class));
    }

    @Test
    void testUpdateEmployee_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).update(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testUpdateEmployee_WithInsufficientRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).update(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_WithNullName_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).update(anyLong(), any(EmployeeDTO.class));
    }

    // ========== DELETE /api/employees/{id} Tests ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_WithValidId_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).softDelete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).softDelete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new NotFoundException("Employee not found")).when(employeeService).softDelete(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).softDelete(999L);
    }

    @Test
    void testDeleteEmployee_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).softDelete(anyLong());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testDeleteEmployee_WithInsufficientRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).softDelete(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_WithAlreadyDeletedEmployee_ReturnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Employee already deleted"))
                .when(employeeService).softDelete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).softDelete(1L);
    }

    // ========== Edge Case Tests ==========

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithSpecialCharactersInName_ReturnsCreated() throws Exception {
        // Arrange
        testEmployeeDTO.setName("John O'Brien-Smith");
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John O'Brien-Smith"));

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithUnicodeCharacters_ReturnsCreated() throws Exception {
        // Arrange
        testEmployeeDTO.setName("JosÃ© GarcÃ­a");
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("JosÃ© GarcÃ­a"));

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_WithLargePageSize_ReturnsOk() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> page = new PageImpl<>(employees, PageRequest.of(0, 1000), 1);
        when(employeeService.getAll(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(employeeService, times(1)).getAll(any());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_WithMaxLengthFields_ReturnsCreated() throws Exception {
        // Arrange
        testEmployeeDTO.setName("A".repeat(100));
        testEmployeeDTO.setDepartment("B".repeat(100));
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).create(any(EmployeeDTO.class));
    }
}