package com.example.warehouseems.controller;

import com.example.warehouseems.dto.EmployeeRequest;
import com.example.warehouseems.model.Employee;
import com.example.warehouseems.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@ExtendWith(SpringExtension.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmployeeService employeeService;
    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;
    private EmployeeRequest employeeRequest;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .badgeId("BADGE123")
                .department("Logistics")
                .deleted(false)
                .build();
        employeeRequest = EmployeeRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .badgeId("BADGE123")
                .department("Logistics")
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clean up if necessary
    }

    @Test
    @DisplayName("POST /employees - create employee - Success")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_Normal_Success() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(employee);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    @DisplayName("POST /employees - create employee - Duplicate badgeId")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DuplicateBadgeId_Conflict() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate badgeId"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /employees - create employee - Invalid email")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidEmail_BadRequest() throws Exception {
        employeeRequest.setEmail("invalid-email");
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenThrow(new IllegalArgumentException("Invalid email"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /employees - list with pagination/filtering - Success")
    @WithMockUser(roles = "USER")
    void testGetAllEmployees_Normal_Success() throws Exception {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeService.getAllEmployees(any(Pageable.class), anyMap())).thenReturn(page);
        mockMvc.perform(get("/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("John Doe")));
    }

    @Test
    @DisplayName("GET /employees/{id} - get by ID - Success")
    @WithMockUser(roles = "USER")
    void testGetEmployeeById_ExistingId_Success() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /employees/{id} - get by ID - Not Found")
    @WithMockUser(roles = "USER")
    void testGetEmployeeById_NonExistingId_NotFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenThrow(new com.example.warehouseems.exception.EntityNotFoundException("Not found"));
        mockMvc.perform(get("/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /employees/{id} - update employee - Success")
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_ExistingId_Success() throws Exception {
        Employee updated = Employee.builder().id(1L).name("Jane Doe").email("jane.doe@example.com").badgeId("BADGE124").department("Packing").deleted(false).build();
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(updated);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Doe")));
    }

    @Test
    @DisplayName("PUT /employees/{id} - update employee - Not Found")
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_NonExistingId_NotFound() throws Exception {
        when(employeeService.updateEmployee(eq(2L), any(EmployeeRequest.class))).thenThrow(new com.example.warehouseems.exception.EntityNotFoundException("Not found"));
        mockMvc.perform(put("/employees/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /employees/{id} - soft delete - Success")
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_ExistingId_Success() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /employees/{id} - soft delete - Not Found")
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_NonExistingId_NotFound() throws Exception {
        doThrow(new com.example.warehouseems.exception.EntityNotFoundException("Not found")).when(employeeService).deleteEmployee(2L);
        mockMvc.perform(delete("/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /employees - Unauthorized access")
    void testCreateEmployee_Unauthorized_Unauthorized() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /employees/{id} - Forbidden for USER role")
    @WithMockUser(roles = "USER")
    void testDeleteEmployee_Forbidden_Forbidden() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /employees - XSS attempt - BadRequest")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_XSSAttempt_BadRequest() throws Exception {
        employeeRequest.setName("<script>alert('xss')</script>");
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenThrow(new IllegalArgumentException("XSS attempt"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /employees - SQL injection attempt - BadRequest")
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_SQLInjectionAttempt_BadRequest() throws Exception {
        employeeRequest.setName("Robert'); DROP TABLE Employees;--");
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenThrow(new IllegalArgumentException("SQL injection attempt"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest());
    }
}
