import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LeaveAbsenceManagementTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void requestPTOLeave() throws Exception {
        mockMvc.perform(post("/leave/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":123,"type":"PTO","start":"2024-06-10","end":"2024-06-12"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    void requestSickLeave() throws Exception {
        mockMvc.perform(post("/leave/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":123,"type":"SICK","start":"2024-06-15","end":"2024-06-16"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    void supervisorApprovesLeave() throws Exception {
        mockMvc.perform(post("/leave/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"requestId":1,"supervisorId":456}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));
    }

    @Test
    void supervisorDeniesLeave() throws Exception {
        mockMvc.perform(post("/leave/deny")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"requestId":2,"supervisorId":456,"reason":"Insufficient balance"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("denied"));
    }

    @Test
    void leaveBalanceUpdated() throws Exception {
        mockMvc.perform(get("/leave/balance?employeeId=123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists());
    }

    @Test
    void leaveSchedulingIntegration() throws Exception {
        mockMvc.perform(get("/leave/schedule-integration?employeeId=123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.integration").value(true));
    }

    @Test
    void exportApprovedLeaves() throws Exception {
        mockMvc.perform(get("/leave/export-approved?employeeId=123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }
}
