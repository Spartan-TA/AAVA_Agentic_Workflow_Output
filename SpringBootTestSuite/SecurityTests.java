package com.company.warehouse.security;

import com.company.warehouse.employee.controller.EmployeeController;
import com.company.warehouse.employee.dto.EmployeeDTO;
import com.company.warehouse.employee.entity.Role;
import com.company.warehouse.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class SecurityTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmployeeService employeeService;
    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setBadgeId("EMP001");
        employeeDTO.setName("John Doe");
        employeeDTO.setRole(Role.WORKER);
        employeeDTO.setDepartment("Logistics");
        employeeDTO.setHireDate(LocalDate.now());
        employeeDTO.setStatus("ACTIVE");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createEmployee_AdminRole_AccessGranted() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR"})
    void createEmployee_HRRole_AccessGranted() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    void createEmployee_WorkerRole_AccessDenied() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    void updateEmployee_SupervisorRole_AccessGranted() throws Exception {
        when(employeeService.updateEmployee(any(Long.class), any(EmployeeDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    void updateEmployee_WorkerRole_AccessDenied() throws Exception {
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getEmployee_AdminRole_AccessGranted() throws Exception {
        when(employeeService.getEmployee(1L)).thenReturn(employeeDTO);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    void getEmployee_WorkerRole_AccessGranted() throws Exception {
        when(employeeService.getEmployee(1L)).thenReturn(employeeDTO);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR"})
    void listEmployees_HRRole_AccessGranted() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    void listEmployees_WorkerRole_AccessDenied() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void softDeleteEmployee_AdminRole_AccessGranted() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    void softDeleteEmployee_WorkerRole_AccessDenied() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isForbidden());
    }
}
