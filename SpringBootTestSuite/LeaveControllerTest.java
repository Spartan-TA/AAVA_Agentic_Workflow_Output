package com.warehouse.ems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.dto.LeaveRequestDto;
import com.warehouse.ems.entity.LeaveRequest;
import com.warehouse.ems.entity.LeaveBalance;
import com.warehouse.ems.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for LeaveController.
 * Covers successful requests, error responses, security, and validation.
 */
@WebMvcTest(LeaveController.class)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    @Autowired
    private ObjectMapper objectMapper;

    private LeaveRequest leaveRequest;
    private LeaveRequestDto leaveRequestDto;
    private LeaveBalance leaveBalance;

    @BeforeEach
    void setUp() {
        leaveRequest = new LeaveRequest();
        leaveRequest.setId(1L);
        leaveRequest.setLeaveType("PTO");
        leaveRequest.setStatus("PENDING");
        leaveRequest.setReason("Vacation");
        leaveRequest.setStartDate(LocalDate.now());
        leaveRequest.setEndDate(LocalDate.now().plusDays(2));

        leaveRequestDto = new LeaveRequestDto();
        leaveRequestDto.setEmployeeId(1L);
        leaveRequestDto.setStartDate(LocalDate.now());
        leaveRequestDto.setEndDate(LocalDate.now().plusDays(2));
        leaveRequestDto.setLeaveType("PTO");
        leaveRequestDto.setReason("Vacation");

        leaveBalance = new LeaveBalance();
        leaveBalance.setLeaveType("PTO");
        leaveBalance.setBalance(10.0);
    }

    /**
     * Test POST /leaves with WORKER role returns 201 Created.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateLeaveRequest_WorkerRole_ReturnsCreated() throws Exception {
        when(leaveService.createLeaveRequest(any(LeaveRequestDto.class))).thenReturn(leaveRequest);
        mockMvc.perform(post("/leaves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leaveRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaveType").value("PTO"));
    }

    /**
     * Test POST /leaves with no authentication returns 401 Unauthorized.
     */
    @Test
    void testCreateLeaveRequest_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/leaves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leaveRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test POST /leaves with invalid DTO returns 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateLeaveRequest_InvalidDto_ReturnsBadRequest() throws Exception {
        LeaveRequestDto invalidDto = new LeaveRequestDto();
        mockMvc.perform(post("/leaves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test POST /leaves/approve with SUPERVISOR role returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testApproveLeave_SupervisorRole_ReturnsOk() throws Exception {
        when(leaveService.approveLeave(eq(1L), eq(2L))).thenReturn(leaveRequest);
        mockMvc.perform(post("/leaves/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""requestId":1," +
                        ""approverId":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    /**
     * Test POST /leaves/reject with SUPERVISOR role returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testRejectLeave_SupervisorRole_ReturnsOk() throws Exception {
        when(leaveService.rejectLeave(eq(1L), eq(2L), eq("Not eligible"))).thenReturn(leaveRequest);
        mockMvc.perform(post("/leaves/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""requestId":1," +
                        ""approverId":2," +
                        ""reason":"Not eligible"}"))
                .andExpect(status().isOk());
    }

    /**
     * Test GET /leaves/balance with valid params returns 200 OK.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    void testGetLeaveBalance_ValidParams_ReturnsOk() throws Exception {
        when(leaveService.getLeaveBalance(eq(1L), eq("PTO"))).thenReturn(leaveBalance);
        mockMvc.perform(get("/leaves/balance?employeeId=1&leaveType=PTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(10.0));
    }

    /**
     * Test POST /leaves/accrue with ADMIN role returns 204 No Content.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAccrueLeave_AdminRole_ReturnsNoContent() throws Exception {
        mockMvc.perform(post("/leaves/accrue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""employeeId":1}"))
                .andExpect(status().isNoContent());
    }

    /**
     * Test POST /leaves/approve with WORKER role returns 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "WORKER")
    void testApproveLeave_WorkerRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/leaves/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        ""requestId":1," +
                        ""approverId":2}"))
                .andExpect(status().isForbidden());
    }
}
