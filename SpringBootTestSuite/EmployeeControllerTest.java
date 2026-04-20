package com.warehouse.management.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.management.employee.dto.EmployeeRequestDTO;
import com.warehouse.management.employee.dto.EmployeeResponseDTO;
import com.warehouse.management.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
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

    private EmployeeResponseDTO getSampleResponseDTO() {
        return EmployeeResponseDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    private EmployeeRequestDTO getSampleRequestDTO() {
        return EmployeeRequestDTO.builder()
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_WithValidData_ReturnsCreated() throws Exception {
        EmployeeRequestDTO request = getSampleRequestDTO();
        EmployeeResponseDTO response = getSampleResponseDTO();

        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_WithInvalidData_ReturnsBadRequest() throws Exception {
        EmployeeRequestDTO invalidRequest = EmployeeRequestDTO.builder()
                .name("")
                .badgeId("bad id!") // invalid pattern
                .role("")
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testCreateEmployee_WithoutAdminOrHRRole_ReturnsForbidden() throws Exception {
        EmployeeRequestDTO request = getSampleRequestDTO();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testGetEmployeeById_WithValidId_ReturnsOk() throws Exception {
        EmployeeResponseDTO response = getSampleResponseDTO();

        when(employeeService.getEmployeeById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser
    void testGetEmployeeById_WithNonExistentId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetAllEmployees_WithPagination_ReturnsOk() throws Exception {
        EmployeeResponseDTO response = getSampleResponseDTO();
        Page<EmployeeResponseDTO> page = new PageImpl<>(Collections.singletonList(response), PageRequest.of(0, 10), 1);

        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEmployee_WithValidData_ReturnsOk() throws Exception {
        EmployeeRequestDTO request = getSampleRequestDTO();
        EmployeeResponseDTO response = getSampleResponseDTO();

        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testPatchEmployee_WithPartialUpdate_ReturnsOk() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Patched Name");
        EmployeeResponseDTO response = getSampleResponseDTO();
        response.setName("Patched Name");

        when(employeeService.patchEmployee(eq(1L), anyMap())).thenReturn(response);

        mockMvc.perform(patch("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Patched Name"));
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testDeleteEmployee_WithoutAdminRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_WithAdminRole_ReturnsNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }
}