package com.company.warehouse.employee.controller;

import com.company.warehouse.employee.dto.*;
import com.company.warehouse.employee.service.EmployeeService;
import com.company.warehouse.employee.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Arrays;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Tests")
public class EmployeeControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private EmployeeService employeeService;
    private EmployeeDTO employeeDTO;
    private EmployeeCreateDTO createDTO;

    @BeforeEach
    public void setUp() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(1L);
        employeeDTO.setName("John Doe");
        employeeDTO.setBadgeId("EMP001");
        employeeDTO.setRole(Role.WORKER);
        employeeDTO.setDepartment("Shipping");
        createDTO = new EmployeeCreateDTO();
        createDTO.setName("John Doe");
        createDTO.setBadgeId("EMP001");
        createDTO.setRole(Role.WORKER);
        createDTO.setDepartment("Shipping");
        createDTO.setHireDate(LocalDate.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /employees with valid data returns 201")
    public void testCreateEmployee_ValidData_Returns201() throws Exception {
        when(employeeService.createEmployee(any(EmployeeCreateDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(post("/api/v1/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test POST /employees with WORKER role returns 403")
    public void testCreateEmployee_WorkerRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test POST /employees without authentication returns 401")
    public void testCreateEmployee_NoAuth_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createDTO)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test GET /employees returns paginated list")
    public void testListEmployees_ReturnsPaginatedList() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Arrays.asList(employeeDTO));
        when(employeeService.listEmployees(any(EmployeeFilterDTO.class), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/v1/employees")
            .param("page", "0")
            .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /employees/{id} returns employee")
    public void testGetEmployee_ValidId_ReturnsEmployee() throws Exception {
        when(employeeService.getEmployee(1L)).thenReturn(employeeDTO);
        mockMvc.perform(get("/api/v1/employees/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /employees/{id} updates employee")
    public void testUpdateEmployee_ValidData_ReturnsUpdated() throws Exception {
        EmployeeUpdateDTO updateDTO = new EmployeeUpdateDTO();
        updateDTO.setName("John Updated");
        when(employeeService.updateEmployee(eq(1L), any(EmployeeUpdateDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(put("/api/v1/employees/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test DELETE /employees/{id} returns 204")
    public void testDeleteEmployee_ValidId_Returns204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);
        mockMvc.perform(delete("/api/v1/employees/1"))
            .andExpect(status().isNoContent());
    }