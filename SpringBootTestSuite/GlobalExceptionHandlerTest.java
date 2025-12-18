package com.warehouse.ems.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.warehouse.ems.employee.EmployeeService employeeService;

    @Test
    public void testValidationExceptionReturns400WithErrorDetails() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType("application/json")
                .content("{"name":""}")
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIllegalArgumentExceptionReturns404WithMessage() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenThrow(new IllegalArgumentException("Employee not found"));

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }
}