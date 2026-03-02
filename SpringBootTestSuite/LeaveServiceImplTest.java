package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class LeaveServiceImplTest {

    @Mock
    private LeaveRepository leaveRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private LeaveServiceImpl leaveService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("requestLeave - valid request - leave created")
    void testRequestLeave_ValidRequest_LeaveCreated() {
        LeaveRequest req = new LeaveRequest(1L, "2024-06-01", "2024-06-05", "PTO");
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(leaveRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Leave leave = leaveService.requestLeave(req);
        assertNotNull(leave);
        assertEquals("PTO", leave.getType());
    }

    @Test
    @DisplayName("requestLeave - invalid employee - throws exception")
    void testRequestLeave_InvalidEmployee_ThrowsException() {
        LeaveRequest req = new LeaveRequest(99L, "2024-06-01", "2024-06-05", "PTO");
        when(employeeRepository.existsById(99L)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> leaveService.requestLeave(req));
    }

    @Test
    @DisplayName("approveLeave - valid leave - status updated")
    void testApproveLeave_ValidLeave_StatusUpdated() {
        Leave leave = new Leave(1L, 1L, "PTO", "PENDING");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        leaveService.approveLeave(1L);
        assertEquals("APPROVED", leave.getStatus());
    }

    @Test
    @DisplayName("approveLeave - leave not found - throws exception")
    void testApproveLeave_LeaveNotFound_ThrowsException() {
        when(leaveRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(LeaveNotFoundException.class, () -> leaveService.approveLeave(2L));
    }

    @Test
    @DisplayName("rejectLeave - valid leave - status updated")
    void testRejectLeave_ValidLeave_StatusUpdated() {
        Leave leave = new Leave(2L, 1L, "SICK", "PENDING");
        when(leaveRepository.findById(2L)).thenReturn(Optional.of(leave));
        leaveService.rejectLeave(2L, "Insufficient balance");
        assertEquals("REJECTED", leave.getStatus());
        assertEquals("Insufficient balance", leave.getRejectionReason());
    }

    @Test
    @DisplayName("getLeavesByEmployee - returns leave list")
    void testGetLeavesByEmployee_ReturnsLeaveList() {
        List<Leave> leaves = Arrays.asList(new Leave(1L, 1L, "PTO", "APPROVED"));
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(leaves);
        List<Leave> result = leaveService.getLeavesByEmployee(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getLeaveBalance - valid employee - returns balance")
    void testGetLeaveBalance_ValidEmployee_ReturnsBalance() {
        when(leaveRepository.getLeaveBalance(1L, "PTO")).thenReturn(10);
        int balance = leaveService.getLeaveBalance(1L, "PTO");
        assertEquals(10, balance);
    }

    @Test
    @DisplayName("getLeaveBalance - negative balance - returns zero")
    void testGetLeaveBalance_NegativeBalance_ReturnsZero() {
        when(leaveRepository.getLeaveBalance(1L, "PTO")).thenReturn(-5);
        int balance = leaveService.getLeaveBalance(1L, "PTO");
        assertEquals(0, balance);
    }

    @Test
    @DisplayName("calculateAccrual - valid accrual - returns correct value")
    void testCalculateAccrual_ValidAccrual_ReturnsCorrectValue() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(new Employee(1L, "John")));
        double accrual = leaveService.calculateAccrual(1L, "PTO", 12);
        assertTrue(accrual >= 0);
    }

    @Test
    @DisplayName("calculateAccrual - employee not found - throws exception")
    void testCalculateAccrual_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> leaveService.calculateAccrual(2L, "PTO", 12));
    }

    @Test
    @DisplayName("requestLeave - overlapping leave - throws exception")
    void testRequestLeave_OverlappingLeave_ThrowsException() {
        LeaveRequest req = new LeaveRequest(1L, "2024-06-01", "2024-06-05", "PTO");
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(leaveRepository.existsOverlappingLeave(1L, "2024-06-01", "2024-06-05")).thenReturn(true);
        assertThrows(LeaveOverlapException.class, () -> leaveService.requestLeave(req));
    }

    @Test
    @DisplayName("approveLeave - already approved - throws exception")
    void testApproveLeave_AlreadyApproved_ThrowsException() {
        Leave leave = new Leave(1L, 1L, "PTO", "APPROVED");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        assertThrows(InvalidLeaveStatusException.class, () -> leaveService.approveLeave(1L));
    }
}