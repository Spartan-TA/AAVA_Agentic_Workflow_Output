package SpringBootTestSuite;

import com.example.controller.LeaveController;
import com.example.dto.LeaveRequestDTO;
import com.example.entity.LeaveRequest;
import com.example.service.LeaveService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaveControllerTest {

    @Mock
    private LeaveService leaveService;

    @InjectMocks
    private LeaveController leaveController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void requestLeave_ShouldReturnLeaveRequest() {
        LeaveRequestDTO dto = new LeaveRequestDTO(1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation");
        LeaveRequest leave = new LeaveRequest(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation", "PENDING");

        when(leaveService.requestLeave(dto)).thenReturn(leave);

        ResponseEntity<LeaveRequest> response = leaveController.requestLeave(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Vacation", response.getBody().getReason());
    }

    @Test
    void approveLeave_ShouldReturnLeaveRequest() {
        LeaveRequest leave = new LeaveRequest(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation", "APPROVED");
        when(leaveService.approveLeave(1L, "APPROVED")).thenReturn(leave);

        ResponseEntity<LeaveRequest> response = leaveController.approveLeave(1L, "APPROVED");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("APPROVED", response.getBody().getStatus());
    }

    @Test
    void getEmployeeLeaveRequests_ShouldReturnList() {
        List<LeaveRequest> leaves = Arrays.asList(
            new LeaveRequest(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation", "PENDING")
        );
        when(leaveService.getEmployeeLeaveRequests(1L)).thenReturn(leaves);

        ResponseEntity<List<LeaveRequest>> response = leaveController.getEmployeeLeaveRequests(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }
}