package com.warehouse.ems.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.domain.employee.EmployeeDto;
import com.warehouse.ems.domain.employee.EmployeeService;
import com.warehouse.ems.domain.employee.EmployeeStatus;
import com.warehouse.ems.domain.employee.Role;
import com.warehouse.ems.exception.BusinessException;
import com.warehouse.ems.exception.ResourceNotFoundException;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Test Suite")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDto testEmployeeDto;

    @BeforeEach
    public void setUp() {
        testEmployeeDto = new EmployeeDto();
        testEmployeeDto.setId(1L);
        testEmployeeDto.setBadgeId("EMP001");
        testEmployeeDto.setName("John Doe");
        testEmployeeDto.setRole(Role.WORKER);
        testEmployeeDto.setDepartment("Warehouse");
        testEmployeeDto.setShiftGroup("A");
        testEmployeeDto.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDto.setStatus(EmployeeStatus.ACTIVE);
    }

    @Test
    @DisplayName("Test create employee - success")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeSuccess() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.badgeId").value("EMP001"))
            .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).create(any(EmployeeDto.class));
    }

    @Test
    @DisplayName("Test create employee - validation error")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeValidationError() throws Exception {
        // Arrange
        testEmployeeDto.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test create employee - duplicate badge ID")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeDuplicateBadgeId() throws Exception {
        // Arrange
        when(employeeService.create(any(EmployeeDto.class)))
            .thenThrow(new BusinessException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test create employee - unauthorized")
    public void testCreateEmployeeUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test create employee - forbidden for non-admin")
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployeeForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test get employee by ID - success")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeByIdSuccess() throws Exception {
        // Arrange
        when(employeeService.findById(1L)).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    @DisplayName("Test get employee by ID - not found")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeByIdNotFound() throws Exception {
        // Arrange
        when(employeeService.findById(999L))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test list all employees - success")
    @WithMockUser(roles = "HR")
    public void testListAllEmployeesSuccess() throws Exception {
        // Arrange
        Page<EmployeeDto> page = new PageImpl<>(Arrays.asList(testEmployeeDto));
        when(employeeService.findAll(any(PageRequest.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"));
    }

    @Test
    @DisplayName("Test list employees with pagination")
    @WithMockUser(roles = "HR")
    public void testListEmployeesWithPagination() throws Exception {
        // Arrange
        Page<EmployeeDto> page = new PageImpl<>(Arrays.asList(testEmployeeDto));
        when(employeeService.findAll(any(PageRequest.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "name,asc"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test update employee - success")
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployeeSuccess() throws Exception {
        // Arrange
        testEmployeeDto.setName("Jane Doe");
        when(employeeService.update(anyLong(), any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @DisplayName("Test update employee - not found")
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployeeNotFound() throws Exception {
        // Arrange
        when(employeeService.update(anyLong(), any(EmployeeDto.class)))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test delete employee - success")
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployeeSuccess() throws Exception {
        // Arrange
        doNothing().when(employeeService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(employeeService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Test delete employee - not found")
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployeeNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
            .when(employeeService).delete(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/999")
                .with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test delete employee - forbidden for non-admin")
    @WithMockUser(roles = "HR")
    public void testDeleteEmployeeForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test create employee with null badge ID")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithNullBadgeId() throws Exception {
        // Arrange
        testEmployeeDto.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test create employee with invalid JSON")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test create employee with missing required fields")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithMissingFields() throws Exception {
        // Arrange
        EmployeeDto incompleteDto = new EmployeeDto();
        incompleteDto.setBadgeId("EMP002");

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test get employee with invalid ID format")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeWithInvalidIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/invalid"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test list employees with invalid pagination parameters")
    @WithMockUser(roles = "HR")
    public void testListEmployeesWithInvalidPagination() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "-1")
                .param("size", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test update employee with mismatched ID")
    @WithMockUser(roles = "ADMIN")
    public void testUpdateEmployeeWithMismatchedId() throws Exception {
        // Arrange
        testEmployeeDto.setId(2L);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test create employee without CSRF token")
    @WithMockUser(roles = "ADMIN")
    public void testCreateEmployeeWithoutCsrfToken() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test list employees with large page size")
    @WithMockUser(roles = "HR")
    public void testListEmployeesWithLargePageSize() throws Exception {
        // Arrange
        Page<EmployeeDto> page = new PageImpl<>(Arrays.asList(testEmployeeDto));
        when(employeeService.findAll(any(PageRequest.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "1000"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test get employee by badge ID - success")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeByBadgeIdSuccess() throws Exception {
        // Arrange
        when(employeeService.findByBadgeId("EMP001")).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/badge/EMP001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    @DisplayName("Test get employee by badge ID - not found")
    @WithMockUser(roles = "HR")
    public void testGetEmployeeByBadgeIdNotFound() throws Exception {
        // Arrange
        when(employeeService.findByBadgeId("INVALID"))
            .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/badge/INVALID"))
            .andExpect(status().isNotFound());
    }
}