package com.example.warehouse.test.controller;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.Department;
import com.example.warehouse.entity.Role;
import com.example.warehouse.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private Employee employee;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        employee = new Employee("Bob White", "B321", Role.WORKER, new Department("Packing"), "D", LocalDate.now().minusDays(30), "ACTIVE");
    }

    @Test
    void testGetEmployeeByBadgeId_Valid_ShouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeByBadgeId("B321")).thenReturn(employee);

        mockMvc.perform(get("/api/employees/B321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob White"))
                .andExpect(jsonPath("$.badgeId").value("B321"));
    }

    @Test
    void testGetEmployeeByBadgeId_NotFound_ShouldReturn404() throws Exception {
        when(employeeService.getEmployeeByBadgeId("X999")).thenThrow(new RuntimeException("Employee not found"));

        mockMvc.perform(get("/api/employees/X999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateEmployee_Valid_ShouldReturnCreated() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bob White"));
    }

    @Test
    void testCreateEmployee_InvalidInput_ShouldReturnBadRequest() throws Exception {
        Employee invalidEmployee = new Employee("", "", null, null, "", null, "");
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest());
    }
}