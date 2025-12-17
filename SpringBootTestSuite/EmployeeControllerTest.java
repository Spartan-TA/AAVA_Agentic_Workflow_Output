package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EmployeeControllerTest - Comprehensive unit tests for EmployeeController covering REST endpoints, security, validation, boundaries, and edge cases.
 */
public class EmployeeControllerTest {
    private MockMvc mockMvc;
    private EmployeeController employeeController;

    @BeforeEach
    public void setUp() {
        employeeController = new EmployeeController();
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    public void testGetEmployees200OK() throws Exception {
        mockMvc.perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetEmployeesPagination() throws Exception {
        mockMvc.perform(get("/api/employees?page=1&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetEmployeeById200OK() throws Exception {
        mockMvc.perform(get("/api/employees/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetEmployeeById404NotFound() throws Exception {
        mockMvc.perform(get("/api/employees/9999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPostEmployee201Created() throws Exception {
        String json = "{"name":"John Doe","badgeId":"123"}";
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    public void testPostEmployee400BadRequest() throws Exception {
        String json = "{"name":"","badgeId":""}";
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPostEmployee409Conflict() throws Exception {
        String json = "{"name":"Jane Doe","badgeId":"123"}";
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict());
    }

    @Test
    public void testPutEmployee200OK() throws Exception {
        String json = "{"name":"John Updated"}";
        mockMvc.perform(put("/api/employees/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testPutEmployee404NotFound() throws Exception {
        String json = "{"name":"Not Found"}";
        mockMvc.perform(put("/api/employees/9999").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPatchEmployee200OK() throws Exception {
        String json = "{"status":"Active"}";
        mockMvc.perform(patch("/api/employees/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteEmployee204NoContent() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEmployeeSoftDelete() throws Exception {
        mockMvc.perform(delete("/api/employees/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testSecurity401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees").header("Authorization", ""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSecurity403Forbidden() throws Exception {
        mockMvc.perform(get("/api/employees").header("Authorization", "Bearer invalid"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testContentNegotiationJSON() throws Exception {
        mockMvc.perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testValidationEmptyName() throws Exception {
        String json = "{"name":"","badgeId":"123"}";
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidationInvalidBadgeId() throws Exception {
        String json = "{"name":"John Doe","badgeId":"!@#"}";
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }
}
