package com.warehouse.employee.management;

import com.warehouse.employee.management.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    // Test endpoint security rules
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testAdminAccess_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    public void testHrAccess_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    public void testSupervisorAccess_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    public void testWorkerAccess_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isForbidden());
    }

    @Test
    public void testUnauthorizedAccess_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testApiKeyAuthentication_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("X-API-KEY", "valid-api-key"))
                .andExpect(status().isOk());
    }

    @Test
    public void testApiKeyAuthentication_InvalidKey_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("X-API-KEY", "invalid-api-key"))
                .andExpect(status().isUnauthorized());
    }
}
