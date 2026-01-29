package com.warehouse.ems.employee;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.*;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_ValidInput_ReturnsCreated() throws Exception {
        EmployeeCreateDto dto = new EmployeeCreateDto("B300", "Carol", "carol@wh.com", "Logistics", "WORKER");
        when(employeeService.createEmployee(any())).thenReturn(new EmployeeEntity(3L