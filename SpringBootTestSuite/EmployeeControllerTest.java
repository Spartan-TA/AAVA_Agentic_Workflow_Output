package com.wms.ems.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.ems.employee.dto.CreateEmployeeRequest;
import com.wms.ems.employee.dto.EmployeeDTO;
import com.wms.ems.employee.dto.UpdateEmployeeRequest;
import com.wms.ems.employee.exception.ResourceNotFoundException;
import com.wms.ems.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
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

    private EmployeeDTO employeeDTO;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employeeId);
        employeeDTO.setName("John Doe");
        employeeDTO.setBadgeId("BADGE12345");
        employeeDTO.setRole("WORKER");
        employeeDTO.setDepartment("Logistics");
        employeeDTO.setShiftGroup("A");
        employeeDTO.setHireDate(LocalDate.of(2022, 1, 1));
        employeeDTO.setStatus("ACTIVE");
        employeeDTO.setCreatedAt(LocalDateTime.now());
        employeeDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_WithValidRequest_Returns201Created() throws Exception {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setName("Jane Smith");
        req.setBadgeId("BADGE54321");
        req.setRole("HR");
        req.setDepartment("HR");
        req.setShiftGroup("B");
        req.setHireDate(LocalDate.of(2023, 5, 10));
        req.setStatus("ACTIVE");
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(employeeDTO);
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_WithInvalidBadgeId_Returns400BadRequest() throws Exception {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setName("Jane Smith");
        req.setBadgeId("badg"); // invalid
        req.setRole("HR");
        req.setDepartment("HR");
        req.setHireDate(LocalDate.now());
        req.setStatus("ACTIVE");
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_WithMissingRequiredFields_Returns400BadRequest() throws Exception {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setBadgeId("BADGE54321");
        req.setRole("HR");
        req.setDepartment("HR");
        req.setHireDate(LocalDate.now());
        req.setStatus("ACTIVE");
        // Missing name
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void testCreateEmployee_WithoutAuthentication_Returns401Unauthorized() throws Exception {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setName("Jane Smith");
        req.setBadgeId("BADGE54321");
        req.setRole("HR");
        req.setDepartment("HR");
        req.setHireDate(LocalDate.now());
        req.setStatus("ACTIVE");
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testCreateEmployee_WithWorkerRole_Returns403Forbidden() throws Exception {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setName("Jane Smith");
        req.setBadgeId("BADGE54321");
        req.setRole("HR");
        req.setDepartment("HR");
        req.setHireDate(LocalDate.now());
        req.setStatus("ACTIVE");
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testGetAllEmployees_WithAuthentication_Returns200OK() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.singletonList(employeeDTO), PageRequest.of(0, 10), 1);
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("John Doe")));
    }

    @Test
    @WithMockUser
    void testGetAllEmployees_WithPagination_ReturnsCorrectPage() throws Exception {
        List<EmployeeDTO> dtos = Arrays.asList(employeeDTO);
        Page<EmployeeDTO> page = new PageImpl<>(dtos, PageRequest.of(1, 1), 2);
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/v1/employees?page=1&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @WithMockUser
    void testGetEmployeeById_WithValidId_Returns200OK() throws Exception {
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employeeDTO);
        mockMvc.perform(get("/api/v1/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employeeId.toString())));
    }

    @Test
    @WithMockUser
    void testGetEmployeeById_WithNonExistentId_Returns404NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(employeeService.getEmployeeById(randomId)).thenThrow(new ResourceNotFoundException("Employee not found"));
        mockMvc.perform(get("/api/v1/employees/" + randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testUpdateEmployee_WithValidData_Returns200OK() throws Exception {
        UpdateEmployeeRequest req = new UpdateEmployeeRequest();
        req.setName("Updated Name");
        when(employeeService.updateEmployee(eq(employeeId), any(UpdateEmployeeRequest.class))).thenReturn(employeeDTO);
        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testUpdateEmployee_WithoutHRRole_Returns403Forbidden() throws Exception {
        UpdateEmployeeRequest req = new UpdateEmployeeRequest();
        req.setName("Updated Name");
        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_WithAdminRole_Returns204NoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(employeeId);
        mockMvc.perform(delete("/api/v1/employees/" + employeeId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testDeleteEmployee_WithoutAdminRole_Returns403Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/" + employeeId))
                .andExpect(status().isForbidden());
    }
}
