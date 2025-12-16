import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class LeaveControllerTest {
    private MockMvc mockMvc;

    @Mock
    private LeaveService leaveService;

    @InjectMocks
    private LeaveController leaveController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(leaveController).build();
    }

    @Test
    public void testSubmitLeaveRequest_ValidInput_ReturnsCreated() throws Exception {
        LeaveRequestDto requestDto = new LeaveRequestDto("EMP123", "PTO", "2024-07-01", "2024-07-05", "Vacation");
        when(leaveService.submitLeaveRequest(any())).thenReturn(new LeaveResponseDto("APPROVAL_PENDING", null));

        mockMvc.perform(post("/leave/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","type":"PTO","startDate":"2024-07-01","endDate":"2024-07-05","reason":"Vacation"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVAL_PENDING"));
    }

    @Test
    public void testSubmitLeaveRequest_NullInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/leave/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testApproveLeave_ValidRequest_ReturnsOk() throws Exception {
        when(leaveService.approveLeave(anyLong(), anyString())).thenReturn(true);
        mockMvc.perform(post("/leave/approve")
                .param("leaveId", "1")
                .param("approverId", "SUP123"))
                .andExpect(status().isOk());
    }

    @Test
    public void testApproveLeave_InvalidLeaveId_ReturnsNotFound() throws Exception {
        when(leaveService.approveLeave(anyLong(), anyString())).thenReturn(false);
        mockMvc.perform(post("/leave/approve")
                .param("leaveId", "999")
                .param("approverId", "SUP123"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDenyLeave_ValidRequest_ReturnsOk() throws Exception {
        when(leaveService.denyLeave(anyLong(), anyString(), anyString())).thenReturn(true);
        mockMvc.perform(post("/leave/deny")
                .param("leaveId", "2")
                .param("approverId", "SUP123")
                .param("reason", "Insufficient balance"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDenyLeave_MissingReason_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/leave/deny")
                .param("leaveId", "2")
                .param("approverId", "SUP123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetLeaveBalance_ValidEmployee_ReturnsOk() throws Exception {
        when(leaveService.getLeaveBalance("EMP123")).thenReturn(10.0);
        mockMvc.perform(get("/leave/balance/EMP123"))
                .andExpect(status().isOk())
                .andExpect(content().string("10.0"));
    }

    @Test
    public void testGetLeaveBalance_InvalidEmployee_ReturnsNotFound() throws Exception {
        when(leaveService.getLeaveBalance("EMP999")).thenThrow(new EmployeeNotFoundException());
        mockMvc.perform(get("/leave/balance/EMP999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAccrualCalculation_BoundaryConditions() {
        when(leaveService.calculateAccrual("EMP123", 0)).thenReturn(0.0);
        assertEquals(0.0, leaveService.calculateAccrual("EMP123", 0));
        when(leaveService.calculateAccrual("EMP123", 365)).thenReturn(20.0);
        assertEquals(20.0, leaveService.calculateAccrual("EMP123", 365));
    }
}
