package com.company.warehouse.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeDTO employeeDTO;

    @BeforeEach
    public void setUp() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(1L);
        employeeDTO.setName("John Doe");
        employeeDTO.setBadgeId("ABC123");
        employeeDTO.setRole("Worker");
        employeeDTO.setDepartment("Logistics");
        employeeDTO.setShiftGroup("A");
        employeeDTO.setHireDate(LocalDate.now().minusDays(10));
        employeeDTO.setStatus("ACTIVE");
    }

    @Test
    public void testCreateEmployee_WithValidRequest_ShouldReturn201Created() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(employeeDTO);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.badgeId", is("ABC123")));
    }

    @Test
    public void testCreateEmployee_WithInvalidData_ShouldReturn400BadRequest() throws Exception {
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setName(""); // NotBlank
        invalidDTO.setBadgeId("bad"); // Pattern
        invalidDTO.setRole("");
        invalidDTO.setDepartment("");
        invalidDTO.setHireDate(LocalDate.now().plusDays(1)); // Future date
        invalidDTO.setStatus("WRONG"); // Pattern
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ShouldReturn409Conflict() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenThrow(new DuplicateBadgeIdException("Badge ID already exists"));
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testGetEmployeeById_WithValidId_ShouldReturn200OK() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeDTO);
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.badgeId", is("ABC123")));
    }

    @Test
    public void testGetEmployeeById_WithInvalidId_ShouldReturn404NotFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenThrow(new EntityNotFoundException("Employee not found"));
        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllEmployees_WithPagination_ShouldReturn200OK() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeDTO> page = new PageImpl<>(Arrays.asList(employeeDTO), pageable, 1);
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].badgeId", is("ABC123")));
    }

    @Test
    public void testUpdateEmployee_WithValidData_ShouldReturn200OK() throws Exception {
        EmployeeDTO updatedDTO = new EmployeeDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Jane Doe");
        updatedDTO.setBadgeId("XYZ789");
        updatedDTO.setRole("Supervisor");
        updatedDTO.setDepartment("Shipping");
        updatedDTO.setShiftGroup("B");
        updatedDTO.setHireDate(LocalDate.now().minusDays(20));
        updatedDTO.setStatus("ACTIVE");
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(updatedDTO);
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Doe")))
                .andExpect(jsonPath("$.badgeId", is("XYZ789")));
    }

    @Test
    public void testUpdateEmployee_WithInvalidData_ShouldReturn400BadRequest() throws Exception {
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setName("");
        invalidDTO.setBadgeId("bad");
        invalidDTO.setRole("");
        invalidDTO.setDepartment("");
        invalidDTO.setHireDate(LocalDate.now().plusDays(1));
        invalidDTO.setStatus("WRONG");
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteEmployee_WithValidId_ShouldReturn204NoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEmployee_WithInvalidId_ShouldReturn404NotFound() throws Exception {
        doThrow(new EntityNotFoundException("Employee not found")).when(employeeService).deleteEmployee(2L);
        mockMvc.perform(delete("/api/employees/2"))
                .andExpect(status().isNotFound());
    }
}
