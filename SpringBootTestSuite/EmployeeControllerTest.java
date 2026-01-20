package com.company.warehouse.employee;

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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for EmployeeController
 * Tests all REST endpoints with various scenarios
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Integration Tests")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDto validEmployeeDto;
    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        validEmployeeDto = new EmployeeDto();
        validEmployeeDto.setName("John Doe");
        validEmployeeDto.setBadgeId("EMP001");
        validEmployeeDto.setRole(Role.WORKER);
        validEmployeeDto.setDepartment("Shipping");
        validEmployeeDto.setShiftGroup("Morning");
        validEmployeeDto.setHireDate(LocalDate.of(2024, 1, 15));
        validEmployeeDto.setStatus(Status.ACTIVE);

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setRole(Role.WORKER);
        validEmployee.setDepartment("Shipping");
        validEmployee.setShiftGroup("Morning");
        validEmployee.setHireDate(LocalDate.of(2024, 1, 15));
        validEmployee.setStatus(Status.ACTIVE);
        validEmployee.setCreatedAt(LocalDateTime.now());
        validEmployee.setUpdatedAt(LocalDateTime.now());
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test POST /employees with valid data returns 201 Created")
    public void testCreateEmployeeWithValidData() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDto.class))).thenReturn(validEmployee);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.role").value("WORKER"))
                .andExpect(jsonPath("$.department").value("Shipping"));

        verify(employeeService, times(1)).create(any(EmployeeDto.class));
    }

    @Test
    @DisplayName("Test POST /employees with duplicate badge ID returns 400 Bad Request")
    public void testCreateEmployeeWithDuplicateBadgeId() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDto.class)))
            .thenThrow(new IllegalArgumentException("Badge ID must be unique"));

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Badge ID must be unique"));

        verify(employeeService, times(1)).create(any(EmployeeDto.class));
    }

    @Test
    @DisplayName("Test POST /employees with missing required fields returns 400 Bad Request")
    public void testCreateEmployeeWithMissingFields() throws Exception {
        // Arrange
        EmployeeDto invalidDto = new EmployeeDto();
        invalidDto.setName(""); // Empty name

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST /employees with invalid badge ID format returns 400 Bad Request")
    public void testCreateEmployeeWithInvalidBadgeIdFormat() throws Exception {
        // Arrange
        validEmployeeDto.setBadgeId("abc"); // Too short

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test POST /employees with null request body returns 400 Bad Request")
    public void testCreateEmployeeWithNullBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    @DisplayName("Test GET /employees returns paginated list")
    public void testGetAllEmployees() throws Exception {
        // Arrange
        Page<Employee> page = new PageImpl<>(Arrays.asList(validEmployee), PageRequest.of(0, 10), 1);
        when(employeeService.findAll(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(employeeService, times(1)).findAll(any());
    }

    @Test
    @DisplayName("Test GET /employees with empty result returns empty page")
    public void testGetAllEmployeesEmptyResult() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(employeeService.findAll(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(employeeService, times(1)).findAll(any());
    }

    @Test
    @DisplayName("Test GET /employees with custom page size")
    public void testGetAllEmployeesWithCustomPageSize() throws Exception {
        // Arrange
        Page<Employee> page = new PageImpl<>(Arrays.asList(validEmployee), PageRequest.of(0, 5), 1);
        when(employeeService.findAll(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5));

        verify(employeeService, times(1)).findAll(any());
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    @DisplayName("Test GET /employees/{id} with valid ID returns employee")
    public void testGetEmployeeByIdWithValidId() throws Exception {
        // Arrange
        when(employeeService.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));

        verify(employeeService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test GET /employees/{id} with non-existent ID returns 404 Not Found")
    public void testGetEmployeeByIdWithNonExistentId() throws Exception {
        // Arrange
        when(employeeService.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Test GET /employees/{id} with negative ID returns 404 Not Found")
    public void testGetEmployeeByIdWithNegativeId() throws Exception {
        // Arrange
        when(employeeService.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/-1"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).findById(-1L);
    }

    @Test
    @DisplayName("Test GET /employees/{id} with zero ID returns 404 Not Found")
    public void testGetEmployeeByIdWithZeroId() throws Exception {
        // Arrange
        when(employeeService.findById(0L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/employees/0"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).findById(0L);
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test PUT /employees/{id} with valid data returns updated employee")
    public void testUpdateEmployeeWithValidData() throws Exception {
        // Arrange
        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(1L);
        updatedEmployee.setName("Jane Doe");
        updatedEmployee.setBadgeId("EMP001");
        updatedEmployee.setRole(Role.SUPERVISOR);
        updatedEmployee.setDepartment("Receiving");
        updatedEmployee.setShiftGroup("Evening");
        updatedEmployee.setHireDate(LocalDate.of(2024, 1, 15));
        updatedEmployee.setStatus(Status.ACTIVE);

        when(employeeService.update(eq(1L), any(EmployeeDto.class))).thenReturn(updatedEmployee);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(employeeService, times(1)).update(eq(1L), any(EmployeeDto.class));
    }

    @Test
    @DisplayName("Test PUT /employees/{id} with non-existent ID returns 400 Bad Request")
    public void testUpdateEmployeeWithNonExistentId() throws Exception {
        // Arrange
        when(employeeService.update(eq(999L), any(EmployeeDto.class)))
            .thenThrow(new IllegalArgumentException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee not found"));

        verify(employeeService, times(1)).update(eq(999L), any(EmployeeDto.class));
    }

    @Test
    @DisplayName("Test PATCH /employees/{id} with partial data returns updated employee")
    public void testPatchEmployeeWithPartialData() throws Exception {
        // Arrange
        when(employeeService.update(eq(1L), any(EmployeeDto.class))).thenReturn(validEmployee);

        // Act & Assert
        mockMvc.perform(patch("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(employeeService, times(1)).update(eq(1L), any(EmployeeDto.class));
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Test DELETE /employees/{id} with valid ID returns 204 No Content")
    public void testDeleteEmployeeWithValidId() throws Exception {
        // Arrange
        doNothing().when(employeeService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Test DELETE /employees/{id} with non-existent ID returns 400 Bad Request")
    public void testDeleteEmployeeWithNonExistentId() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Employee not found"))
            .when(employeeService).delete(999L);

        // Act & Assert
        mockMvc.perform(delete("/employees/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee not found"));

        verify(employeeService, times(1)).delete(999L);
    }

    // ========== GET EMPLOYEES BY DEPARTMENT TESTS ==========

    @Test
    @DisplayName("Test GET /employees/department/{department} returns filtered employees")
    public void testGetEmployeesByDepartment() throws Exception {
        // Arrange
        Page<Employee> page = new PageImpl<>(Arrays.asList(validEmployee), PageRequest.of(0, 10), 1);
        when(employeeService.findByDepartment(eq("Shipping"), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees/department/Shipping")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].department").value("Shipping"));

        verify(employeeService, times(1)).findByDepartment(eq("Shipping"), any());
    }

    @Test
    @DisplayName("Test GET /employees/department/{department} with non-existent department returns empty page")
    public void testGetEmployeesByNonExistentDepartment() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(employeeService.findByDepartment(eq("NonExistent"), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees/department/NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(employeeService, times(1)).findByDepartment(eq("NonExistent"), any());
    }

    @Test
    @DisplayName("Test GET /employees/department/{department} with special characters in department name")
    public void testGetEmployeesByDepartmentWithSpecialCharacters() throws Exception {
        // Arrange
        Page<Employee> page = new PageImpl<>(Arrays.asList(validEmployee), PageRequest.of(0, 10), 1);
        when(employeeService.findByDepartment(eq("Shipping & Receiving"), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees/department/Shipping & Receiving"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).findByDepartment(eq("Shipping & Receiving"), any());
    }
}