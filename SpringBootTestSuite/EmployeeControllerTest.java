package com.warehouse.ems.controller;

import com.warehouse.ems.domain.entity.Employee;
import com.warehouse.ems.domain.entity.EmployeeStatus;
import com.warehouse.ems.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void testCreateEmployee() throws Exception {
        Employee employee = new Employee();
        employee.setBadgeId("B123");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setStatus(EmployeeStatus.ACTIVE);

        Mockito.when(employeeService.createEmployee(Mockito.any(Employee.class))).thenReturn(employee);

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"B123","firstName":"John","lastName":"Doe","email":"john.doe@example.com","status":"ACTIVE"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B123"));
    }

    @Test
    void testCreateEmployeeWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"B123","firstName":"John","lastName":"Doe","email":"invalid-email","status":"ACTIVE"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetEmployeeNotFound() throws Exception {
        Mockito.when(employeeService.getEmployee(Mockito.anyLong())).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/v1/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteEmployee() throws Exception {
        Mockito.doNothing().when(employeeService).softDeleteEmployee(Mockito.anyLong());

        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isNoContent());
    }
}