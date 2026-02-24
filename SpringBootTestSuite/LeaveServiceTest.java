package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    private LeaveRequest validLeaveRequest;
    private LeaveBalance validBalance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validLeaveRequest = new LeaveRequest(1L, 1L, "PTO", LocalDate.now(), LocalDate.now().plusDays(3), "REQUESTED", "Vacation");
        validBalance = new LeaveBalance(1L, 1L, "PTO", 15.0, 5.0);
    }

    @AfterEach
    void tearDown() {
        // Clean up resources if needed
    }

    @Test
    void testRequestLeave_ValidInput() {
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);
        when(leaveRepository.getBalance(anyLong(), anyString())).thenReturn(validBalance);
        LeaveRequest result = leaveService.requestLeave(1L, "PTO", LocalDate.now(), LocalDate.now().plusDays(3), "Vacation");
        assertNotNull(result);
        assertEquals("REQUESTED", result.getStatus());
    }

    @Test
    void testRequestLeave_InsufficientBalance() {
        LeaveBalance insufficientBalance = new LeaveBalance(1L, 1L, "PTO", 15.0, 14.0);
        when(leaveRepository.getBalance(anyLong(), anyString())).thenReturn(insufficientBalance);
        assertThrows(IllegalStateException.class, () -> leaveService.requestLeave(1L, "PTO", LocalDate.now(), LocalDate.now().plusDays(3), "Vacation"));
    }

    @Test
    void testApproveLeave_ValidInput() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(validLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);
        LeaveRequest result = leaveService.approveLeave(1L, 2L);
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void testApproveLeave_InvalidId() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(LeaveRequestNotFoundException.class, () -> leaveService.approveLeave(999L, 2L));
    }

    @Test
    void testDenyLeave_ValidInput() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(validLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(validLeaveRequest);
        LeaveRequest result = leaveService.denyLeave(1L, 2L, "Insufficient coverage");
        assertNotNull(result);
        assertEquals("DENIED", result.getStatus());
    }

    @Test
    void testGetLeaveBalance_ValidInput() {
        when(leaveRepository.getBalance(1L, "PTO")).thenReturn(validBalance);
        LeaveBalance result = leaveService.getLeaveBalance(1L, "PTO");
        assertNotNull(result);
        assertEquals(15.0, result.getAccrued());
        assertEquals(5.0, result.getUsed());
    }

    @Test
    void testGetLeaveBalance_NoBalance() {
        when(leaveRepository.getBalance(999L, "PTO")).thenReturn(null);
        assertThrows(LeaveBalanceNotFoundException.class, () -> leaveService.getLeaveBalance(999L, "PTO"));
    }

    @Test
    void testAccrueLeave_ValidInput() {
        when(leaveRepository.getBalance(1L, "PTO")).thenReturn(validBalance);
        when(leaveRepository.saveBalance(any(LeaveBalance.class))).thenReturn(validBalance);
        LeaveBalance result = leaveService.accrueLeave(1L, "PTO", 1.25);
        assertNotNull(result);
        assertTrue(result.getAccrued() > 15.0);
    }

    @Test
    void testRequestLeave_OverlappingDates() {
        when(leaveRepository.findOverlappingRequests(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(validLeaveRequest));
        assertThrows(IllegalStateException.class, () -> leaveService.requestLeave(1L, "PTO", LocalDate.now(), LocalDate.now().plusDays(3), "Vacation"));
    }

    @Test
    void testRequestLeave_PastDates() {
        assertThrows(IllegalArgumentException.class, () -> leaveService.requestLeave(1L, "PTO", LocalDate.now().minusDays(5), LocalDate.now().minusDays(3), "Vacation"));
    }
}