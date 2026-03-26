package com.warehouse.employee.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.management.dto.EmployeeDTO;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import com.warehouse.employee.management.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests all REST endpoints with MockMvc
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController Test Suite")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO testEmployeeDTO;
    private List<EmployeeDTO> employeeDTOList;

    @BeforeEach
    void setUp() {
        // Initialize test employee DTO
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setFirstName("John");
        testEmployeeDTO.setLastName("Doe");
        testEmployeeDTO.setEmail("john.doe@warehouse.com");
        testEmployeeDTO.setPhoneNumber("+1234567890");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Shipping");
        testEmployeeDTO.setShiftGroup("Morning");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");

        // Initialize employee DTO list
        EmployeeDTO employee2 = new EmployeeDTO();
        employee2.setId(2L);
        employee2.setBadgeId("EMP002");
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("jane.smith@warehouse.com");

        employeeDTOList = Arrays.asList(testEmployeeDTO, employee2);
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees - Normal Case - Returns 200 and Employee List")
    void testGetAllEmployees_ValidRequest_Returns200AndEmployeeList() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(employeeDTOList);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].badgeId", is("EMP001")))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[1].badgeId", is("EMP002")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test GET /api/employees - HR Role - Returns 200")
    void testGetAllEmployees_HRRole_Returns200() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(employeeDTOList);

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test GET /api/employees - Unauthorized - Returns 401")
    void testGetAllEmployees_Unauthorized_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test GET /api/employees - Worker Role - Returns 403")
    void testGetAllEmployees_WorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees - Empty List - Returns 200 and Empty Array")
    void testGetAllEmployees_EmptyList_Returns200AndEmptyArray() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} - Valid ID - Returns 200 and Employee")
    void testGetEmployeeById_ValidId_Returns200AndEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@warehouse.com")));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} - Invalid ID - Returns 404")
    void testGetEmployeeById_InvalidId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(999L);
    }

    @Test
    @DisplayName("Test GET /api/employees/{id} - Unauthorized - Returns 401")
    void testGetEmployeeById_Unauthorized_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} - Negative ID - Returns 404")
    void testGetEmployeeById_NegativeId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(-1L))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/-1"))
                .andExpect(status().isNotFound());
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees - Valid DTO - Returns 201 and Created Employee")
    void testCreateEmployee_ValidDTO_Returns201AndCreatedEmployee() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.firstName", is("John")));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test POST /api/employees - HR Role - Returns 201")
    void testCreateEmployee_HRRole_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test POST /api/employees - Unauthorized - Returns 401")
    void testCreateEmployee_Unauthorized_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test POST /api/employees - Worker Role - Returns 403")
    void testCreateEmployee_WorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees - Missing Required Field - Returns 400")
    void testCreateEmployee_MissingRequiredField_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees - Invalid Email Format - Returns 400")
    void testCreateEmployee_InvalidEmailFormat_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees - Empty Request Body - Returns 400")
    void testCreateEmployee_EmptyRequestBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees - Duplicate BadgeId - Returns 400")
    void testCreateEmployee_DuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/employees/{id} - Valid ID and DTO - Returns 200 and Updated Employee")
    void testUpdateEmployee_ValidIdAndDTO_Returns200AndUpdatedEmployee() throws Exception {
        // Arrange
        EmployeeDTO updatedDTO = new EmployeeDTO();
        updatedDTO.setId(1L);
        updatedDTO.setBadgeId("EMP001");
        updatedDTO.setFirstName("John Updated");
        updatedDTO.setLastName("Doe Updated");
        updatedDTO.setEmail("john.updated@warehouse.com");
        updatedDTO.setPhoneNumber("+9876543210");
        updatedDTO.setRole("SUPERVISOR");
        updatedDTO.setDepartment("Receiving");
        updatedDTO.setShiftGroup("Evening");
        updatedDTO.setHireDate(LocalDate.of(2023, 1, 15));
        updatedDTO.setStatus("ACTIVE");

        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class))).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John Updated")))
                .andExpect(jsonPath("$.role", is("SUPERVISOR")));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/employees/{id} - Invalid ID - Returns 404")
    void testUpdateEmployee_InvalidId_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test PUT /api/employees/{id} - Unauthorized - Returns 401")
    void testUpdateEmployee_Unauthorized_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test PUT /api/employees/{id} - Worker Role - Returns 403")
    void testUpdateEmployee_WorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /api/employees/{id} - Missing Required Field - Returns 400")
    void testUpdateEmployee_MissingRequiredField_Returns400() throws Exception {
        // Arrange
        testEmployeeDTO.setFirstName(null);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test DELETE /api/employees/{id} - Valid ID - Returns 204")
    void testDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test DELETE /api/employees/{id} - Invalid ID - Returns 404")
    void testDeleteEmployee_InvalidId_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test DELETE /api/employees/{id} - Unauthorized - Returns 401")
    void testDeleteEmployee_Unauthorized_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test DELETE /api/employees/{id} - HR Role - Returns 403")
    void testDeleteEmployee_HRRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test DELETE /api/employees/{id} - Supervisor Role - Returns 403")
    void testDeleteEmployee_SupervisorRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ==================== EDGE CASE AND BOUNDARY TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees - Maximum Length Fields - Returns 201")
    void testCreateEmployee_MaximumLengthFields_Returns201() throws Exception {
        // Arrange
        String longString = "A".repeat(255);
        testEmployeeDTO.setFirstName(longString);
        testEmployeeDTO.setLastName(longString);
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees - Special Characters in Name - Returns 201")
    void testCreateEmployee_SpecialCharactersInName_Returns201() throws Exception {
        // Arrange
        testEmployeeDTO.setFirstName("Jean-Pierre");
        testEmployeeDTO.setLastName("O'Connor");
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /api/employees/{id} - Zero ID - Returns 404")
    void testGetEmployeeById_ZeroId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(0L))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees - Malformed JSON - Returns 400")
    void testCreateEmployee_MalformedJSON_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /api/employees - Future Hire Date - Returns 201")
    void testCreateEmployee_FutureHireDate_Returns201() throws Exception {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(30));
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());
    }
}