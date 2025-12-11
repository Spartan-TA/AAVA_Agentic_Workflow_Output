package com.example.warehouse_employee_mgmt_epics;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    public void testCreateEmployee_ValidRequest_Returns201() throws Exception {
        String employeeJson = "{"badgeId":"A123","email":"john.doe@example.com","firstName":"John","lastName":"Doe","department":"Logistics","role":"WORKER","status":"ACTIVE","isActive":true}";
        when(employeeService.createEmployee(any())).thenReturn(new Employee());

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson))
                .andExpect(status().isCreated());
    }

    @Test
    public void testCreateEmployee_InvalidRequest_Returns400() throws Exception {
        String employeeJson = "{"badgeId":"","email":"invalid","firstName":"","lastName":"Doe"}";
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_Returns400() throws Exception {
        String employeeJson = "{"badgeId":"A123","email":"john.doe@example.com","firstName":"John","lastName":"Doe"}";
        when(employeeService.createEmployee(any())).thenThrow(new DuplicateBadgeIdException("Badge ID already exists"));

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_Unauthorized_Returns401() throws Exception {
        String employeeJson = "{"badgeId":"A123","email":"john.doe@example.com","firstName":"John","lastName":"Doe"}";
        // Simulate unauthorized by not providing authentication
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateEmployee_Forbidden_Returns403() throws Exception {
        String employeeJson = "{"badgeId":"A123","email":"john.doe@example.com","firstName":"John","lastName":"Doe"}";
        // Simulate forbidden by providing wrong role
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson)
                .header("Role", "WORKER"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllEmployees_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllEmployees_WithPaginationAndFiltering() throws Exception {
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10")
                .param("department", "Logistics"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetEmployeeById_Valid_Returns200() throws Exception {
        when(employeeService.getEmployeeById(anyLong())).thenReturn(new Employee());
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetEmployeeById_Invalid_Returns404() throws Exception {
        when(employeeService.getEmployeeById(anyLong())).thenThrow(new EmployeeNotFoundException("Not found"));
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateEmployee_Valid_Returns200() throws Exception {
        String employeeJson = "{"badgeId":"A123","email":"john.doe@example.com","firstName":"John","lastName":"Doe"}";
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(new Employee());

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateEmployee_InvalidId_Returns404() throws Exception {
        String employeeJson = "{"badgeId":"A123","email":"john.doe@example.com","firstName":"John","lastName":"Doe"}";
        when(employeeService.updateEmployee(eq(999L), any())).thenThrow(new EmployeeNotFoundException("Not found"));

        mockMvc.perform(put("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateEmployee_InvalidData_Returns400() throws Exception {
        String employeeJson = "{"badgeId":"","email":"invalid","firstName":"","lastName":"Doe"}";
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPartialUpdateEmployee_Valid_Returns200() throws Exception {
        String patchJson = "{"department":"HR"}";
        when(employeeService.partialUpdateEmployee(eq(1L), any())).thenReturn(new Employee());

        mockMvc.perform(patch("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testPartialUpdateEmployee_InvalidId_Returns404() throws Exception {
        String patchJson = "{"department":"HR"}";
        when(employeeService.partialUpdateEmployee(eq(999L), any())).thenThrow(new EmployeeNotFoundException("Not found"));

        mockMvc.perform(patch("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteEmployee_Valid_Returns204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1")
                .header("Role", "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEmployee_InvalidId_Returns404() throws Exception {
        doThrow(new EmployeeNotFoundException("Not found")).when(employeeService).deleteEmployee(999L);

        mockMvc.perform(delete("/api/employees/999")
                .header("Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteEmployee_NotAdmin_Returns403() throws Exception {
        mockMvc.perform(delete("/api/employees/1")
                .header("Role", "WORKER"))
                .andExpect(status().isForbidden());
    }
}