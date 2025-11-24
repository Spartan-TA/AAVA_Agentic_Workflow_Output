package com.warehousemgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehousemgmt.domain.EmployeeStatus;
import com.warehousemgmt.domain.Role;
import com.warehousemgmt.dto.EmployeeRequestDTO;
import com.warehousemgmt.dto.EmployeeResponseDTO;
import com.warehousemgmt.service.EmployeeService;
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
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Covers all REST endpoints, security, validation, and HTTP status codes
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeRequestDTO validRequestDTO;
    private EmployeeResponseDTO validResponseDTO;

    @BeforeEach
    public void setUp() {
        validRequestDTO = new EmployeeRequestDTO();
        validRequestDTO.setName("John Doe");
        validRequestDTO.setBadgeId("EMP001");
        validRequestDTO.setRole(Role.WORKER);
        validRequestDTO.setDepartment("Warehouse");
        validRequestDTO.setShiftGroup("Morning");
        validRequestDTO.setHireDate(LocalDate.of(2023, 1, 15));
        validRequestDTO.setStatus(EmployeeStatus.ACTIVE);

        validResponseDTO = new EmployeeResponseDTO();
        validResponseDTO.setId(1L);
        validResponseDTO.setName("John Doe");
        validResponseDTO.setBadgeId("EMP001");
        validResponseDTO.setRole(Role.WORKER);
        validResponseDTO.setDepartment("Warehouse");
        validResponseDTO.setShiftGroup("Morning");
        validResponseDTO.setHireDate(LocalDate.of(2023, 1, 15));
        validResponseDTO.setStatus(EmployeeStatus.ACTIVE);
    }

    // ========== CREATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_ValidInput_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(validResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_InvalidInput_Returns400() throws Exception {
        // Arrange
        validRequestDTO.setName(null);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployee_UnauthorizedRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    public void testCreateEmployee_Unauthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_DuplicateBadgeId_Returns409() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_EmptyRequestBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_MalformedJson_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    // ========== GET ALL EMPLOYEES ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_NoFilters_Returns200() throws Exception {
        // Arrange
        Page<EmployeeResponseDTO> page = new PageImpl<>(Arrays.asList(validResponseDTO));
        when(employeeService.getAllEmployees(any(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));

        verify(employeeService, times(1)).getAllEmployees(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_WithDepartmentFilter_Returns200() throws Exception {
        // Arrange
        Page<EmployeeResponseDTO> page = new PageImpl<>(Arrays.asList(validResponseDTO));
        when(employeeService.getAllEmployees(any(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "20")
                .param("department", "Warehouse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Warehouse"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testGetAllEmployees_SupervisorRole_Returns200() throws Exception {
        // Arrange
        Page<EmployeeResponseDTO> page = new PageImpl<>(Arrays.asList(validResponseDTO));
        when(employeeService.getAllEmployees(any(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAllEmployees_WorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).getAllEmployees(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_InvalidPageNumber_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "-1")
                .param("size", "20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_InvalidPageSize_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    // ========== GET EMPLOYEE BY ID ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployeeById_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(validResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployeeById_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployeeById_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testGetEmployeeById_SupervisorRole_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(validResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk());
    }

    // ========== UPDATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_ValidInput_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                .thenReturn(validResponseDTO);

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(patch("/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_HRRole_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                .thenReturn(validResponseDTO);

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testUpdateEmployee_WorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_PartialUpdate_Returns200() throws Exception {
        // Arrange
        EmployeeRequestDTO partialDTO = new EmployeeRequestDTO();
        partialDTO.setDepartment("Logistics");
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                .thenReturn(validResponseDTO);

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialDTO)))
                .andExpect(status().isOk());
    }

    // ========== DELETE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).softDeleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).softDeleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Employee not found"))
                .when(employeeService).softDeleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testDeleteEmployee_HRRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).softDeleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testDeleteEmployee_WorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).softDeleteEmployee(anyLong());
    }

    @Test
    public void testDeleteEmployee_Unauthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).softDeleteEmployee(anyLong());
    }

    // ========== CONTENT TYPE AND HEADER TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_UnsupportedMediaType_Returns415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_AcceptHeader_ReturnsJson() throws Exception {
        // Arrange
        Page<EmployeeResponseDTO> page = new PageImpl<>(Arrays.asList(validResponseDTO));
        when(employeeService.getAllEmployees(any(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .accept(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ========== PAGINATION BOUNDARY TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_MaxPageSize_Returns200() throws Exception {
        // Arrange
        Page<EmployeeResponseDTO> page = new PageImpl<>(Arrays.asList(validResponseDTO));
        when(employeeService.getAllEmployees(any(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllEmployees_ExceedMaxPageSize_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "1001"))
                .andExpect(status().isBadRequest());
    }
}