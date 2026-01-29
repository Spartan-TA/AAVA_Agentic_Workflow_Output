package com.wms.ems.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.ems.common.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {
    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee employee;
    private EmployeeDTO employeeDTO;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
        employeeDTO = EmployeeDTO.builder()
                .badgeId("B123")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();
        employee = Employee.builder()
                .id(1L)
                .badgeId("B123")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
    }

    // Test getEmployees endpoint (GET /api/employees)
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployees_ValidRole_ReturnsPage() throws Exception {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    // Test getEmployees forbidden for unauthorized role
    @Test
    @WithMockUser(roles = {"GUEST"})
    void testGetEmployees_UnauthorizedRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    // Test getEmployee by id (GET /api/employees/{id})
    @Test
    @WithMockUser(roles = {"HR"})
    void testGetEmployee_ValidId_ReturnsEmployee() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B123"));
    }

    // Test getEmployee by id not found
    @Test
    @WithMockUser(roles = {"HR"})
    void testGetEmployee_NotFound_Returns404() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenThrow(new ResourceNotFoundException("Not found"));
        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    // Test getEmployeeByBadgeId (GET /api/employees/badge/{badgeId})
    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void testGetEmployeeByBadgeId_Valid_ReturnsEmployee() throws Exception {
        when(employeeService.getEmployeeByBadgeId("B123")).thenReturn(employee);
        mockMvc.perform(get("/api/employees/badge/B123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    // Test createEmployee (POST /api/employees)
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_Valid_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(employee);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("B123"));
    }

    // Test createEmployee invalid input (missing badgeId)
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        EmployeeDTO invalidDTO = EmployeeDTO.builder()
                .badgeId("")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // Test updateEmployee (PUT /api/employees/{id})
    @Test
    @WithMockUser(roles = {"HR"})
    void testUpdateEmployee_Valid_ReturnsEmployee() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(employee);
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B123"));
    }

    // Test updateEmployee not found
    @Test
    @WithMockUser(roles = {"HR"})
    void testUpdateEmployee_NotFound_Returns404() throws Exception {
        when(employeeService.updateEmployee(eq(2L), any(EmployeeDTO.class))).thenThrow(new ResourceNotFoundException("Not found"));
        mockMvc.perform(put("/api/employees/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isNotFound());
    }

    // Test deleteEmployee (DELETE /api/employees/{id})
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_Valid_ReturnsNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    // Test deleteEmployee forbidden for non-admin
    @Test
    @WithMockUser(roles = {"HR"})
    void testDeleteEmployee_NonAdmin_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    // Test filterEmployees (GET /api/employees/filter)
    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void testFilterEmployees_Valid_ReturnsPage() throws Exception {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeService.filterEmployees(eq("Packing"), eq("Active"), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees/filter?department=Packing&status=Active&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("B123"));
    }

    // Test searchEmployees (GET /api/employees/search)
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testSearchEmployees_Valid_ReturnsPage() throws Exception {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeService.searchEmployeesByName(eq("John"), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees/search?name=John&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    // Test getEmployee unauthorized (no auth)
    @Test
    void testGetEmployee_NoAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isUnauthorized());
    }
}
