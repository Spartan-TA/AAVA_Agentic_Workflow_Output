package com.company.wms.employee;

import com.company.wms.common.ApiResponse;
import com.company.wms.common.PageResponse;
import com.company.wms.employee.dto.EmployeeCreateRequest;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.dto.EmployeeUpdateRequest;
import com.company.wms.exception.DuplicateResourceException;
import com.company.wms.exception.ResourceNotFoundException;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Comprehensive unit tests for EmployeeController
 * 
 * Tests cover:
 * - All REST endpoints
 * - Request validation
 * - Response status codes
 * - Security/authorization
 * - JSON serialization/deserialization
 * - Edge cases and error handling
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController Unit Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private EmployeeMapper employeeMapper;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;
    private EmployeeCreateRequest createRequest;
    private EmployeeUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setPhone("+1234567890");
        testEmployee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setStatus("ACTIVE");
        testEmployee.setActive(true);

        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setFirstName("John");
        testEmployeeDTO.setLastName("Doe");
        testEmployeeDTO.setEmail("john.doe@example.com");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setStatus("ACTIVE");

        createRequest = new EmployeeCreateRequest();
        createRequest.setBadgeId("EMP001");
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john.doe@example.com");
        createRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        createRequest.setRole("WORKER");
        createRequest.setDepartment("Warehouse");
        createRequest.setHireDate(LocalDate.now());

        updateRequest = new EmployeeUpdateRequest();
        updateRequest.setBadgeId("EMP001");
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@example.com");
        updateRequest.setRole("SUPERVISOR");
        updateRequest.setDepartment("Logistics");
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return all employees with pagination - Normal case")
    void testGetAllEmployees_Success() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 20), 1);
        Page<EmployeeDTO> dtoPage = new PageImpl<>(Arrays.asList(testEmployeeDTO), PageRequest.of(0, 20), 1);
        
        when(employeeService.getAllEmployees(any())).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDir", "ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(employeeService, times(1)).getAllEmployees(any());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 403 when WORKER tries to access all employees")
    void testGetAllEmployees_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).getAllEmployees(any());
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user tries to access")
    void testGetAllEmployees_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should handle invalid page parameter")
    void testGetAllEmployees_InvalidPageParameter() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should handle invalid size parameter")
    void testGetAllEmployees_InvalidSizeParameter() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET ACTIVE EMPLOYEES TESTS ====================

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Should return active employees")
    void testGetActiveEmployees_Success() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 20), 1);
        
        when(employeeService.getActiveEmployees(any())).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));

        verify(employeeService, times(1)).getActiveEmployees(any());
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return employee by ID - Normal case")
    void testGetEmployeeById_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.badgeId").value("EMP001"));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 404 when employee not found")
    void testGetEmployeeById_NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found with ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(employeeService, times(1)).getEmployeeById(999L);
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should handle invalid ID format")
    void testGetEmployeeById_InvalidIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET EMPLOYEE BY BADGE ID TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return employee by badge ID")
    void testGetEmployeeByBadgeId_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/badge/EMP001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.badgeId").value("EMP001"));

        verify(employeeService, times(1)).getEmployeeByBadgeId("EMP001");
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 404 when badge ID not found")
    void testGetEmployeeByBadgeId_NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByBadgeId("INVALID"))
                .thenThrow(new ResourceNotFoundException("Employee not found with badge ID: INVALID"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/badge/INVALID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should create employee successfully with valid data")
    void testCreateEmployee_Success() throws Exception {
        // Arrange
        when(employeeMapper.toEntity(any(EmployeeCreateRequest.class))).thenReturn(testEmployee);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Employee created successfully"))
                .andExpect(jsonPath("$.data.badgeId").value("EMP001"));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 400 when required fields missing")
    void testCreateEmployee_MissingRequiredFields() throws Exception {
        // Arrange
        EmployeeCreateRequest invalidRequest = new EmployeeCreateRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 409 when badge ID already exists")
    void testCreateEmployee_DuplicateBadgeId() throws Exception {
        // Arrange
        when(employeeMapper.toEntity(any(EmployeeCreateRequest.class))).thenReturn(testEmployee);
        when(employeeService.createEmployee(any(Employee.class)))
                .thenThrow(new DuplicateResourceException("Employee with badge ID EMP001 already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 400 when invalid email format")
    void testCreateEmployee_InvalidEmailFormat() throws Exception {
        // Arrange
        createRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 403 when WORKER tries to create employee")
    void testCreateEmployee_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 400 when request body is null")
    void testCreateEmployee_NullRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 400 when request body is malformed JSON")
    void testCreateEmployee_MalformedJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should update employee successfully")
    void testUpdateEmployee_Success() throws Exception {
        // Arrange
        when(employeeMapper.toEntity(any(EmployeeUpdateRequest.class))).thenReturn(testEmployee);
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Employee updated successfully"));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 404 when updating non-existent employee")
    void testUpdateEmployee_NotFound() throws Exception {
        // Arrange
        when(employeeMapper.toEntity(any(EmployeeUpdateRequest.class))).thenReturn(testEmployee);
        when(employeeService.updateEmployee(eq(999L), any(Employee.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found with ID: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 403 when WORKER tries to update employee")
    void testUpdateEmployee_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete employee successfully")
    void testDeleteEmployee_Success() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Employee deleted successfully"));

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when deleting non-existent employee")
    void testDeleteEmployee_NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found with ID: 999"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return 403 when HR tries to delete employee")
    void testDeleteEmployee_ForbiddenForHR() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    // ==================== SEARCH EMPLOYEES TESTS ====================

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Should search employees successfully")
    void testSearchEmployees_Success() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 20), 1);
        
        when(employeeService.searchEmployees(eq("John"), any())).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/search")
                .param("query", "John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].firstName").value("John"));

        verify(employeeService, times(1)).searchEmployees(eq("John"), any());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return empty result when search term not found")
    void testSearchEmployees_NoResults() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        when(employeeService.searchEmployees(eq("NonExistent"), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees/search")
                .param("query", "NonExistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should handle empty search query")
    void testSearchEmployees_EmptyQuery() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        when(employeeService.searchEmployees(eq(""), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees/search")
                .param("query", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== GET EMPLOYEES BY DEPARTMENT TESTS ====================

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Should return employees by department")
    void testGetEmployeesByDepartment_Success() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 20), 1);
        
        when(employeeService.getEmployeesByDepartment(eq("Warehouse"), any())).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/department/Warehouse")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].department").value("Warehouse"));
    }

    // ==================== GET EMPLOYEES BY ROLE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return employees by role")
    void testGetEmployeesByRole_Success() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(0, 20), 1);
        
        when(employeeService.getEmployeesByRole(eq("WORKER"), any())).thenReturn(employeePage);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/role/WORKER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].role").value("WORKER"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Should return 403 when SUPERVISOR tries to get employees by role")
    void testGetEmployeesByRole_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/role/WORKER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ==================== GET EMPLOYEE STATISTICS TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Should return employee statistics")
    void testGetEmployeeStats_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeCount()).thenReturn(100L);
        when(employeeService.getActiveEmployeeCount()).thenReturn(85L);

        // Act & Assert
        mockMvc.perform(get("/api/employees/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEmployees").value(100))
                .andExpect(jsonPath("$.data.activeEmployees").value(85));

        verify(employeeService, times(1)).getEmployeeCount();
        verify(employeeService, times(1)).getActiveEmployeeCount();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Should return 403 when WORKER tries to get statistics")
    void testGetEmployeeStats_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}