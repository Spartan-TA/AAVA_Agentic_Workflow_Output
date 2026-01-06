package com.company.wms.employee.controller;

import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Covers REST endpoints, security, validation, and edge cases
 */
@SpringBootTest
@AutoConfigureMockMvc
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
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Day Shift");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_AdminRole_ReturnsCreated() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    void createEmployee_HRRole_ReturnsCreated() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void createEmployee_WorkerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    void createEmployee_NoAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setName(""); // Invalid empty name

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_MissingRequiredField_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId(null); // Missing required field

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_MalformedJSON_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
            .andExpect(status().isBadRequest());
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployee_AdminRole_ReturnsEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getEmployee_HRRole_ReturnsEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void getEmployee_SupervisorRole_ReturnsEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "EMP001", roles = "WORKER")
    void getEmployee_OwnProfile_ReturnsEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "EMP002", roles = "WORKER")
    void getEmployee_OtherProfile_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployee_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/employees/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployee_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/invalid"))
            .andExpect(status().isBadRequest());
    }

    // ========== LIST EMPLOYEES TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEmployees_AdminRole_ReturnsPagedResults() throws Exception {
        // Arrange
        Page<EmployeeDTO> page = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.listEmployees(any(), any()))
            .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("John Doe"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEmployees_WithDepartmentFilter_ReturnsFilteredResults() throws Exception {
        // Arrange
        Page<EmployeeDTO> page = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.listEmployees(any(), any()))
            .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("department", "Warehouse")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEmployees_WithStatusFilter_ReturnsFilteredResults() throws Exception {
        // Arrange
        Page<EmployeeDTO> page = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.listEmployees(any(), any()))
            .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void listEmployees_WorkerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEmployees_InvalidPageNumber_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "-1")
                .param("size", "10"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEmployees_InvalidPageSize_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEmployees_LargePageSize_ReturnsResults() throws Exception {
        // Arrange
        Page<EmployeeDTO> page = new PageImpl<>(Arrays.asList(testEmployeeDTO));
        when(employeeService.listEmployees(any(), any()))
            .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "1000"))
            .andExpect(status().isOk());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEmployee_AdminRole_ReturnsUpdatedEmployee() throws Exception {
        // Arrange
        testEmployeeDTO.setName("Jane Doe");
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class)))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void updateEmployee_HRRole_ReturnsUpdatedEmployee() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class)))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void updateEmployee_WorkerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEmployee_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(999L), any(EmployeeDTO.class)))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDTO.setName(""); // Invalid empty name

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isBadRequest());
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEmployee_AdminRole_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).softDeleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1"))
            .andExpect(status().isNoContent());

        verify(employeeService, times(1)).softDeleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    void deleteEmployee_HRRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1"))
            .andExpect(status().isForbidden());

        verify(employeeService, never()).softDeleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void deleteEmployee_WorkerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEmployee_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
            .when(employeeService).softDeleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/employees/999"))
            .andExpect(status().isNotFound());
    }

    // ========== PATCH EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void patchEmployee_AdminRole_ReturnsUpdatedEmployee() throws Exception {
        // Arrange
        EmployeeDTO patchDTO = new EmployeeDTO();
        patchDTO.setStatus("INACTIVE");
        
        testEmployeeDTO.setStatus("INACTIVE");
        when(employeeService.patchEmployee(eq(1L), any(EmployeeDTO.class)))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patchEmployee_EmptyBody_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_MaxLengthFields_ReturnsCreated() throws Exception {
        // Arrange
        testEmployeeDTO.setName("A".repeat(255));
        testEmployeeDTO.setDepartment("D".repeat(100));
        
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEmployees_EmptyResult_ReturnsEmptyPage() throws Exception {
        // Arrange
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.listEmployees(any(), any()))
            .thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployee_IdZero_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployee_NegativeId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_SpecialCharactersInName_ReturnsCreated() throws Exception {
        // Arrange
        testEmployeeDTO.setName("O'Brien-Smith Jr.");
        
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
            .thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
            .andExpect(status().isCreated());
    }
}