package com.company.wms.leave.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.http.MediaType;

@WebMvcTest(LeaveController.class)
public class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    @Test
    @WithMockUser(roles = "WORKER")
    public void testCreateLeaveRequest_WithValidInput_Returns201() throws Exception {
        when(leaveService.createLeaveRequest(any())).thenReturn(new LeaveRequest(1L, 1L, "SICK", "PENDING", "2024-06-01", "2024-06-02", ""));
        mockMvc.perform(post("/api/v1/leave/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":1,"type":"SICK","startDate":"2024-06-01","endDate":"2024-06-02"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testCreateLeaveRequest_WithInvalidInput_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/leave/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateLeaveRequest_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/leave/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testApproveLeave_AsSupervisor_Returns200() throws Exception {
        when(leaveService.approveLeave(anyLong())).thenReturn(true);
        mockMvc.perform(put("/api/v1/leave/requests/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testApproveLeave_AsWorker_Returns403() throws Exception {
        mockMvc.perform(put("/api/v1/leave/requests/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testDenyLeave_WithValidReason_Returns200() throws Exception {
        when(leaveService.denyLeave(anyLong(), anyString())).thenReturn(true);
        mockMvc.perform(put("/api/v1/leave/requests/1/deny")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"reason":"Not eligible"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetEmployeeLeaveRequests_WithPagination_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/leave/requests/employee/1?page=0&size=10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testGetPendingRequests_AsSupervisor_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/leave/requests/pending"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetLeaveBalance_WithValidEmployeeId_Returns200() throws Exception {
        when(leaveService.getLeaveBalance(anyLong())).thenReturn(5);
        mockMvc.perform(get("/api/v1/leave/balance/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testGetLeaveBalance_WithInvalidEmployeeId_Returns404() throws Exception {
        when(leaveService.getLeaveBalance(anyLong())).thenThrow(new ResourceNotFoundException("Employee not found"));
        mockMvc.perform(get("/api/v1/leave/balance/99"))
                .andExpect(status().isNotFound());
    }
}
