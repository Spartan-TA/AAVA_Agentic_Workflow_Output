package com.wms.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit tests for SecurityConfig covering role-based access and security edge cases.
 */
@WebMvcTest
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        // Any setup if needed
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testAdminAccess_EmployeeDelete_Allowed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    public void testHRAccess_EmployeeDelete_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    public void testHRAccess_EmployeeCreate_Allowed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/employees").contentType("application/json").content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    public void testSupervisorAccess_EmployeeList_Allowed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    public void testWorkerAccess_EmployeeList_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUnauthorizedAccess_EmployeeList_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testAdminAccess_AttendanceClockIn_Allowed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/attendance/clock-in").param("employeeId", "1").param("deviceInfo", "Terminal1"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    public void testWorkerAccess_AttendanceClockIn_Allowed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/attendance/clock-in").param("employeeId", "1").param("deviceInfo", "Terminal1"))
                .andExpect(status().isCreated());
    }

    @Test
    public void testUnauthorizedAccess_AttendanceClockIn_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/attendance/clock-in").param("employeeId", "1").param("deviceInfo", "Terminal1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testAdminAccess_AttendanceList_Allowed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/attendance/employee/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    public void testWorkerAccess_AttendanceList_Allowed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/attendance/employee/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnauthorizedAccess_AttendanceList_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/attendance/employee/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    public void testHRAccess_EmployeeUpdate_Allowed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/employees/1").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    public void testSupervisorAccess_EmployeeUpdate_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/employees/1").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }
}
