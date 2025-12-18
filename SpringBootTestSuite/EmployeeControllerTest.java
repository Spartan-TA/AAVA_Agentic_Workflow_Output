package com.warehouse.ems.employee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2022, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    @Test
    public void testPostEmployeesReturns201WithValidData() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","badgeId":"BADGE123","role":"WORKER"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void testPostEmployeesReturns400WithInvalidData() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"","badgeId":"","role":""}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetEmployeesReturnsAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    public void testGetEmployeesWithFilters() throws Exception {
        when(employeeService.filterEmployees(eq("John"), eq(null), eq(null))).thenReturn(List.of(employee));

        mockMvc.perform(get("/employees?name=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    public void testGetEmployeeByIdReturns200WithValidId() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testGetEmployeeByIdReturns404WithInvalidId() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPutEmployeesReturns200WithValidData() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(employee);

        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","badgeId":"BADGE123","role":"WORKER"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testPutEmployeesReturns404WithInvalidId() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(Employee.class))).thenThrow(new IllegalArgumentException("Not found"));

        mockMvc.perform(put("/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","badgeId":"BADGE123","role":"WORKER"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPatchEmployeeStatusReturns200() throws Exception {
        when(employeeService.patchEmployeeStatus(eq(1L), eq("INACTIVE"))).thenReturn(employee);

        mockMvc.perform(patch("/employees/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"status":"INACTIVE"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testPatchEmployeeStatusReturns404WithInvalidId() throws Exception {
        when(employeeService.patchEmployeeStatus(eq(99L), eq("INACTIVE"))).thenThrow(new IllegalArgumentException("Not found"));

        mockMvc.perform(patch("/employees/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"status":"INACTIVE"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteEmployeeReturns204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEmployeeReturns404WithInvalidId() throws Exception {
        doThrow(new IllegalArgumentException("Not found")).when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/employees/99"))
                .andExpect(status().isNotFound());
    }
}