package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LeaveServiceTest {
    @Mock
    private LeaveRepository leaveRepository;
    @InjectMocks
    private LeaveService leaveService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRequestLeave_validInput() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(2);
        Leave leave = new Leave();
        leave.setEmployeeId(1L);
        leave.setLeaveType("SICK");
        leave.setStartDate(start);
        leave.setEndDate(end);
        leave.setStatus("REQUESTED");
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);
        Leave result = leaveService.requestLeave(1L, "SICK", start, end);
        assertEquals("REQUESTED", result.getStatus());
        assertEquals(start, result.getStartDate());
        assertEquals(end, result.getEndDate());
    }

    @Test
    void testRequestLeave_nullStartDate() {
        LocalDate end = LocalDate.now().plusDays(2);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> leaveService.requestLeave(1L, "SICK", null, end));
        assertEquals("Dates required", ex.getMessage());
    }

    @Test
    void testRequestLeave_nullEndDate() {
        LocalDate start = LocalDate.now();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> leaveService.requestLeave(1L, "SICK", start, null));
        assertEquals("Dates required", ex.getMessage());
    }

    @Test
    void testRequestLeave_endBeforeStart() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().minusDays(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> leaveService.requestLeave(1L, "SICK", start, end));
        assertEquals("End date must be after start date", ex.getMessage());
    }

    @Test
    void testApproveLeave_validInput() {
        Leave leave = new Leave();
        leave.setId(10L);
        leave.setStatus("REQUESTED");
        when(leaveRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any(Leave.class))).thenAnswer(invocation -> {
            Leave l = invocation.getArgument(0);
            return l;
        });
        Leave result = leaveService.approveLeave(10L);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void testApproveLeave_notFound() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.approveLeave(99L));
    }

    @Test
    void testApproveLeave_alreadyProcessed() {
        Leave leave = new Leave();
        leave.setId(10L);
        leave.setStatus("APPROVED");
        when(leaveRepository.findById(10L)).thenReturn(Optional.of(leave));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> leaveService.approveLeave(10L));
        assertEquals("Leave already processed", ex.getMessage());
    }

    // DTO and Exception classes for test compilation
    static class Leave {
        private Long id;
        private Long employeeId;
        private String leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getLeaveType() { return leaveType; }
        public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    interface LeaveRepository {
        Leave save(Leave leave);
        Optional<Leave> findById(Long id);
    }
    static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String msg) { super(msg); }
    }
    static class LeaveService {
        private LeaveRepository leaveRepository;
        public Leave requestLeave(Long employeeId, String leaveType, LocalDate startDate, LocalDate endDate) {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Dates required");
            }
            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date must be after start date");
            }
            Leave leave = new Leave();
            leave.setEmployeeId(employeeId);
            leave.setLeaveType(leaveType);
            leave.setStartDate(startDate);
            leave.setEndDate(endDate);
            leave.setStatus("REQUESTED");
            return leaveRepository.save(leave);
        }
        public Leave approveLeave(Long leaveId) {
            Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
            if (!"REQUESTED".equals(leave.getStatus())) {
                throw new IllegalStateException("Leave already processed");
            }
            leave.setStatus("APPROVED");
            return leaveRepository.save(leave);
        }
    }
}
