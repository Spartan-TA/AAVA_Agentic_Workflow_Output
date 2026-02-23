package com.example.controller;

import com.example.dto.EmployeeDto;
import com.example.entity.Employee;
import com.example.entity.EmployeeStatus;
import com.example.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void createEmployee_validInput_returnsCreated() throws Exception {
        EmployeeDto dto = new EmployeeDto("B123", "John Doe", "ENGINEER", "IT", "A", LocalDate.now(), EmployeeStatus.ACTIVE, List.of(), List.of());
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeService.createEmployee(any())).thenReturn(emp);

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"B123","name":"John Doe","role":"ENGINEER","department":"IT","shiftGroup":"A","hireDate":"2022-01-01","status":"ACTIVE"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void createEmployee_forbiddenForWorker() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"B123","name":"John Doe","role":"ENGINEER","department":"IT","shiftGroup":"A","hireDate":"2022-01-01","status":"ACTIVE"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void getEmployee_found_returnsOk() throws Exception {
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeService.getEmployee(1L)).thenReturn(emp);

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void getEmployee_notFound_returnsNotFound() throws Exception {
        when(employeeService.getEmployee(1L)).thenThrow(new com.example.exception.ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void updateEmployee_validInput_returnsOk() throws Exception {
        EmployeeDto dto = new EmployeeDto("B123", "Jane Doe", "MANAGER", "HR", "B", LocalDate.now(), EmployeeStatus.ACTIVE, List.of(), List.of());
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(emp);

        mockMvc.perform(put("/api/v1/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"B123","name":"Jane Doe","role":"MANAGER","department":"HR","shiftGroup":"B","hireDate":"2022-01-01","status":"ACTIVE"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteEmployee_adminRole_returnsNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void deleteEmployee_hrRole_forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void getAllEmployees_withPagination_returnsOk() throws Exception {
        Page<Employee> page = new PageImpl<>(List.of(new Employee(), new Employee()), PageRequest.of(0, 10), 2);
        when(employeeService.getAllEmployees(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/employees?page=0&size=10&department=IT&status=ACTIVE"))
                .andExpect(status().isOk());
    }
}