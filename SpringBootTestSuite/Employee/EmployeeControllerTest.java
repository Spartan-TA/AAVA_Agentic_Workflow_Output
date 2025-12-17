package com.example.warehouse.employee;

import com.example.warehouse.employee.controller.EmployeeController;
import com.example.warehouse.employee.dto.EmployeeDTO;
import com.example.warehouse.employee.entity.Employee;
import com.example.warehouse.employee.service.EmployeeService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO validDto;
    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        validDto = new EmployeeDTO();
        validDto.setName("John Doe");
        validDto.setBadgeId("BADGE123");
        validDto.setRole("WORKER");
        validDto.setDepartment("Shipping");
        validDto.setShiftGroup("A");
        validDto.setHireDate(LocalDate.now());
        validDto.setStatus("ACTIVE");

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName(validDto.getName());
        validEmployee.setBadgeId(validDto.getBadgeId());
        validEmployee.setRole(validDto.getRole());
        validEmployee.setDepartment(validDto.getDepartment());
        validEmployee.setShiftGroup(validDto.getShiftGroup());
        validEmployee.setHireDate(validDto.getHireDate());
        validEmployee.setStatus(validDto.getStatus());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testCreateEmployee_ValidInput_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployee);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""name": "John Doe"," +
                        ""badgeId": "BADGE123"," +
                        ""role": "WORKER"," +
                        ""department": "Shipping"," +
                        ""shiftGroup": "A"," +
                        ""hireDate": "" + LocalDate.now() + ""," +
                        ""status": "ACTIVE"}")
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testCreateEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    public void testGetAllEmployees_ReturnsPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(validEmployee));
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("BADGE123"));
    }

    @Test
    public void testGetEmployeeById_ValidId_ReturnsEmployee() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(validEmployee);
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    @Test
    public void testGetEmployeeById_InvalidId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testUpdateEmployee_ValidInput_ReturnsUpdated() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(validEmployee);
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""name": "Jane Doe"," +
                        ""badgeId": "BADGE123"," +
                        ""role": "SUPERVISOR"," +
                        ""department": "Receiving"," +
                        ""shiftGroup": "B"," +
                        ""hireDate": "" + LocalDate.now() + ""," +
                        ""status": "ACTIVE"}")
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testUpdateEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteEmployee_ValidId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    public void testDeleteEmployee_Unauthorized_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}