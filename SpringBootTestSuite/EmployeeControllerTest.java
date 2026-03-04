package com.warehouse.ems.controller;

import com.warehouse.ems.dto.EmployeeDTO;
import com.warehouse.ems.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmployeeService employeeService;
    private EmployeeDTO employeeDTO;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        employeeDTO = EmployeeDTO.builder()
                .id(1L)
                .badgeId("BADGE1")
                .name("Alice")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_ValidInput_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId", is("BADGE1")));
        verify(employeeService).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        EmployeeDTO invalid = EmployeeDTO.builder().badgeId("").name("").role("").status("").build();
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEmployee_ValidInput_ReturnsOk() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("BADGE1")));
        verify(employeeService).updateEmployee(eq(1L), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEmployee_NonExistent_ReturnsNotFound() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(EmployeeDTO.class))).thenThrow(new jakarta.persistence.EntityNotFoundException("Employee not found"));
        mockMvc.perform(put("/api/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"})
    void testGetEmployee_ValidId_ReturnsOk() throws Exception {
        when(employeeService.getEmployee(1L)).thenReturn(Optional.of(employeeDTO));
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("BADGE1")));
        verify(employeeService).getEmployee(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"})
    void testGetEmployee_NonExistent_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployee(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void testGetAllEmployees_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        Page<EmployeeDTO> page = new PageImpl<>(List.of(employeeDTO), pageable, 1);
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId", is("BADGE1")));
        verify(employeeService).getAllEmployees(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_ValidId_ReturnsNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_NonExistent_ReturnsNotFound() throws Exception {
        doThrow(new jakarta.persistence.EntityNotFoundException("Employee not found")).when(employeeService).deleteEmployee(99L);
        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUnauthorizedAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testGetAllEmployees_UnauthorizedRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_BoundaryBadgeIdLength() throws Exception {
        EmployeeDTO dto = EmployeeDTO.builder().badgeId("B".repeat(32)).name("Boundary").role("WORKER").status("ACTIVE").build();
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(dto);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId", is("B".repeat(32))));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_XssAndSqlInjection() throws Exception {
        EmployeeDTO dto = EmployeeDTO.builder().badgeId("<script>alert('xss')</script>").name("'; DROP TABLE employee; --").role("WORKER").status("ACTIVE").build();
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(dto);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId", is("<script>alert('xss')</script>")))
                .andExpect(jsonPath("$.name", is("'; DROP TABLE employee; --")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllEmployees_PaginationBoundary() throws Exception {
        Pageable pageable = PageRequest.of(100, 2);
        Page<EmployeeDTO> page = new PageImpl<>(List.of(), pageable, 0);
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees")
                .param("page", "100")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
