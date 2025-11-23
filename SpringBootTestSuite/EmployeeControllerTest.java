package com.warehouse.ems.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.service.EmployeeService;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EmployeeController
 * Tests REST API endpoints with security and validation
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDto testEmployeeDto;

    @BeforeEach
    void setUp() {
        testEmployeeDto = new EmployeeDto();
        testEmployeeDto.setId(1L);
        testEmployeeDto.setBadgeId("EMP001");
        testEmployeeDto.setName("John Doe");
        testEmployeeDto.setRole("WORKER");
        testEmployeeDto.setDepartment("Warehouse");
        testEmployeeDto.setShiftGroup("A");
        testEmployeeDto.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDto.setStatus("ACTIVE");
        testEmployeeDto.setWarehouseId(1L);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_ValidInput_ReturnsCreated() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.role", is("WORKER")));

        verify(employeeService, times(1)).createEmployee(any(EmployeeDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDto.setBadgeId(null);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DuplicateBadgeId_ReturnsConflict() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeDto.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_InsufficientPermissions_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateEmployee_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_EmptyName_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDto.setName("");

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_FutureHireDate_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDto.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isBadRequest());
    }

    // ========== GET ALL EMPLOYEES TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_ValidRequest_ReturnsPage() throws Exception {
        // Arrange
        List<EmployeeDto> employees = Arrays.asList(testEmployeeDto);
        Page<EmployeeDto> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].badgeId", is("EMP001")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_EmptyResult_ReturnsEmptyPage() throws Exception {
        // Arrange
        Page<EmployeeDto> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_CustomPageSize_ReturnsCorrectPage() throws Exception {
        // Arrange
        List<EmployeeDto> employees = Arrays.asList(testEmployeeDto, testEmployeeDto, testEmployeeDto);
        Page<EmployeeDto> page = new PageImpl<>(employees, PageRequest.of(0, 20), 3);
        when(employeeService.getAllEmployees(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    void testGetAllEmployees_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_ValidId_ReturnsEmployee() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployeeById_NegativeId_ReturnsBadRequest() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(-1L))
                .thenThrow(new IllegalArgumentException("Invalid employee ID"));

        // Act & Assert
        mockMvc.perform(get("/employees/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetEmployeeById_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isUnauthorized());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_ValidInput_ReturnsUpdated() throws Exception {
        // Arrange
        testEmployeeDto.setName("Jane Doe");
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Doe")));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(eq(999L), any(EmployeeDto.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        // Arrange
        testEmployeeDto.setName(null);

        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testUpdateEmployee_InsufficientPermissions_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateEmployee_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isUnauthorized());
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_ValidId_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(employeeService).softDeleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).softDeleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Employee not found"))
                .when(employeeService).softDeleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testDeleteEmployee_InsufficientPermissions_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteEmployee_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_AlreadyDeleted_ReturnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Employee already deleted"))
                .when(employeeService).softDeleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/employees/1")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MaxLengthName_ReturnsCreated() throws Exception {
        // Arrange
        testEmployeeDto.setName("A".repeat(255));
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_SpecialCharactersInName_ReturnsCreated() throws Exception {
        // Arrange
        testEmployeeDto.setName("O'Brien-Smith Jr.");
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllEmployees_LargePageNumber_ReturnsEmptyPage() throws Exception {
        // Arrange
        Page<EmployeeDto> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(1000, 10), 0);
        when(employeeService.getAllEmployees(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                .param("page", "1000")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MissingOptionalFields_ReturnsCreated() throws Exception {
        // Arrange
        testEmployeeDto.setShiftGroup(null);
        testEmployeeDto.setWarehouseId(null);
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(testEmployeeDto);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDto)))
                .andExpect(status().isCreated());
    }
}