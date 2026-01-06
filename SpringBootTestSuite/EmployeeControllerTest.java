package com.warehouse.ems.controller;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmployeeService employeeService;
    @Autowired
    private ObjectMapper objectMapper;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("B12345");
        employee.setName("John Doe");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_ValidData_Returns201() throws Exception {
        when(employeeService.create(any(Employee.class))).thenReturn(employee);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("B12345"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_NullBody_Returns400() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DuplicateBadgeId_Returns409() throws Exception {
        when(employeeService.create(any(Employee.class))).thenThrow(new DataIntegrityViolationException("Duplicate badgeId"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployees_PaginatedList_Returns200() throws Exception {
        when(employeeService.list(anyInt(), anyInt(), anyString(), anyString())).thenReturn(Arrays.asList(employee));
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].badgeId").value("B12345"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testGetEmployeeById_ValidId_Returns200() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(employee));
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B12345"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testGetEmployeeById_NonExistentId_Returns404() throws Exception {
        when(employeeService.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_ValidData_Returns200() throws Exception {
        employee.setName("Jane Doe");
        when(employeeService.update(eq(1L), any(Employee.class))).thenReturn(employee);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testPatchEmployee_PartialUpdate_Returns200() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "INACTIVE");
        when(employeeService.partialUpdate(eq(1L), anyMap())).thenReturn(employee);
        mockMvc.perform(patch("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_ValidId_Returns204() throws Exception {
        doNothing().when(employeeService).delete(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployees_WithFilters_Returns200() throws Exception {
        when(employeeService.list(anyInt(), anyInt(), eq("ACTIVE"), eq("Logistics"))).thenReturn(Arrays.asList(employee));
        mockMvc.perform(get("/employees")
                .param("status", "ACTIVE")
                .param("department", "Logistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetEmployees_WithPaginationAndSort_Returns200() throws Exception {
        when(employeeService.list(eq(0), eq(20), isNull(), isNull())).thenReturn(Arrays.asList(employee));
        mockMvc.perform(get("/employees")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "name,asc"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateEmployee_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_Forbidden_Returns403() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidJSON_Returns400() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_MissingRequiredFields_Returns400() throws Exception {
        employee.setBadgeId(null);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest());
    }
}