package com.company.wems;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.*;

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

    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
    }

    @Test
    public void testGetAllEmployees_DefaultPagination() throws Exception {
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees, PageRequest.of(0, 10), 1);
        when(employeeService.filterEmployees(any(), any(), any())).thenReturn(page);
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    public void testGetAllEmployees_WithPaginationAndFiltering() throws Exception {
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees, PageRequest.of(0, 1), 1);
        when(employeeService.filterEmployees(eq("Worker"), eq("Logistics"), any())).thenReturn(page);
        mockMvc.perform(get("/employees")
                .param("role", "Worker")
                .param("department", "Logistics")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").value("Worker"));
    }

    @Test
    public void testGetEmployeeById_Found() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employee));
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testGetEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateEmployee_Valid() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testCreateEmployee_NullBody() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateEmployee_Valid() throws Exception {
        Employee updated = Employee.builder().id(1L).name("Jane Smith").badgeId("BADGE123").role("Manager").department("HR").shiftGroup("B").hireDate(LocalDate.of(2021, 5, 10)).status("Inactive").deleted(false).build();
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(updated);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Smith"));
    }

    @Test
    public void testUpdateEmployee_NotFound() throws Exception {
        when(employeeService.updateEmployee(eq(2L), any(Employee.class))).thenThrow(new NoSuchElementException());
        mockMvc.perform(put("/employees/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteEmployee_Valid() throws Exception {
        doNothing().when(employeeService).softDeleteEmployee(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEmployee_NotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(employeeService).softDeleteEmployee(2L);
        mockMvc.perform(delete("/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateEmployee_EmptyFields() throws Exception {
        Employee emp = Employee.builder().name("").badgeId("").role("").department("").shiftGroup("").hireDate(null).status("").deleted(false).build();
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(emp);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(""));
    }

    @Test
    public void testCreateEmployee_BoundaryFields() throws Exception {
        String longString = "x".repeat(255);
        Employee emp = Employee.builder().name(longString).badgeId(longString).role(longString).department(longString).shiftGroup(longString).hireDate(LocalDate.now()).status(longString).deleted(false).build();
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(emp);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(longString));
    }
}
