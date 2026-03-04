package com.wms.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.employee.dto.EmployeeDTO;
import com.wms.employee.service.EmployeeService;
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
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for EmployeeController.
 * Tests cover normal cases, boundary conditions, edge cases, HTTP status codes, and request/response validation.
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
        // Arrange: Create test DTO
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("Day Shift");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== Tests for POST /employees (Create Employee) ==========

    @Test
    @DisplayName("Test create employee with valid data returns 201 Created")
    public void testCreateEmployee_ValidData_Returns201() throws Exception {
        // Arrange
        EmployeeDTO requestDTO = new EmployeeDTO();
        requestDTO.setBadgeId("EMP001");
        requestDTO.setName("John Doe");
        requestDTO.setRole("WORKER");
        requestDTO.setDepartment("Warehouse");
        requestDTO.setShiftGroup("Day Shift");
        requestDTO.setHireDate(LocalDate.of(2023, 1, 15));
        requestDTO.setStatus("ACTIVE");

        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.role", is("WORKER")))
                .andExpect(jsonPath("$.department", is("Warehouse")))
                .andExpect(jsonPath("$.shiftGroup", is("Day Shift")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test create employee with null badgeId returns 400 Bad Request")
    public void testCreateEmployee_NullBadgeId_Returns400() throws Exception {
        // Arrange
        EmployeeDTO requestDTO = new EmployeeDTO();
        requestDTO.setBadgeId(null);
        requestDTO.setName("John Doe");
        requestDTO.setRole("WORKER");

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test create employee with empty badgeId returns 400 Bad Request")
    public void testCreateEmployee_EmptyBadgeId_Returns400() throws Exception {
        // Arrange
        EmployeeDTO requestDTO = new EmployeeDTO();
        requestDTO.setBadgeId("");
        requestDTO.setName("John Doe");
        requestDTO.setRole("WORKER");

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test create employee with whitespace badgeId returns 400 Bad Request")
    public void testCreateEmployee_WhitespaceBadgeId_Returns400() throws Exception {
        // Arrange
        EmployeeDTO requestDTO = new EmployeeDTO();
        requestDTO.setBadgeId("   ");
        requestDTO.setName("John Doe");
        requestDTO.setRole("WORKER");

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test create employee with invalid JSON returns 400 Bad Request")
    public void testCreateEmployee_InvalidJSON_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test create employee with empty request body returns 400 Bad Request")
    public void testCreateEmployee_EmptyBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test create employee with special characters in name returns 201 Created")
    public void testCreateEmployee_SpecialCharactersInName_Returns201() throws Exception {
        // Arrange
        EmployeeDTO requestDTO = new EmployeeDTO();
        requestDTO.setBadgeId("EMP001");
        requestDTO.setName("JosÃ© MarÃ­a O'Brien-Smith");
        requestDTO.setRole("WORKER");

        testEmployeeDTO.setName("JosÃ© MarÃ­a O'Brien-Smith");
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("JosÃ© MarÃ­a O'Brien-Smith")));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test create employee with very long name (boundary) returns 201 Created")
    public void testCreateEmployee_VeryLongName_Returns201() throws Exception {
        // Arrange
        String longName = "A".repeat(255);
        EmployeeDTO requestDTO = new EmployeeDTO();
        requestDTO.setBadgeId("EMP001");
        requestDTO.setName(longName);
        requestDTO.setRole("WORKER");

        testEmployeeDTO.setName(longName);
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(longName)));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDTO.class));
    }

    // ========== Tests for GET /employees/{id} (Get Employee) ==========

    @Test
    @DisplayName("Test get employee by valid ID returns 200 OK")
    public void testGetEmployee_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.name", is("John Doe")));

        verify(employeeService, times(1)).getEmployee(1L);
    }

    @Test
    @DisplayName("Test get employee by non-existent ID returns 404 Not Found")
    public void testGetEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployee(999L);
    }

    @Test
    @DisplayName("Test get employee by invalid ID format returns 400 Bad Request")
    public void testGetEmployee_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).getEmployee(anyLong());
    }

    @Test
    @DisplayName("Test get employee by negative ID returns 400 Bad Request")
    public void testGetEmployee_NegativeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.getEmployee(-1L)).thenThrow(new IllegalArgumentException("Invalid ID"));

        // Act & Assert
        mockMvc.perform(get("/employees/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).getEmployee(-1L);
    }

    @Test
    @DisplayName("Test get employee by zero ID returns 400 Bad Request")
    public void testGetEmployee_ZeroId_Returns400() throws Exception {
        // Arrange
        when(employeeService.getEmployee(0L)).thenThrow(new IllegalArgumentException("Invalid ID"));

        // Act & Assert
        mockMvc.perform(get("/employees/0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).getEmployee(0L);
    }

    // ========== Tests for GET /employees (List Employees) ==========

    @Test
    @DisplayName("Test list employees with pagination returns 200 OK")
    public void testListEmployees_WithPagination_Returns200() throws Exception {
        // Arrange
        EmployeeDTO employee2 = new EmployeeDTO();
        employee2.setId(2L);
        employee2.setBadgeId("EMP002");
        employee2.setName("Jane Smith");
        employee2.setRole("SUPERVISOR");

        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO, employee2);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 10), 2);

        when(employeeService.listEmployees(any(), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].badgeId", is("EMP001")))
                .andExpect(jsonPath("$.content[1].badgeId", is("EMP002")))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));

        verify(employeeService, times(1)).listEmployees(any(), any());
    }

    @Test
    @DisplayName("Test list employees with empty result returns 200 OK")
    public void testListEmployees_EmptyResult_Returns200() throws Exception {
        // Arrange
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(employeeService.listEmployees(any(), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));

        verify(employeeService, times(1)).listEmployees(any(), any());
    }

    @Test
    @DisplayName("Test list employees with default pagination returns 200 OK")
    public void testListEmployees_DefaultPagination_Returns200() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 20), 1);

        when(employeeService.listEmployees(any(), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(employeeService, times(1)).listEmployees(any(), any());
    }

    @Test
    @DisplayName("Test list employees with page size 1 returns 200 OK")
    public void testListEmployees_PageSizeOne_Returns200() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 1), 2);

        when(employeeService.listEmployees(any(), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)));

        verify(employeeService, times(1)).listEmployees(any(), any());
    }

    @Test
    @DisplayName("Test list employees with large page size returns 200 OK")
    public void testListEmployees_LargePageSize_Returns200() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        Page<EmployeeDTO> employeePage = new PageImpl<>(employees, PageRequest.of(0, 100), 1);

        when(employeeService.listEmployees(any(), any())).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(employeeService, times(1)).listEmployees(any(), any());
    }

    @Test
    @DisplayName("Test list employees with invalid page parameter returns 400 Bad Request")
    public void testListEmployees_InvalidPageParameter_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "invalid")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).listEmployees(any(), any());
    }

    @Test
    @DisplayName("Test list employees with negative page number returns 400 Bad Request")
    public void testListEmployees_NegativePageNumber_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).listEmployees(any(), any());
    }

    // ========== Tests for PUT /employees/{id} (Update Employee) ==========

    @Test
    @DisplayName("Test update employee with valid data returns 200 OK")
    public void testUpdateEmployee_ValidData_Returns200() throws Exception {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setRole("SUPERVISOR");
        updateDTO.setDepartment("Management");

        EmployeeDTO updatedEmployee = new EmployeeDTO();
        updatedEmployee.setId(1L);
        updatedEmployee.setBadgeId("EMP001");
        updatedEmployee.setName("Updated Name");
        updatedEmployee.setRole("SUPERVISOR");
        updatedEmployee.setDepartment("Management");

        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(updatedEmployee);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.role", is("SUPERVISOR")))
                .andExpect(jsonPath("$.department", is("Management")));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test update employee with non-existent ID returns 404 Not Found")
    public void testUpdateEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Updated Name");

        when(employeeService.updateEmployee(eq(999L), any(EmployeeDTO.class)))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(eq(999L), any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test update employee with invalid ID format returns 400 Bad Request")
    public void testUpdateEmployee_InvalidIdFormat_Returns400() throws Exception {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Updated Name");

        // Act & Assert
        mockMvc.perform(put("/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test update employee with empty request body returns 400 Bad Request")
    public void testUpdateEmployee_EmptyBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test update employee with partial data returns 200 OK")
    public void testUpdateEmployee_PartialData_Returns200() throws Exception {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Updated Name");

        EmployeeDTO updatedEmployee = new EmployeeDTO();
        updatedEmployee.setId(1L);
        updatedEmployee.setBadgeId("EMP001");
        updatedEmployee.setName("Updated Name");
        updatedEmployee.setRole("WORKER"); // Original value

        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(updatedEmployee);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.role", is("WORKER")));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(EmployeeDTO.class));
    }

    // ========== Tests for DELETE /employees/{id} (Delete Employee) ==========

    @Test
    @DisplayName("Test delete employee with valid ID returns 204 No Content")
    public void testDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @DisplayName("Test delete employee with non-existent ID returns 404 Not Found")
    public void testDeleteEmployee_NonExistentId_Returns404() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Employee not found")).when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployee(999L);
    }

    @Test
    @DisplayName("Test delete employee with invalid ID format returns 400 Bad Request")
    public void testDeleteEmployee_InvalidIdFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    @DisplayName("Test delete employee with negative ID returns 400 Bad Request")
    public void testDeleteEmployee_NegativeId_Returns400() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid ID")).when(employeeService).deleteEmployee(-1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).deleteEmployee(-1L);
    }

    @Test
    @DisplayName("Test delete employee with zero ID returns 400 Bad Request")
    public void testDeleteEmployee_ZeroId_Returns400() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid ID")).when(employeeService).deleteEmployee(0L);

        // Act & Assert
        mockMvc.perform(delete("/employees/0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).deleteEmployee(0L);
    }

    // ========== Tests for Content-Type validation ==========

    @Test
    @DisplayName("Test create employee with unsupported media type returns 415 Unsupported Media Type")
    public void testCreateEmployee_UnsupportedMediaType_Returns415() throws Exception {
        // Arrange
        EmployeeDTO requestDTO = new EmployeeDTO();
        requestDTO.setBadgeId("EMP001");
        requestDTO.setName("John Doe");

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnsupportedMediaType());

        verify(employeeService, never()).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("Test update employee with unsupported media type returns 415 Unsupported Media Type")
    public void testUpdateEmployee_UnsupportedMediaType_Returns415() throws Exception {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setName("Updated Name");

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnsupportedMediaType());

        verify(employeeService, never()).updateEmployee(anyLong(), any(EmployeeDTO.class));
    }
}