package com.company.wems.employee.controller;

import com.company.wems.employee.dto.EmployeeDTO;
import com.company.wems.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for EmployeeController
 * Tests cover REST endpoints, security, validation, and HTTP responses
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Tests")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeDTO validEmployeeDTO;

    @BeforeEach
    void setUp() {
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setId(1L);
        validEmployeeDTO.setBadgeId("EMP001");
        validEmployeeDTO.setFirstName("John");
        validEmployeeDTO.setLastName("Doe");
        validEmployeeDTO.setEmail("john.doe@company.com");
        validEmployeeDTO.setPhone("+1234567890");
        validEmployeeDTO.setRole("WORKER");
        validEmployeeDTO.setDepartment("Warehouse");
        validEmployeeDTO.setHireDate(LocalDate.now());
    }

    // ==================== CREATE EMPLOYEE ENDPOINT TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/employees - Valid Input - Should Return 201 Created")
    void testCreateEmployee_WithValidInput_ShouldReturn201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("POST /api/employees - HR Role - Should Return 201 Created")
    void testCreateEmployee_WithHRRole_ShouldReturn201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("POST /api/employees - Worker Role - Should Return 403 Forbidden")
    void testCreateEmployee_WithWorkerRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/employees - Invalid Email - Should Return 400 Bad Request")
    void testCreateEmployee_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Arrange
        validEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/employees - Missing Required Fields - Should Return 400 Bad Request")
    void testCreateEmployee_WithMissingFields_ShouldReturn400() throws Exception {
        // Arrange
        validEmployeeDTO.setFirstName(null);
        validEmployeeDTO.setLastName(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/employees - Empty Request Body - Should Return 400 Bad Request")
    void testCreateEmployee_WithEmptyBody_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== UPDATE EMPLOYEE ENDPOINT TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/employees/{id} - Valid Input - Should Return 200 OK")
    void testUpdateEmployee_WithValidInput_ShouldReturn200() throws Exception {
        // Arrange
        Long employeeId = 1L;
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("PUT /api/employees/{id} - HR Role - Should Return 200 OK")
    void testUpdateEmployee_WithHRRole_ShouldReturn200() throws Exception {
        // Arrange
        Long employeeId = 1L;
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("PUT /api/employees/{id} - Supervisor Role - Should Return 403 Forbidden")
    void testUpdateEmployee_WithSupervisorRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/employees/{id} - Invalid ID Format - Should Return 400 Bad Request")
    void testUpdateEmployee_WithInvalidIdFormat_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET EMPLOYEE ENDPOINT TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/employees/{id} - Valid ID - Should Return 200 OK")
    void testGetEmployee_WithValidId_ShouldReturn200() throws Exception {
        // Arrange
        Long employeeId = 1L;
        when(employeeService.getEmployeeById(employeeId)).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).getEmployeeById(employeeId);
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("GET /api/employees/{id} - Supervisor Role - Should Return 200 OK")
    void testGetEmployee_WithSupervisorRole_ShouldReturn200() throws Exception {
        // Arrange
        Long employeeId = 1L;
        when(employeeService.getEmployeeById(employeeId)).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/" + employeeId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("GET /api/employees/{id} - Worker Role - Should Return 403 Forbidden")
    void testGetEmployee_WithWorkerRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/employees - Valid Request - Should Return 200 OK with Page")
    void testGetAllEmployees_WithValidRequest_ShouldReturn200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(validEmployeeDTO), PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/employees - Empty Result - Should Return 200 OK with Empty Page")
    void testGetAllEmployees_WithEmptyResult_ShouldReturn200() throws Exception {
        // Arrange
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ==================== DELETE EMPLOYEE ENDPOINT TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/employees/{id} - Valid ID - Should Return 204 No Content")
    void testDeleteEmployee_WithValidId_ShouldReturn204() throws Exception {
        // Arrange
        Long employeeId = 1L;
        doNothing().when(employeeService).deleteEmployee(employeeId);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/" + employeeId))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(employeeId);
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("DELETE /api/employees/{id} - HR Role - Should Return 204 No Content")
    void testDeleteEmployee_WithHRRole_ShouldReturn204() throws Exception {
        // Arrange
        Long employeeId = 1L;
        doNothing().when(employeeService).deleteEmployee(employeeId);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/" + employeeId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("DELETE /api/employees/{id} - Supervisor Role - Should Return 403 Forbidden")
    void testDeleteEmployee_WithSupervisorRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/employees/{id} - Invalid ID Format - Should Return 400 Bad Request")
    void testDeleteEmployee_WithInvalidIdFormat_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    // ==================== BOUNDARY AND EDGE CASE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/employees - Maximum Length Fields - Should Return 201 Created")
    void testCreateEmployee_WithMaxLengthFields_ShouldReturn201() throws Exception {
        // Arrange
        validEmployeeDTO.setBadgeId("A".repeat(50));
        validEmployeeDTO.setFirstName("B".repeat(100));
        validEmployeeDTO.setLastName("C".repeat(100));
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/employees - Large Page Size - Should Return 200 OK")
    void testGetAllEmployees_WithLargePageSize_ShouldReturn200() throws Exception {
        // Arrange
        Page<EmployeeDTO> employeePage = new PageImpl<>(Arrays.asList(validEmployeeDTO), PageRequest.of(0, 1000), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/employees/{id} - Zero ID - Should Return 400 Bad Request")
    void testGetEmployee_WithZeroId_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/employees/{id} - Negative ID - Should Return 400 Bad Request")
    void testGetEmployee_WithNegativeId_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/-1"))
                .andExpect(status().isBadRequest());
    }
}