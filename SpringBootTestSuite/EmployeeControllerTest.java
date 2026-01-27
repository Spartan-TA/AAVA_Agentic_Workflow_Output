package com.warehouse.ems.controller.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.domain.employee.Role;
import com.warehouse.ems.dto.employee.EmployeeRequest;
import com.warehouse.ems.dto.employee.EmployeeResponse;
import com.warehouse.ems.service.employee.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for EmployeeController.
 * Tests cover all REST endpoints, security, validation, and error handling.
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeRequest testRequest;
    private EmployeeResponse testResponse;

    @BeforeEach
    public void setUp() {
        testRequest = new EmployeeRequest();
        testRequest.setBadgeId("EMP001");
        testRequest.setName("John Doe");
        testRequest.setRole(Role.WORKER);
        testRequest.setDepartment("Warehouse");
        testRequest.setShiftGroup("Morning");
        testRequest.setHireDate(LocalDate.of(2023, 1, 15));
        testRequest.setStatus("ACTIVE");

        testResponse = EmployeeResponse.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(Role.WORKER)
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_AsAdmin_Success() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).create(any(EmployeeRequest.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_AsHR_Success() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployee_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).create(any(EmployeeRequest.class));
    }

    @Test
    public void testCreateEmployee_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_InvalidInput_BadRequest() throws Exception {
        // Arrange - missing required field
        testRequest.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_EmptyBadgeId_BadRequest() throws Exception {
        // Arrange
        testRequest.setBadgeId("");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_DuplicateBadgeId_BadRequest() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeRequest.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetById_AsAdmin_Success() throws Exception {
        // Arrange
        when(employeeService.getById(1L)).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testGetById_AsHR_Success() throws Exception {
        // Arrange
        when(employeeService.getById(1L)).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testGetById_AsSupervisor_Success() throws Exception {
        // Arrange
        when(employeeService.getById(1L)).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetById_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetById_NonExistent_NotFound() throws Exception {
        // Arrange
        when(employeeService.getById(999L))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetById_InvalidId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAll_NoDepartmentFilter_Success() throws Exception {
        // Arrange
        Page<EmployeeResponse> page = new PageImpl<>(Arrays.asList(testResponse));
        when(employeeService.getAll(anyString(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAll_WithDepartmentFilter_Success() throws Exception {
        // Arrange
        Page<EmployeeResponse> page = new PageImpl<>(Arrays.asList(testResponse));
        when(employeeService.getAll(anyString(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("department", "Warehouse")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testGetAll_AsSupervisor_Success() throws Exception {
        // Arrange
        Page<EmployeeResponse> page = new PageImpl<>(Arrays.asList(testResponse));
        when(employeeService.getAll(anyString(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetAll_AsWorker_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAll_EmptyResult_Success() throws Exception {
        // Arrange
        Page<EmployeeResponse> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.getAll(anyString(), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAll_LargePageSize_Success() throws Exception {
        // Arrange
        Page<EmployeeResponse> page = new PageImpl<>(Arrays.asList(testResponse));
        when(employeeService.getAll(anyString(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("size", "1000"))
                .andExpect(status().isOk());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdate_AsAdmin_Success() throws Exception {
        // Arrange
        when(employeeService.update(anyLong(), any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testUpdate_AsHR_Success() throws Exception {
        // Arrange
        when(employeeService.update(anyLong(), any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testUpdate_AsSupervisor_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdate_NonExistent_NotFound() throws Exception {
        // Arrange
        when(employeeService.update(anyLong(), any(EmployeeRequest.class)))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdate_InvalidInput_BadRequest() throws Exception {
        // Arrange
        testRequest.setName(null);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDelete_AsAdmin_Success() throws Exception {
        // Arrange
        doNothing().when(employeeService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).delete(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testDelete_AsHR_Success() throws Exception {
        // Arrange
        doNothing().when(employeeService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testDelete_AsSupervisor_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).delete(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDelete_NonExistent_NotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Employee not found"))
                .when(employeeService).delete(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDelete_InvalidId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/invalid")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_MaxLengthFields_Success() throws Exception {
        // Arrange
        testRequest.setBadgeId("A".repeat(32));
        testRequest.setName("B".repeat(128));
        when(employeeService.create(any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_SpecialCharacters_Success() throws Exception {
        // Arrange
        testRequest.setName("O'Brien-Smith Jr.");
        when(employeeService.create(any(EmployeeRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAll_NegativePage_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAll_ZeroPageSize_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}