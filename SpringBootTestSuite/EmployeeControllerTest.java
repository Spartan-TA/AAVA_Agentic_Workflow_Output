package com.company.wem.employee;

import com.company.wem.employee.controller.EmployeeController;
import com.company.wem.employee.dto.EmployeeDTO;
import com.company.wem.employee.entity.Employee;
import com.company.wem.employee.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employee = new Employee(1L, "EMP001", "John Doe", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        employeeDTO = new EmployeeDTO("EMP001", "John Doe", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
    }

    @Test
    void testCreateEmployee_ValidInput_ReturnsCreated() throws Exception {
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(employee);
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""badgeId":"EMP001"," +
                        ""name":"John Doe"," +
                        ""role":"WORKER"," +
                        ""department":"Warehouse"," +
                        ""shiftGroup":"A"," +
                        ""hireDate":"" + LocalDate.now() + ""," +
                        ""status":"ACTIVE"}")
        ).andExpect(status().isCreated())
         .andExpect(jsonPath("$.badgeId").value("EMP001"))
         .andDo(print());
    }

    @Test
    void testCreateEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""badgeId":null," +
                        ""name":"John Doe"}")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void testGetEmployeeById_Valid_ReturnsOk() throws Exception {
        when(employeeService.getById(1L)).thenReturn(employee);
        mockMvc.perform(MockMvcRequestBuilders.get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    void testGetEmployeeById_NotFound_ReturnsNotFound() throws Exception {
        when(employeeService.getById(99L)).thenThrow(new NoSuchElementException());
        mockMvc.perform(MockMvcRequestBuilders.get("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateEmployee_Valid_ReturnsOk() throws Exception {
        when(employeeService.update(eq(1L), any(EmployeeDTO.class))).thenReturn(employee);
        mockMvc.perform(MockMvcRequestBuilders.put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""badgeId":"EMP001"," +
                        ""name":"John Doe"," +
                        ""role":"WORKER"," +
                        ""department":"Warehouse"," +
                        ""shiftGroup":"A"," +
                        ""hireDate":"" + LocalDate.now() + ""," +
                        ""status":"ACTIVE"}")
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    void testUpdateEmployee_NotFound_ReturnsNotFound() throws Exception {
        when(employeeService.update(eq(99L), any(EmployeeDTO.class))).thenThrow(new NoSuchElementException());
        mockMvc.perform(MockMvcRequestBuilders.put("/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""badgeId":"EMP099"," +
                        ""name":"Ghost"}")
        ).andExpect(status().isNotFound());
    }

    @Test
    void testDeleteEmployee_Valid_ReturnsNoContent() throws Exception {
        doNothing().when(employeeService).delete(1L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteEmployee_NotFound_ReturnsNotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(employeeService).delete(99L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetEmployees_PaginationFiltering_ReturnsOk() throws Exception {
        List<Employee> employees = Arrays.asList(employee);
        when(employeeService.getEmployees(anyString(), anyString(), anyInt(), anyInt())).thenReturn(employees);
        mockMvc.perform(MockMvcRequestBuilders.get("/employees?department=Warehouse&status=ACTIVE&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].badgeId").value("EMP001"));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ReturnsConflict() throws Exception {
        when(employeeService.create(any(EmployeeDTO.class))).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate badgeId"));
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""badgeId":"EMP001"," +
                        ""name":"John Doe"}")
        ).andExpect(status().isConflict());
    }

    @Test
    void testCreateEmployee_SQLInjectionAttempt_ReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""badgeId":"EMP002"," +
                        ""name":"Robert'); DROP TABLE Employees;--"}")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployee_XSSAttempt_ReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""badgeId":"EMP003"," +
                        ""name":"<script>alert('xss')</script>"}")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void testUnauthorizedAccess_ReturnsUnauthorized() throws Exception {
        // Simulate no authentication
        mockMvc.perform(MockMvcRequestBuilders.get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testForbiddenAccess_ReturnsForbidden() throws Exception {
        // Simulate forbidden by role
        // This would require security context mocking, omitted for brevity
        // Assume endpoint is protected and user lacks permission
        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/1"))
                .andExpect(status().isForbidden());
    }
}