package com.warehouse.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.dto.EmployeeDTO;
import com.warehouse.exception.DuplicateResourceException;
import com.warehouse.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests cover all REST endpoints with normal cases, boundary conditions, and edge cases
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO validEmployeeDTO;

    @BeforeEach
    public void setUp() {
        // Arrange: Set up valid test data
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setId(1L);
        validEmployeeDTO.setBadgeId("EMP001");
        validEmployeeDTO.setFirstName("John");
        validEmployeeDTO.setLastName("Doe");
        validEmployeeDTO.setEmail("john.doe@warehouse.com");
        validEmployeeDTO.setRole(EmployeeRole.WORKER);
        validEmployeeDTO.setDepartment("Warehouse");
        validEmployeeDTO.setShiftGroup("Morning");
        validEmployeeDTO.setHireDate(LocalDate.now());
        validEmployeeDTO.setStatus(EmployeeStatus.ACTIVE);
    }

    // ========== POST /api/employees (CREATE) TESTS ==========

    @Test
    public void testCreateEmployee_WithValidData_ShouldReturn201Created() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@warehouse.com")))
                .andExpect(jsonPath("$.role", is("WORKER")))
                .andExpect(jsonPath("$.department", is("Warehouse")));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ShouldReturn409Conflict() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
                .thenThrow(new DuplicateResourceException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Badge ID already exists")));
    }

    @Test
    public void testCreateEmployee_WithMissingBadgeId_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_WithEmptyBadgeId_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setBadgeId("");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_WithInvalidEmailFormat_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_WithMissingFirstName_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setFirstName(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_WithMissingLastName_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setLastName(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_WithEmptyRequestBody_ShouldReturn400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_WithMalformedJSON_ShouldReturn400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    // ========== GET /api/employees/{id} (READ BY ID) TESTS ==========

    @Test
    public void testGetEmployeeById_WithValidId_ShouldReturn200OK() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.firstName", is("John")));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    public void testGetEmployeeById_WithNonExistentId_ShouldReturn404NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Employee not found")));
    }

    @Test
    public void testGetEmployeeById_WithInvalidIdFormat_ShouldReturn400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetEmployeeById_WithNegativeId_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(-1L))
                .thenThrow(new IllegalArgumentException("Invalid employee ID"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== GET /api/employees (LIST ALL) TESTS ==========

    @Test
    public void testGetAllEmployees_WithDefaultPagination_ShouldReturn200OK() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(validEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 20), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].badgeId", is("EMP001")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    public void testGetAllEmployees_WithCustomPagination_ShouldReturn200OK() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(validEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees?page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    public void testGetAllEmployees_WithDepartmentFilter_ShouldReturn200OK() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(validEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 20), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees?department=Warehouse")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department", is("Warehouse")));
    }

    @Test
    public void testGetAllEmployees_WithEmptyResult_ShouldReturn200OKWithEmptyList() throws Exception {
        // Arrange
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    public void testGetAllEmployees_WithInvalidPageNumber_ShouldReturn400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees?page=-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllEmployees_WithInvalidPageSize_ShouldReturn400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees?size=0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== PUT /api/employees/{id} (UPDATE) TESTS ==========

    @Test
    public void testUpdateEmployee_WithValidData_ShouldReturn200OK() throws Exception {
        // Arrange
        EmployeeDTO updatedDTO = new EmployeeDTO();
        updatedDTO.setId(1L);
        updatedDTO.setFirstName("Jane");
        updatedDTO.setLastName("Smith");
        updatedDTO.setDepartment("Logistics");
        
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class))).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    public void testUpdateEmployee_WithNonExistentId_ShouldReturn404NotFound() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateEmployee_WithInvalidData_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        validEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateEmployee_WithEmptyRequestBody_ShouldReturn400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ========== DELETE /api/employees/{id} (SOFT DELETE) TESTS ==========

    @Test
    public void testDeleteEmployee_WithValidId_ShouldReturn204NoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    public void testDeleteEmployee_WithNonExistentId_ShouldReturn404NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
                .when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteEmployee_WithInvalidIdFormat_ShouldReturn400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== POST /api/employees/{id}/restore (RESTORE) TESTS ==========

    @Test
    public void testRestoreEmployee_WithValidId_ShouldReturn200OK() throws Exception {
        // Arrange
        when(employeeService.restoreEmployee(1L)).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees/1/restore")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.badgeId", is("EMP001")));

        verify(employeeService, times(1)).restoreEmployee(1L);
    }

    @Test
    public void testRestoreEmployee_WithNonExistentId_ShouldReturn404NotFound() throws Exception {
        // Arrange
        when(employeeService.restoreEmployee(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(post("/api/employees/999/restore")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ========== CONTENT TYPE TESTS ==========

    @Test
    public void testCreateEmployee_WithUnsupportedMediaType_ShouldReturn415UnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void testCreateEmployee_WithMissingContentType_ShouldReturn415UnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testCreateEmployee_WithMaxLengthFields_ShouldReturn201Created() throws Exception {
        // Arrange
        validEmployeeDTO.setFirstName("A".repeat(50));
        validEmployeeDTO.setLastName("B".repeat(50));
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testCreateEmployee_WithMinLengthFields_ShouldReturn201Created() throws Exception {
        // Arrange
        validEmployeeDTO.setFirstName("A");
        validEmployeeDTO.setLastName("B");
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testGetAllEmployees_WithMaxPageSize_ShouldReturn200OK() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(validEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 100), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees?size=100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ========== ROLE-BASED TESTS ==========

    @Test
    public void testCreateEmployee_WithAllRoles_ShouldReturn201Created() throws Exception {
        // Test ADMIN role
        validEmployeeDTO.setRole(EmployeeRole.ADMIN);
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());

        // Test HR role
        validEmployeeDTO.setRole(EmployeeRole.HR);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());

        // Test SUPERVISOR role
        validEmployeeDTO.setRole(EmployeeRole.SUPERVISOR);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());

        // Test WORKER role
        validEmployeeDTO.setRole(EmployeeRole.WORKER);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());
    }

    // ========== STATUS-BASED TESTS ==========

    @Test
    public void testCreateEmployee_WithAllStatuses_ShouldReturn201Created() throws Exception {
        // Test ACTIVE status
        validEmployeeDTO.setStatus(EmployeeStatus.ACTIVE);
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());

        // Test INACTIVE status
        validEmployeeDTO.setStatus(EmployeeStatus.INACTIVE);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());

        // Test ON_LEAVE status
        validEmployeeDTO.setStatus(EmployeeStatus.ON_LEAVE);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());

        // Test TERMINATED status
        validEmployeeDTO.setStatus(EmployeeStatus.TERMINATED);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());
    }
}