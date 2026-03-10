package com.example.warehouse.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void testGetAllEmployees() throws Exception {
        Employee emp1 = new Employee();
        Employee emp2 = new Employee();
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(emp1, emp2));
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }
}
