package com.example.warehouse.controller;

import com.example.warehouse.dto.EmployeeDTO;
import com.example.warehouse.entity.EmployeeStatus;
import com.example.warehouse.entity.Role;
import com.example.warehouse.exception.DuplicateResourceException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = new EmployeeDTO();
        validDto.setName("John Doe");
        validDto.setBadgeId("B123");
        validDto.setEmail("john.doe@example.com");
        validDto.setRole(Role.WORKER);
        validDto.setDepartment("Logistics");
        validDto.setShiftGroup("A");
        validDto.setHireDate(LocalDate.now());
        validDto.setStatus(EmployeeStatus.ACTIVE);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_ValidRequest_Returns201Created() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validDto);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_InvalidRequest_Returns400BadRequest() throws Exception {
        EmployeeDTO invalidDto = new EmployeeDTO();
        invalidDto.setName(""); // Invalid name

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployee_Unauthorized_Returns401Unauthorized() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testCreateEmployee_ForbiddenRole_Returns403Forbidden() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_DuplicateBadgeId_Returns409Conflict() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class)))
                .thenThrow(new DuplicateResourceException("BadgeId already exists"));

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployee_ValidId_Returns200OK() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(validDto);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployee_InvalidId_Returns404NotFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenThrow(new ResourceNotFoundException("Employee not found"));

        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetEmployee_Unauthorized_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllEmployees_WithPagination_Returns200OK() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.singletonList(validDto));
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllEmployees_WithSorting_Returns200OK() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.singletonList(validDto));
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/employees?page=0&size=10&sort=name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllEmployees_EmptyResults_Returns200WithEmptyPage() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.emptyList());
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEmployee_ValidRequest_Returns200OK() throws Exception {
        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setName("Jane Doe");
        updateDto.setEmail("jane.doe@example.com");
        updateDto.setDepartment("Packing");

        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(updateDto);

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEmployee_InvalidId_Returns404NotFound() throws Exception {
        when(employeeService.updateEmployee(eq(2L), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        mockMvc.perform(put("/api/employees/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEmployee_InvalidData_Returns400BadRequest() throws Exception {
        EmployeeDTO invalidDto = new EmployeeDTO();
        invalidDto.setName(""); // Invalid

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateEmployee_Unauthorized_Returns401Unauthorized() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_ValidId_Returns204NoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_InvalidId_Returns404NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Employee not found")).when(employeeService).deleteEmployee(2L);

        mockMvc.perform(delete("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testDeleteEmployee_ForbiddenRole_Returns403Forbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}