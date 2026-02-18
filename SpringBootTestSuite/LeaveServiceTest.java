package SpringBootTestSuite;

import com.example.dto.LeaveRequestDTO;
import com.example.entity.LeaveRequest;
import com.example.repository.LeaveRepository;
import com.example.service.LeaveService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void requestLeave_ShouldSaveLeaveRequest() {
        LeaveRequestDTO dto = new LeaveRequestDTO(1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation");
        LeaveRequest leave = new LeaveRequest(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation", "PENDING");

        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(leave);

        LeaveRequest result = leaveService.requestLeave(dto);

        assertNotNull(result);
        assertEquals("Vacation", result.getReason());
    }

    @Test
    void approveLeave_ShouldUpdateStatus() {
        LeaveRequest leave = new LeaveRequest(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation", "PENDING");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(leave);

        LeaveRequest result = leaveService.approveLeave(1L, "APPROVED");

        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void approveLeave_ShouldThrowException_WhenNotFound() {
        when(leaveRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> leaveService.approveLeave(2L, "APPROVED"));
    }

    @Test
    void getEmployeeLeaveRequests_ShouldReturnList() {
        List<LeaveRequest> leaves = Arrays.asList(
            new LeaveRequest(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation", "PENDING")
        );
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(leaves);

        List<LeaveRequest> result = leaveService.getEmployeeLeaveRequests(1L);

        assertEquals(1, result.size());
    }
}