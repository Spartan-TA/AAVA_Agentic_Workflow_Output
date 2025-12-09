import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MobileControllerTest {
    private MobileController controller;

    @BeforeEach
    public void setUp() {
        controller = new MobileController();
    }

    @Test
    public void testMobileClockIn_Valid() {
        MobileClockRequest req = new MobileClockRequest("emp1", "2024-07-01T08:00:00Z", "geo1");
        assertDoesNotThrow(() -> controller.mobileClockIn(req));
    }

    @Test
    public void testMobileClockIn_InvalidGeofence() {
        MobileClockRequest req = new MobileClockRequest("emp2", "2024-07-01T08:00:00Z", "invalidGeo");
        assertThrows(InvalidGeofenceException.class, () -> controller.mobileClockIn(req));
    }

    @Test
    public void testMobileClockOut_Valid() {
        MobileClockRequest req = new MobileClockRequest("emp3", "2024-07-01T17:00:00Z", "geo1");
        assertDoesNotThrow(() -> controller.mobileClockOut(req));
    }

    @Test
    public void testGetEmployeeSchedule_Valid() {
        assertNotNull(controller.getEmployeeSchedule("emp4", "2024-07-01"));
    }

    @Test
    public void testGetEmployeeSchedule_NullRequest() {
        assertThrows(IllegalArgumentException.class, () -> controller.getEmployeeSchedule(null, null));
    }

    @Test
    public void testSubmitLeaveRequest_Valid() {
        LeaveRequest req = new LeaveRequest("emp5", "2024-07-10", "2024-07-12", "Annual");
        assertDoesNotThrow(() -> controller.submitLeaveRequest(req));
    }

    @Test
    public void testSubmitLeaveRequest_NullRequest() {
        assertThrows(IllegalArgumentException.class, () -> controller.submitLeaveRequest(null));
    }

    @Test
    public void testViewAnnouncements_Valid() {
        assertNotNull(controller.viewAnnouncements("emp6"));
    }

    @Test
    public void testSyncOfflineData_Valid() {
        OfflineSyncData data = new OfflineSyncData("emp7", "clockIn", "2024-07-01T08:00:00Z");
        assertDoesNotThrow(() -> controller.syncOfflineData(data));
    }

    @Test
    public void testSyncOfflineData_NullData() {
        assertThrows(IllegalArgumentException.class, () -> controller.syncOfflineData(null));
    }
}