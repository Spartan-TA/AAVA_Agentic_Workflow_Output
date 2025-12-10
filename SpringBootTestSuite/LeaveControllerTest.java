import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaveController.class)
public class LeaveControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LeaveService leaveService;
    @Autowired
    private ObjectMapper objectMapper;
    private LeaveRequestDto validLeaveRequestDto;
    private LeaveRequest validLeaveRequest;

    @BeforeEach
    void setUp() {
        validLeaveRequestDto = new LeaveRequestDto();
        validLeaveRequestDto.setEmployeeId(1L);
        validLeaveRequestDto.setStartDate(java.time.LocalDate.now().plusDays(1));
        validLeaveRequestDto.setEndDate(java.time.LocalDate.now().plusDays(3));
        validLeaveRequestDto.setReason("Vacation");
        validLeaveRequest = new LeaveRequest();
        validLeaveRequest.setId(1L);
        validLeaveRequest.setStatus(LeaveStatus.PENDING);
    }

    @Test
    void testRequestLeave_ValidRequest() throws Exception {
        when(leaveService.requestLeave(any(LeaveRequestDto.class))).thenReturn(validLeaveRequest);
        mockMvc.perform(post("/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLeaveRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testRequestLeave_InvalidDates() throws Exception {
        LeaveRequestDto invalidDto = new LeaveRequestDto();
        invalidDto.setEmployeeId(1L);
        invalidDto.setStartDate(java.time.LocalDate.now().plusDays(5));
        invalidDto.setEndDate(java.time.LocalDate.now().plusDays(2));
        invalidDto.setReason("Vacation");
        when(leaveService.requestLeave(any(LeaveRequestDto.class))).thenThrow(new javax.validation.ValidationException("Invalid dates"));
        mockMvc.perform(post("/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testApproveLeave_ValidId() throws Exception {
        validLeaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveService.approveLeave(1L)).thenReturn(validLeaveRequest);
        mockMvc.perform(patch("/leave/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void testApproveLeave_NonExistentId() throws Exception {
        when(leaveService.approveLeave(2L)).thenThrow(new ResourceNotFoundException("Not found"));
        mockMvc.perform(patch("/leave/2/approve"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDenyLeave_ValidId() throws Exception {
        validLeaveRequest.setStatus(LeaveStatus.DENIED);
        when(leaveService.denyLeave(1L)).thenReturn(validLeaveRequest);
        mockMvc.perform(patch("/leave/1/deny"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DENIED"));
    }

    @Test
    void testGetBalance_ValidEmployeeId() throws Exception {
        when(leaveService.getBalance(1L)).thenReturn(10);
        mockMvc.perform(get("/leave/balance/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }
}