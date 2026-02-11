package SpringBootTestSuite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.controller.EmployeeController;
import com.example.service.EmployeeService;
import com.example.model.Employee;

import java.util.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_ValidRequest_Returns201() throws Exception {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("B123");
        when(employeeService.createEmployee(any())).thenReturn(employee);
        mockMvc.perform(post("/employees")
                .contentType("application/json")
                .content("{"name":"John","badgeId":"B123"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidRequest_Returns400() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType("application/json")
                .content("{"name":"","badgeId":""}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DuplicateBadgeId_Returns400() throws Exception {
        when(employeeService.createEmployee(any())).thenThrow(new IllegalArgumentException("Duplicate badgeId"));
        mockMvc.perform(post("/employees")
                .contentType("application/json")
                .content("{"name":"John","badgeId":"B123"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployee_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType("application/json")
                .content("{"name":"John","badgeId":"B123"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_Returns200WithPagedResults() throws Exception {
        List<Employee> employees = Arrays.asList(new Employee());
        when(employeeService.getAllEmployees(anyInt(), anyInt())).thenReturn(employees);
        mockMvc.perform(get("/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_Exists_Returns200() throws Exception {
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employee));
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NotFound_Returns404() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_ValidRequest_Returns200() throws Exception {
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(Optional.of(employee));
        mockMvc.perform(put("/employees/1")
                .contentType("application/json")
                .content("{"name":"Jane"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_AdminRole_Returns204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testDeleteEmployee_NonAdminRole_Returns403() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isForbidden());
    }
}
