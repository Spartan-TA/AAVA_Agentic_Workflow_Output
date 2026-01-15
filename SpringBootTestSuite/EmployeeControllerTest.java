package com.company.wms.employee.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.http.MediaType;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_AsHR_Returns201() throws Exception {
        when(employeeService.createEmployee(any())).thenReturn(new Employee(1L, "John Doe", "john.doe@company.com", "BADGE123", "HR", "ACTIVE", "WORKER", null));
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","email":"john.doe@company.com","badgeId":"BADGE123","department":"HR","status":"ACTIVE","role":"WORKER"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testCreateEmployee_AsWorker_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testCreateEmployee_WithInvalidData_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testGetAllEmployees_WithPagination_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/employees?page=0&size=10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testGetEmployeeById_WithValidId_Returns200() throws Exception {
        when(employeeService.findById(anyLong())).thenReturn(new Employee(1L, "John Doe", "john.doe@company.com", "BADGE123", "HR", "ACTIVE", "WORKER", null));
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testGetEmployeeById_WithInvalidId_Returns404() throws Exception {
        when(employeeService.findById(anyLong())).thenThrow(new ResourceNotFoundException("Employee not found"));
        mockMvc.perform(get("/api/v1/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testUpdateEmployee_AsHR_Returns200() throws Exception {
        when(employeeService.updateEmployee(anyLong(), any())).thenReturn(new Employee(1L, "Jane Doe", "jane.doe@company.com", "BADGE123", "HR", "ACTIVE", "SUPERVISOR", null));
        mockMvc.perform(put("/api/v1/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Jane Doe","email":"jane.doe@company.com","department":"HR","status":"ACTIVE","role":"SUPERVISOR"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateEmployee_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(put("/api/v1/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEmployee_AsAdmin_Returns204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(anyLong());
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testDeleteEmployee_AsSupervisor_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isForbidden());
    }
}
