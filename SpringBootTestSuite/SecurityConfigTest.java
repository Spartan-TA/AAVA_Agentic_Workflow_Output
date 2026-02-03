package com.example.warehouse.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminAccess_AllEndpoints_Allowed() throws Exception {
        mockMvc.perform(get("/employees")).andExpect(status().isOk());
        mockMvc.perform(post("/employees")).andExpect(status().isCreated());
        mockMvc.perform(delete("/employees/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testWorkerAccess_RestrictedEndpoints_Denied() throws Exception {
        mockMvc.perform(post("/employees")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/employees/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testSupervisorAccess_TeamDataOnly_Allowed() throws Exception {
        mockMvc.perform(get("/employees")).andExpect(status().isOk());
        mockMvc.perform(delete("/employees/1")).andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedAccess_ProtectedEndpoints_Returns401() throws Exception {
        mockMvc.perform(get("/employees")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/employees")).andExpect(status().isUnauthorized());
    }
}