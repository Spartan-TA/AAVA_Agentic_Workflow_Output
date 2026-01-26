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
class EmployeeControllerTest_Part2 {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /employees/search endpoint")
    void testSearchEmployees() throws Exception {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("Alice Smith");
        when(employeeService.getEmployeesByFilters(eq("Receiving"), eq(null), eq(null))).thenReturn(Arrays.asList(dto));
        mockMvc.perform(get("/employees/search?department=Receiving"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice Smith"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /employees/{id} endpoint")
    void testUpdateEmployee() throws Exception {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("Jane Doe");
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(dto);
        String json = "{"name":"Jane Doe"}";
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test DELETE /employees/{id} endpoint")
    void testDeleteEmployee() throws Exception {
        Mockito.doNothing().when(employeeService).softDeleteEmployee(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /employees with pagination")
    void testGetEmployeesWithPagination() throws Exception {
        EmployeeDTO dto1 = new EmployeeDTO();
        dto1.setName("Alice Smith");
        EmployeeDTO dto2 = new EmployeeDTO();
        dto2.setName("Bob Jones");
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(dto1, dto2));
        mockMvc.perform(get("/employees?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice Smith"))
                .andExpect(jsonPath("$[1].name").value("Bob Jones"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /employees/{id} not found")
    void testGetEmployeeNotFound() throws Exception {
        when(employeeService.getEmployee(99L)).thenThrow(new IllegalArgumentException("Employee not found"));
        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test PUT /employees/{id} with invalid data")
    void testUpdateEmployeeInvalidData() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenThrow(new IllegalArgumentException("Invalid data"));
        String json = "{"name":null}";
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}