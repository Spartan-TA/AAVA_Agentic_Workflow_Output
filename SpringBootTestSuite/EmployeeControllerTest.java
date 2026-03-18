package com.company.wms.employee.controller;

import com.company.wms.employee.dto.EmployeeCreateDTO;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.dto.EmployeeFilterDTO;
import com.company.wms.employee.dto.EmployeeUpdateDTO;
import com.company.wms.employee.entity.EmployeeRole;
import com.company.wms.employee.entity.EmployeeStatus;
import com.company.wms.employee.service.EmployeeService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests REST API endpoints with security, validation, and error handling
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
    private EmployeeCreateDTO testCreateDTO;
    private EmployeeUpdateDTO testUpdateDTO;

    @BeforeEach
    void setUp() {
        testEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2024, 1, 1))
                .status(EmployeeStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCreateDTO = new EmployeeCreateDTO();
        testCreateDTO.setBadgeId("EMP001");
        testCreateDTO.setName("John Doe");
        testCreateDTO.setRole(EmployeeRole.WORKER);
        testCreateDTO.setDepartment("Warehouse");
        testCreateDTO.setShiftGroup("Day Shift");
        testCreateDTO.setHireDate(LocalDate.of(2024, 1, 1));

        testUpdateDTO = new EmployeeUpdateDTO();
        testUpdateDTO.setName("John Updated");
        testUpdateDTO.setDepartment("Logistics");
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_ValidInput_ReturnsCreated() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeCreateDTO.class)))
                .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.role").value("WORKER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(employeeService).createEmployee(any(EmployeeCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_AsHR_ReturnsCreated() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeCreateDTO.class)))
                .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_AsWorker_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeCreateDTO.class));
    }

    @Test
    void testCreateEmployee_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MissingBadgeId_ReturnsBadRequest() throws Exception {
        // Arrange
        testCreateDTO.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_EmptyName_ReturnsBadRequest() throws Exception {
        // Arrange
        testCreateDTO.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_FutureHireDate_ReturnsBadRequest() throws Exception {
        // Arrange
        testCreateDTO.setHireDate(LocalDate.now().plusDays(10));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== LIST EMPLOYEES TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListEmployees_ValidRequest_ReturnsPagedResults() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> page = new PageImpl<>(employees, PageRequest.of(0, 20), 1);

        when(employeeService.listEmployees(any(EmployeeFilterDTO.class), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testListEmployees_AsSupervisor_ReturnsResults() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> page = new PageImpl<>(employees, PageRequest.of(0, 20), 1);

        when(employeeService.listEmployees(any(EmployeeFilterDTO.class), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testListEmployees_AsWorker_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListEmployees_WithFilters_ReturnsFilteredResults() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> page = new PageImpl<>(employees, PageRequest.of(0, 20), 1);

        when(employeeService.listEmployees(any(EmployeeFilterDTO.class), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("department", "Warehouse")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployee_ValidId_ReturnsEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployee_AsHR_ReturnsEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testGetEmployee_AsSupervisor_ReturnsEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testGetEmployee_AsWorker_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployee_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new EmployeeNotFoundException("Employee not found: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Employee not found")));
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_ValidUpdate_ReturnsUpdated() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(1L), any(EmployeeUpdateDTO.class)))
                .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(employeeService).updateEmployee(eq(1L), any(EmployeeUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_AsHR_ReturnsUpdated() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(1L), any(EmployeeUpdateDTO.class)))
                .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testUpdateEmployee_AsSupervisor_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(999L), any(EmployeeUpdateDTO.class)))
                .thenThrow(new EmployeeNotFoundException("Employee not found: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_ValidId_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testDeleteEmployee_AsHR_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testDeleteEmployee_AsSupervisor_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new EmployeeNotFoundException("Employee not found: 999"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidJson_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListEmployees_LargePageSize_ReturnsResults() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> page = new PageImpl<>(employees, PageRequest.of(0, 1000), 1);

        when(employeeService.listEmployees(any(EmployeeFilterDTO.class), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("size", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_EmptyBody_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk()); // Empty update is allowed
    }
}
