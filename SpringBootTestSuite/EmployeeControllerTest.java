package com.company.warehouse.employee.controller;

import com.company.warehouse.common.exception.BadRequestException;
import com.company.warehouse.common.exception.ResourceNotFoundException;
import com.company.warehouse.employee.dto.EmployeeDto;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Employee employee;
    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
        objectMapper = new ObjectMapper();
        employee = Employee.builder()
                .id(1L)
                .badgeId("B123456")
                .name("John Doe")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        employeeDto = EmployeeDto.builder()
                .id(1L)
                .badgeId("B123456")
                .name("John Doe")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("GET /api/employees returns paged EmployeeDto list")
    void getAllEmployees_returnsPagedEmployeeDtoList() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        given(employeeService.getAllEmployees(any(Pageable.class))).willReturn(page);
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].badgeId", is("B123456")));
    }

    @Test
    @DisplayName("GET /api/employees/{id} returns EmployeeDto when found")
    void getEmployeeById_found_returnsEmployeeDto() throws Exception {
        given(employeeService.getEmployeeById(1L)).willReturn(employee);
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("B123456")));
    }

    @Test
    @DisplayName("GET /api/employees/{id} returns 404 when not found")
    void getEmployeeById_notFound_returns404() throws Exception {
        given(employeeService.getEmployeeById(2L)).willThrow(new ResourceNotFoundException("Employee not found"));
        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/employees creates and returns EmployeeDto when valid")
    void createEmployee_valid_returnsCreatedEmployeeDto() throws Exception {
        given(employeeService.createEmployee(any(EmployeeDto.class))).willReturn(employee);
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId", is("B123456")));
    }

    @Test
    @DisplayName("POST /api/employees returns 400 when validation fails")
    void createEmployee_invalid_returns400() throws Exception {
        EmployeeDto invalidDto = employeeDto.toBuilder().badgeId("").build();
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees returns 400 when badgeId exists")
    void createEmployee_badgeIdExists_returns400() throws Exception {
        given(employeeService.createEmployee(any(EmployeeDto.class))).willThrow(new BadRequestException("Badge ID already exists"));
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/employees/{id} updates and returns EmployeeDto when valid")
    void updateEmployee_valid_returnsUpdatedEmployeeDto() throws Exception {
        Employee updated = employee.toBuilder().name("Jane Updated").build();
        given(employeeService.updateEmployee(eq(1L), any(EmployeeDto.class))).willReturn(updated);
        EmployeeDto updateDto = employeeDto.toBuilder().name("Jane Updated").build();
        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Updated")));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} returns 400 when validation fails")
    void updateEmployee_invalid_returns400() throws Exception {
        EmployeeDto invalidDto = employeeDto.toBuilder().name("").build();
        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/employees/{id} returns 404 when not found")
    void updateEmployee_notFound_returns404() throws Exception {
        given(employeeService.updateEmployee(eq(2L), any(EmployeeDto.class))).willThrow(new ResourceNotFoundException("Employee not found"));
        mockMvc.perform(put("/api/employees/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} returns 204 when deleted")
    void deleteEmployee_valid_returns204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} returns 404 when not found")
    void deleteEmployee_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Employee not found")).when(employeeService).deleteEmployee(2L);
        mockMvc.perform(delete("/api/employees/2"))
                .andExpect(status().isNotFound());
    }
}
