package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Setup logic if needed
    }

    @AfterEach
    void tearDown() {
        // Teardown logic if needed
    }

    // ADMIN role tests
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessAllEndpoints_ShouldReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees"))
                .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","role":"WORKER"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Jane Doe","role":"HR"}"))
                .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"role":"SUPERVISOR"}"))
                .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    // HR role tests
    @Test
    @WithMockUser(username = "hruser", roles = {"HR"})
    void testHrCanCreateAndUpdateEmployees_ShouldReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","role":"WORKER"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Jane Doe","role":"HR"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "hruser", roles = {"HR"})
    void testHrCannotDeleteEmployees_ShouldReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    // SUPERVISOR role tests
    @Test
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    void testSupervisorHasReadOnlyAccess_ShouldReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/team"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    void testSupervisorCannotModifyEmployees_ShouldReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"John Doe","role":"WORKER"}"))
                .andExpect(status().isForbidden());
    }

    // WORKER role tests
    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    void testWorkerHasReadOnlyAccess_ShouldReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    void testWorkerCannotModifyEmployees_ShouldReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"Jane Doe","role":"HR"}"))
                .andExpect(status().isForbidden());
    }

    // API Key authentication tests
    @Test
    void testApiKeyAuthenticationWithValidKey_ShouldReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                .header("X-API-Key", "valid-api-key"))
                .andExpect(status().isOk());
    }

    @Test
    void testApiKeyAuthenticationWithInvalidKey_ShouldReturn401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                .header("X-API-Key", "invalid-api-key"))
                .andExpect(status().isUnauthorized());
    }

    // OAuth2 authentication test (if enabled)
    @Test
    void testOAuth2Authentication_ShouldReturn200Or401() throws Exception {
        // Simulate OAuth2 token (replace with actual token if available)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                .header("Authorization", "Bearer valid-oauth2-token"))
                .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                .header("Authorization", "Bearer invalid-oauth2-token"))
                .andExpect(status().isUnauthorized());
    }

    // Unauthorized requests
    @Test
    void testUnauthorizedRequest_ShouldReturn401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    // Forbidden requests
    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    void testForbiddenRequest_ShouldReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    // CORS configuration
    @Test
    void testCorsConfigurationAllowsExpectedOrigins() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options("/api/employees")
                .header("Origin", "http://allowed-origin.com")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://allowed-origin.com"))
                .andExpect(status().isOk());
    }

    // Password encoding with BCrypt
    @Test
    void testPasswordEncodingWithBCrypt_ShouldMatchEncodedPassword() {
        String rawPassword = "password123";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
        Assertions.assertTrue(new BCryptPasswordEncoder().matches(rawPassword, encodedPassword));
    }
}