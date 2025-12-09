import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class LeaveRequestServiceTest {
    private LeaveRequestService service;

    @BeforeEach
    public void setUp() {
        service = new LeaveRequestService();
    }

    @Test
    public void testCreateLeaveRequest_ValidRequest() {
        LeaveRequest req = new LeaveRequest("emp1", "2024-07-01", "2024-07-05", "Annual");
        assertDoesNotThrow(() -> service.createLeaveRequest(req));
    }

    @Test
    public void testCreateLeaveRequest_InsufficientBalance() {
        LeaveRequest req = new LeaveRequest("emp2", "2024-07-01", "2024-07-10", "Annual");
        assertThrows(InsufficientLeaveBalanceException.class, () -> service.createLeaveRequest(req));
    }

    @Test
    public void testCreateLeaveRequest_OverlappingRequests() {
        LeaveRequest req1 = new LeaveRequest("emp3", "2024-07-01", "2024-07-05", "Annual");
        LeaveRequest req2 = new LeaveRequest("emp3", "2024-07-03", "2024-07-07", "Annual");
        service.createLeaveRequest(req1);
        assertThrows(OverlappingLeaveRequestException.class, () -> service.createLeaveRequest(req2));
    }

    @Test
    public void testCreateLeaveRequest_NullDates() {
        LeaveRequest req = new LeaveRequest("emp4", null, null, "Annual");
        assertThrows(IllegalArgumentException.class, () -> service.createLeaveRequest(req));
    }

    @Test
    public void testApproveLeaveRequest_Valid() {
        LeaveRequest req = new LeaveRequest("emp5", "2024-07-01", "2024-07-02", "Annual");
        service.createLeaveRequest(req);
        assertTrue(service.approveLeaveRequest(req.getId()));
    }

    @Test
    public void testApproveLeaveRequest_NegativeBalance() {
        LeaveRequest req = new LeaveRequest("emp6", "2024-07-01", "2024-07-10", "Annual");
        service.createLeaveRequest(req);
        assertThrows(InsufficientLeaveBalanceException.class, () -> service.approveLeaveRequest(req.getId()));
    }

    @Test
    public void testRejectLeaveRequest_Valid() {
        LeaveRequest req = new LeaveRequest("emp7", "2024-07-01", "2024-07-02", "Annual");
        service.createLeaveRequest(req);
        assertTrue(service.rejectLeaveRequest(req.getId()));
    }

    @Test
    public void testGetLeaveBalance_Valid() {
        assertEquals(20, service.getLeaveBalance("emp8", "Annual"));
    }

    @Test
    public void testCalculateAccrual_Valid() {
        assertEquals(1.67, service.calculateAccrual("emp9", "Annual", 20), 0.01);
    }

    @Test
    public void testCheckAvailability_Valid() {
        assertTrue(service.checkAvailability("emp10", "2024-07-01", "2024-07-05", "Annual"));
    }
}