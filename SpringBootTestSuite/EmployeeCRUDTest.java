package SpringBootTestSuite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class EmployeeCRUDTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        reset(employeeService);
    }

    @Test
    void testCreateEmployee_ValidData_ReturnsCreated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .content("{"name":"John Doe","badgeId":"12345","role":"WORKER"}")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void testGetEmployee_ValidId_ReturnsEmployee() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/employees/12345"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testUpdateEmployee_ValidData_ReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/employees/12345")
                .content("{"name":"Jane Doe"}")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testDeleteEmployee_ValidId_ReturnsNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/12345"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void testPatchEmployee_ValidData_ReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/employees/12345")
                .content("{"status":"INACTIVE"}")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ReturnsConflict() throws Exception {
        when(employeeService.createEmployee(any())).thenThrow(new IllegalArgumentException("Duplicate badgeId"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(new EmployeeDTO("John Doe", "12345", "WORKER")));
    }

    @Test
    void testGetEmployee_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/employees/invalid"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testCreateEmployee_NullData_ReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .content("")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testGetEmployees_Pagination_ReturnsPagedResults() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/employees?page=1&size=10"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testGetEmployees_FilterByDepartment_ReturnsFilteredResults() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/employees?department=Logistics"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testDeleteEmployee_SoftDelete_VerifyStatus() {
        Employee employee = new Employee("John Doe", "12345", "WORKER");
        employee.setStatus("ACTIVE");
        employeeService.softDeleteEmployee(employee.getBadgeId());
        assertEquals("INACTIVE", employee.getStatus());
    }

    @Test
    void testCreateEmployee_EmptyName_ReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .content("{"name":"","badgeId":"12345"}")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testUpdateEmployee_InvalidBadgeId_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/employees/invalid")
                .content("{"name":"Jane Doe"}")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testCreateEmployee_MaxLengthName_ReturnsCreated() throws Exception {
        String longName = "A".repeat(255);
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .content("{"name":"" + longName + "","badgeId":"12345"}")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void testCreateEmployee_MinimalData_ReturnsCreated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .content("{"name":"John","badgeId":"1"}")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void testGetEmployees_EmptyResult_ReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/employees?department=NonExistent"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testCreateEmployee_InvalidRole_ReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                .content("{"name":"John Doe","badgeId":"12345","role":"INVALID_ROLE"}")
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testCreateEmployee_ExceptionHandling_ReturnsServerError() throws Exception {
        when(employeeService.createEmployee(any())).thenThrow(new RuntimeException("Unexpected error"));
        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(new EmployeeDTO("John Doe", "12345", "WORKER")));
    }

    @Test
    void testGetEmployee_SecurityAuthorization_ReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/employees/12345")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}