package com.warehouse.employee.management.config;

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
class SecurityConfigTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void testAccessDenied_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAdminAccess_CreateEmployee() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest()); // Fails validation, but not forbidden
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testHRAccess_DeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNotFound()); // Not found, but not forbidden
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void testSupervisorAccess_CannotDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testWorkerAccess_CannotListEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAdminAccess_ListEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testHRAccess_ListEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void testSupervisorAccess_ListEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testWorkerAccess_GetEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isNotFound()); // Not found, but not forbidden
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAdminAccess_UpdateEmployee() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testHRAccess_UpdateEmployee() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void testSupervisorAccess_UpdateEmployee() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testWorkerAccess_UpdateEmployee_Forbidden() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}
