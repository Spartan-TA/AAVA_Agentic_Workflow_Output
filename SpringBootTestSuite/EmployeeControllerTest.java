package com.wems.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wems.common.exception.ResourceNotFoundException;
import com.wems.employee.domain.Employee;
import com.wems.employee.dto.EmployeeDTO;
import com.wems.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {
    @Mock
    private EmployeeService employeeService;
    @InjectMocks
    private EmployeeController employeeController;
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setActive(true);
        employeeDTO = new EmployeeDTO();
        employeeDTO.setName("John Doe");
        employeeDTO.setEmail("john.doe@example.com");
        employeeDTO.setBadgeId("BADGE123");
        employeeDTO.setRole("WORKER");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createEmployee_Success() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(employee);
        mockMvc.perform(post("/api/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setName("");
        invalidDTO.setEmail("not-an-email");
        invalidDTO.setBadgeId("");
        invalidDTO.setRole("");
        mockMvc.perform(post("/api/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createEmployee_DuplicateBadgeId_ReturnsBadRequest() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenThrow(new IllegalArgumentException("Badge ID must be unique."));
        mockMvc.perform(post("/api/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void getActiveEmployees_Success() throws Exception {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeService.getActiveEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void updateEmployee_Success() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(employee);
        mockMvc.perform(put("/api/employee/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void updateEmployee_NotFound_ReturnsNotFound() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found: 99"));
        mockMvc.perform(put("/api/employee/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void softDeleteEmployee_Success() throws Exception {
        doNothing().when(employeeService).softDeleteEmployee(1L);
        mockMvc.perform(delete("/api/employee/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void softDeleteEmployee_NotFound_ReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Employee not found: 99")).when(employeeService).softDeleteEmployee(99L);
        mockMvc.perform(delete("/api/employee/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void getEmployeeByBadgeId_Success() throws Exception {
        when(employeeService.getEmployeeByBadgeId("BADGE123")).thenReturn(employee);
        mockMvc.perform(get("/api/employee/badge/BADGE123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void getEmployeeByBadgeId_NotFound_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeByBadgeId("BADGE999")).thenThrow(new ResourceNotFoundException("Employee not found with badgeId: BADGE999"));
        mockMvc.perform(get("/api/employee/badge/BADGE999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthorizedAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isForbidden());
    }
}
