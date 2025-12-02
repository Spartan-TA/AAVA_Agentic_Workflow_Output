package com.warehouse.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.dto.EmployeeDTO;
import com.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController.
 * Tests REST API endpoints with MockMvc and security context.
 */
@WebMvcTest(EmployeeController.class)
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
        testEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_WithValidData_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.role").value("WORKER"))
                .andExpect(jsonPath("$.department").value("Warehouse"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_WithHRRole_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testCreateEmployee_WithSupervisorRole_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployee_WithWorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_WithInvalidData_Returns400() throws Exception {
        // Arrange - Create invalid DTO (missing required fields)
        EmployeeDTO invalidDTO = EmployeeDTO.builder()
                .name("") // Empty name
                .badgeId("")
                .role("")
                .department("")
                .status("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_WithNullName_Returns400() throws Exception {
        // Arrange
        EmployeeDTO invalidDTO = EmployeeDTO.builder()
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_WithFutureHireDate_Returns400() throws Exception {
        // Arrange
        EmployeeDTO invalidDTO = EmployeeDTO.builder()
                .name("Test Employee")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now().plusDays(1)) // Future date
                .status("ACTIVE")
                .build();

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployee_WithExistingId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L)).thenReturn(Optional.of(testEmployeeDTO));

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).getEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetEmployee_WithWorkerRole_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L)).thenReturn(Optional.of(testEmployeeDTO));

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployee_WithNonExistingId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployee(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployee_WithZeroId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(0L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployee_WithNegativeId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/-1"))
                .andExpect(status().isNotFound());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_WithValidData_Returns200() throws Exception {
        // Arrange
        EmployeeDTO updatedDTO = EmployeeDTO.builder()
                .id(1L)
                .name("John Updated")
                .badgeId("EMP001")
                .role("SUPERVISOR")
                .department("New Department")
                .status("ACTIVE")
                .build();

        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"))
                .andExpect(jsonPath("$.department").value("New Department"));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_WithHRRole_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testUpdateEmployee_WithWorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_WithNonExistingId_Returns400() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(999L), any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployee_WithInvalidData_Returns400() throws Exception {
        // Arrange - Invalid DTO with empty required fields
        EmployeeDTO invalidDTO = EmployeeDTO.builder()
                .name("")
                .badgeId("")
                .role("")
                .department("")
                .status("")
                .build();

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_WithAdminRole_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testDeleteEmployee_WithHRRole_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testDeleteEmployee_WithSupervisorRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testDeleteEmployee_WithWorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_WithNonExistingId_Returns400() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Employee not found"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/employees/999")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ========== LIST EMPLOYEES TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListEmployees_WithoutFilters_Returns200() throws Exception {
        // Arrange
        EmployeeDTO employee2 = EmployeeDTO.builder()
                .id(2L)
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .status("ACTIVE")
                .build();

        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDTO, employee2));
        when(employeeService.listEmployees(isNull(), any(Pageable.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[1].name").value("Jane Smith"));

        verify(employeeService, times(1)).listEmployees(isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testListEmployees_WithWorkerRole_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.listEmployees(isNull(), any(Pageable.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListEmployees_WithStatusFilter_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.listEmployees(eq("ACTIVE"), any(Pageable.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

        verify(employeeService, times(1)).listEmployees(eq("ACTIVE"), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListEmployees_WithPagination_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(
                Arrays.asList(testEmployeeDTO),
                PageRequest.of(1, 5),
                10
        );
        when(employeeService.listEmployees(isNull(), any(Pageable.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListEmployees_WithCustomPageSize_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(
                Arrays.asList(testEmployeeDTO),
                PageRequest.of(0, 50),
                1
        );
        when(employeeService.listEmployees(isNull(), any(Pageable.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListEmployees_WithEmptyResult_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.listEmployees(isNull(), any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListEmployees_WithDefaultPagination_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(
                Arrays.asList(testEmployeeDTO),
                PageRequest.of(0, 20),
                1
        );
        when(employeeService.listEmployees(isNull(), any(Pageable.class))).thenReturn(employeePage);

        // Act & Assert - Default page=0, size=20
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    // ========== SECURITY TESTS ==========

    @Test
    public void testCreateEmployee_WithoutAuthentication_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    public void testGetEmployee_WithoutAuthentication_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).getEmployee(anyLong());
    }

    @Test
    public void testUpdateEmployee_WithoutAuthentication_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    public void testDeleteEmployee_WithoutAuthentication_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    public void testListEmployees_WithoutAuthentication_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).listEmployees(anyString(), any(Pageable.class));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_WithExtraLongName_Returns400() throws Exception {
        // Arrange - Name exceeding 100 characters
        EmployeeDTO invalidDTO = EmployeeDTO.builder()
                .name("A".repeat(101))
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListEmployees_WithNegativePage_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListEmployees_WithZeroPageSize_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetEmployee_WithInvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_WithMalformedJSON_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListEmployees_WithMultipleFilters_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.listEmployees(eq("ACTIVE"), any(Pageable.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
}