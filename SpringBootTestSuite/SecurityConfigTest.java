package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SecurityConfig.
 * Verifies endpoint access for all roles and unauthenticated users.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testEndpointSecurity_AdminAccess_AllEndpoints() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"A","badgeId":"B","role":"ADMIN","department":"HR"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/employees/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testEndpointSecurity_HRAccess_LimitedEndpoints() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"A","badgeId":"B","role":"HR","department":"HR"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/employees/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void testEndpointSecurity_SupervisorAccess_LimitedEndpoints() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"A","badgeId":"B","role":"SUPERVISOR","department":"Ops"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testEndpointSecurity_WorkerAccess_SelfServiceOnly() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"A","badgeId":"B","role":"WORKER","department":"Ops"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEndpointSecurity_UnauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"name":"A","badgeId":"B","role":"WORKER","department":"Ops"}"))
                .andExpect(status().isUnauthorized());
    }
}
