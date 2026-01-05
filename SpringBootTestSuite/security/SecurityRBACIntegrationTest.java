package com.example.warehouse.test.security;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRBACIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminAccess_Allowed_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "worker", roles = {"WORKER"})
    void testWorkerAccess_Forbidden_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR"})
    void testHRAccess_AllowedToRead_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    void testSupervisorAccess_AllowedToRead_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }
}