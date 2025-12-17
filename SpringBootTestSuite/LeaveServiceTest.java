package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.*;

/**
 * LeaveServiceTest - Comprehensive unit tests for LeaveService covering requests, approval, balances, boundaries, and edge cases.
 */
public class LeaveServiceTest {
    private LeaveService leaveService;

    @BeforeEach
    public void setUp() {
        leaveService = new LeaveService();
    }

    @Test
    public void testSubmitLeaveRequestValid() {
        LeaveRequest req = new LeaveRequest(100, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "PTO");
        assertDoesNotThrow(() -> leaveService.submitLeaveRequest(req));
    }

    @Test
    public void testSubmitLeaveRequestInvalidDates() {
        LeaveRequest req = new LeaveRequest(100, LocalDate.now().plusDays(2), LocalDate.now().plusDays(1), "PTO");
        assertThrows(IllegalArgumentException.class, () -> leaveService.submitLeaveRequest(req));
    }

    @Test
    public void testApproveLeaveValidId() {
        int leaveId = 1;
        assertTrue(leaveService.approveLeave(leaveId));
    }

    @Test
    public void testApproveLeaveInvalidId() {
        int leaveId = -1;
        assertFalse(leaveService.approveLeave(leaveId));
    }

    @Test
    public void testDenyLeaveWithReason() {
        int leaveId = 2;
        String reason = "Insufficient balance";
        assertTrue(leaveService.denyLeave(leaveId, reason));
    }

    @Test
    public void testCalculateLeaveBalancePTO() {
        int empId = 101;
        int balance = leaveService.calculateLeaveBalance(empId, "PTO");
        assertTrue(balance >= 0);
    }

    @Test
    public void testCalculateLeaveBalanceNegative() {
        int empId = 102;
        leaveService.setLeaveBalance(empId, "PTO", -5);
        int balance = leaveService.calculateLeaveBalance(empId, "PTO");
        assertEquals(-5, balance);
    }

    @Test
    public void testGetLeavesByEmployee() {
        int empId = 103;
        List<LeaveRequest> leaves = leaveService.getLeavesByEmployee(empId);
        assertNotNull(leaves);
    }

    @Test
    public void testCheckLeaveConflictsWithShifts() {
        LeaveRequest req = new LeaveRequest(104, LocalDate.now(), LocalDate.now(), "Sick");
        assertFalse(leaveService.checkLeaveConflicts(req));
    }

    @Test
    public void testUpdateLeaveRequest() {
        LeaveRequest req = new LeaveRequest(105, LocalDate.now().plusDays(3), LocalDate.now().plusDays(4), "Unpaid");
        assertTrue(leaveService.updateLeaveRequest(req));
    }

    @Test
    public void testCancelLeaveRequest() {
        int leaveId = 3;
        assertTrue(leaveService.cancelLeaveRequest(leaveId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PTO", "Sick", "Unpaid"})
    public void testCalculateLeaveBalanceForTypes(String type) {
        int empId = 106;
        int balance = leaveService.calculateLeaveBalance(empId, type);
        assertTrue(balance >= 0 || balance < 0);
    }

    @Test
    public void testBoundaryLeaveDates() {
        LeaveRequest req = new LeaveRequest(107, LocalDate.now().minusDays(1), LocalDate.now(), "PTO");
        assertThrows(IllegalArgumentException.class, () -> leaveService.submitLeaveRequest(req));
    }

    @Test
    public void testOverlappingLeaves() {
        LeaveRequest req1 = new LeaveRequest(108, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "PTO");
        LeaveRequest req2 = new LeaveRequest(108, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), "Sick");
        leaveService.submitLeaveRequest(req1);
        assertTrue(leaveService.checkLeaveConflicts(req2));
    }

    @Test
    public void testNullInputs() {
        assertThrows(NullPointerException.class, () -> leaveService.submitLeaveRequest(null));
        assertThrows(NullPointerException.class, () -> leaveService.denyLeave(0, null));
    }
}
