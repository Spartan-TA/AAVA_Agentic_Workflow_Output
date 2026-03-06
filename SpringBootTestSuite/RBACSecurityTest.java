import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RBACSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void adminAccessAllowed() throws Exception {
        mockMvc.perform(get("/admin/secure-endpoint"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void hrAccessAllowed() throws Exception {
        mockMvc.perform(get("/hr/secure-endpoint"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void supervisorAccessAllowed() throws Exception {
        mockMvc.perform(get("/supervisor/secure-endpoint"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void workerAccessAllowed() throws Exception {
        mockMvc.perform(get("/worker/secure-endpoint"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthorizedAccessReturns401() throws Exception {
        mockMvc.perform(get("/admin/secure-endpoint"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void forbiddenAccessReturns403() throws Exception {
        mockMvc.perform(get("/admin/secure-endpoint"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void rowLevelSecurityEnforced() throws Exception {
        mockMvc.perform(get("/hr/employees/123"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/hr/employees/999"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void oauth2ToggleEnabled() throws Exception {
        mockMvc.perform(get("/security/oauth2-status"))
                .andExpect(status().isOk())
                .andExpect(content().string("enabled"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void apiKeyToggleEnabled() throws Exception {
        mockMvc.perform(get("/security/apikey-status"))
                .andExpect(status().isOk())
                .andExpect(content().string("enabled"));
    }
}
