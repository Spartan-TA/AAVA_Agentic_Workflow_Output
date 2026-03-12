package SpringBootTestSuite;

import com.example.warehouse.controller.EmployeeController;
import com.example.warehouse.model.Employee;
import com.example.warehouse.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for EmployeeController.
 * Uses MockMvc to test all REST endpoints and security constraints.
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setDepartment("Shipping");
        employee.setStatus("ACTIVE");
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testCreateEmployee_ValidInput_Returns201() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","badgeId":"BADGE123","role":"WORKER","department":"Shipping"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEmployee_InvalidInput_Returns400() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenThrow(new IllegalArgumentException("Invalid input"));
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"","badgeId":"","role":"","department":""}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testCreateEmployee_UnauthorizedRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","badgeId":"BADGE123","role":"WORKER","department":"Shipping"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    void testGetAllEmployees_Returns200WithList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Collections.singletonList(employee));
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testGetAllEmployees_UnauthorizedRole_Returns403() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployeeById_ValidId_Returns200() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employee));
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployeeById_InvalidId_Returns404() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployeeByBadgeId_ValidBadgeId_Returns200() throws Exception {
        when(employeeService.getEmployeeByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        mockMvc.perform(get("/api/employees/badge/BADGE123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployeeByBadgeId_InvalidBadgeId_Returns404() throws Exception {
        when(employeeService.getEmployeeByBadgeId("BADGE999")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/employees/badge/BADGE999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testUpdateEmployee_ValidInput_Returns200() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(employee);
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","badgeId":"BADGE123","role":"WORKER","department":"Shipping"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testUpdateEmployee_InvalidId_Returns404() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(Employee.class))).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(put("/api/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","badgeId":"BADGE123","role":"WORKER","department":"Shipping"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testUpdateEmployee_UnauthorizedRole_Returns403() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","badgeId":"BADGE123","role":"WORKER","department":"Shipping"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testPatchEmployee_ValidInput_Returns200() throws Exception {
        when(employeeService.patchEmployee(eq(1L), any(Employee.class))).thenReturn(employee);
        mockMvc.perform(patch("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"department":"Receiving"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testPatchEmployee_InvalidId_Returns404() throws Exception {
        when(employeeService.patchEmployee(eq(99L), any(Employee.class))).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(patch("/api/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"department":"Receiving"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_ValidId_Returns204() throws Exception {
        when(employeeService.softDeleteEmployee(1L)).thenReturn(employee);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEmployee_InvalidId_Returns404() throws Exception {
        when(employeeService.softDeleteEmployee(99L)).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testDeleteEmployee_UnauthorizedRole_Returns403() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEmployeesByDepartment_ValidDepartment_Returns200() throws Exception {
        when(employeeService.getEmployeesByDepartment("Shipping")).thenReturn(Collections.singletonList(employee));
        mockMvc.perform(get("/api/employees/department/Shipping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Shipping"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    void testGetEmployeesByRole_ValidRole_Returns200() throws Exception {
        when(employeeService.getEmployeesByRole("WORKER")).thenReturn(Collections.singletonList(employee));
        mockMvc.perform(get("/api/employees/role/WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("WORKER"));
    }
}
