package com.warehouse.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.dto.EmployeeDTO;
import com.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests cover REST API endpoints, validation, security, and HTTP status codes
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
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Day Shift");
        testEmployeeDTO.setHireDate(LocalDate.of(2024, 1, 1));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== CREATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test POST /api/employees - valid employee - returns 201 Created")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_ValidData_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.role").value("WORKER"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test POST /api/employees - missing badge ID - returns 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_MissingBadgeId_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test POST /api/employees - empty badge ID - returns 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_EmptyBadgeId_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST /api/employees - missing name - returns 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_MissingName_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST /api/employees - invalid role - returns 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_InvalidRole_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setRole("INVALID_ROLE");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST /api/employees - duplicate badge ID - returns 409 Conflict")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_DuplicateBadgeId_Returns409() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Test POST /api/employees - unauthorized - returns 401 Unauthorized")
    public void testCreateEmployee_Unauthorized_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test POST /api/employees - forbidden role - returns 403 Forbidden")
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployee_ForbiddenRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test POST /api/employees - malformed JSON - returns 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_MalformedJSON_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // ========== GET ALL EMPLOYEES ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test GET /api/employees - returns 200 OK with employee list")
    @WithMockUser(roles = "HR")
    public void testGetAllEmployees_Success_Returns200() throws Exception {
        // Arrange
        EmployeeDTO employee2 = new EmployeeDTO();
        employee2.setBadgeId("EMP002");
        employee2.setName("Jane Smith");
        employee2.setRole("SUPERVISOR");

        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO, employee2);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$[1].badgeId").value("EMP002"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test GET /api/employees - empty list - returns 200 OK")
    @WithMockUser(roles = "HR")
    public void testGetAllEmployees_EmptyList_Returns200() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Test GET /api/employees - unauthorized - returns 401 Unauthorized")
    public void testGetAllEmployees_Unauthorized_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET EMPLOYEE BY ID ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test GET /api/employees/{id} - valid ID - returns 200 OK")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeById_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @DisplayName("Test GET /api/employees/{id} - not found - returns 404 Not Found")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeById_NotFound_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new IllegalArgumentException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test GET /api/employees/{id} - invalid ID format - returns 400 Bad Request")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeById_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test GET /api/employees/{id} - negative ID - returns 400 Bad Request")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeById_NegativeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(-1L))
                .thenThrow(new IllegalArgumentException("Invalid employee ID"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== UPDATE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test PUT /api/employees/{id} - valid update - returns 200 OK")
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_ValidData_Returns200() throws Exception {
        // Arrange
        testEmployeeDTO.setName("John Doe Updated");
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe Updated"));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test PUT /api/employees/{id} - not found - returns 404 Not Found")
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_NotFound_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(999L), any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test PUT /api/employees/{id} - invalid data - returns 400 Bad Request")
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_InvalidData_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setName("");

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test PUT /api/employees/{id} - forbidden role - returns 403 Forbidden")
    @WithMockUser(roles = "WORKER")
    public void testUpdateEmployee_ForbiddenRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());
    }

    // ========== DELETE EMPLOYEE ENDPOINT TESTS ==========

    @Test
    @DisplayName("Test DELETE /api/employees/{id} - valid ID - returns 204 No Content")
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @DisplayName("Test DELETE /api/employees/{id} - not found - returns 404 Not Found")
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_NotFound_Returns404() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Employee not found"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test DELETE /api/employees/{id} - forbidden role - returns 403 Forbidden")
    @WithMockUser(roles = "WORKER")
    public void testDeleteEmployee_ForbiddenRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test DELETE /api/employees/{id} - already deleted - returns 409 Conflict")
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_AlreadyDeleted_Returns409() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("Employee already deleted"))
                .when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isConflict());
    }

    // ========== CONTENT TYPE TESTS ==========

    @Test
    @DisplayName("Test POST /api/employees - unsupported media type - returns 415")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_UnsupportedMediaType_Returns415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test POST /api/employees - maximum length fields - returns 201 Created")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_MaxLengthFields_Returns201() throws Exception {
        // Arrange
        testEmployeeDTO.setName("A".repeat(255));
        testEmployeeDTO.setDepartment("D".repeat(100));
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test POST /api/employees - special characters - returns 201 Created")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployee_SpecialCharacters_Returns201() throws Exception {
        // Arrange
        testEmployeeDTO.setName("O'Brien-Smith Jr.");
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test GET /api/employees - large result set - returns 200 OK")
    @WithMockUser(roles = "HR")
    public void testGetAllEmployees_LargeResultSet_Returns200() throws Exception {
        // Arrange
        List<EmployeeDTO> largeList = Arrays.asList(new EmployeeDTO[1000]);
        when(employeeService.getAllEmployees()).thenReturn(largeList);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1000));
    }
}