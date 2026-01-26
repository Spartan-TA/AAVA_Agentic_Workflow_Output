package com.example.warehouse.employee;

import com.example.warehouse.employee.dto.EmployeeDTO;
import com.example.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class EmployeeControllerTest_Part1 {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test POST /employees with valid data")
    void testCreateEmployeeValid() throws Exception {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("John Doe");
        dto.setBadgeId("EMP001");
        dto.setRole("WORKER");
        dto.setDepartment("Receiving");
        dto.setShiftGroup("A");
        dto.setHireDate(LocalDate.now().minusYears(1));
        dto.setStatus("ACTIVE");
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(dto);
        String json = "{"name":"John Doe","badgeId":"EMP001","role":"WORKER","department":"Receiving","shiftGroup":"A","hireDate":"" + LocalDate.now().minusYears(1) + "","status":"ACTIVE"}";
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /employees returns list")
    void testGetAllEmployees() throws Exception {
        EmployeeDTO dto1 = new EmployeeDTO();
        dto1.setName("Alice Smith");
        EmployeeDTO dto2 = new EmployeeDTO();
        dto2.setName("Bob Jones");
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(dto1, dto2));
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice Smith"))
                .andExpect(jsonPath("$[1].name").value("Bob Jones"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /employees/{id} returns employee")
    void testGetEmployeeById() throws Exception {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("Alice Smith");
        when(employeeService.getEmployee(1L)).thenReturn(dto);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test GET /employees forbidden for WORKER role")
    void testGetAllEmployeesForbidden() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test POST /employees unauthorized")
    void testCreateEmployeeUnauthorized() throws Exception {
        String json = "{"name":"John Doe","badgeId":"EMP001"}";
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }
}