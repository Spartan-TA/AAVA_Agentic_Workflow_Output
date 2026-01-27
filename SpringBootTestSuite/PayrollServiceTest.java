package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class PayrollServiceTest {
    @Mock
    private PayrollRepository payrollRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private PayrollService payrollService;

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
    void testGeneratePayrollExport_Valid_Success() {
        PayrollExport export = new PayrollExport(1L, 1L, 40.0, "PENDING");
        when(payrollRepository.save(any(PayrollExport.class))).thenReturn(export);
        PayrollExport result = payrollService.generatePayrollExport(1L);
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void testGeneratePayrollExport_InvalidAttendance_ThrowsException() {
        when(attendanceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AttendanceNotFoundException.class, () -> payrollService.generatePayrollExport(99L));
    }

    @Test
    void testReconcilePayrollTotals_Valid_Success() {
        double attendanceTotal = 40.0;
        double leaveTotal = 8.0;
        when(attendanceRepository.getTotalHours(1L)).thenReturn(attendanceTotal);
        when(leaveRepository.getTotalLeaveHours(1L)).thenReturn(leaveTotal);
        boolean result = payrollService.reconcilePayrollTotals(1L);
        assertTrue(result);
    }

    @Test
    void testReconcilePayrollTotals_Mismatch_ReturnsFalse() {
        double attendanceTotal = 40.0;
        double leaveTotal = 10.0;
        when(attendanceRepository.getTotalHours(1L)).thenReturn(attendanceTotal);
        when(leaveRepository.getTotalLeaveHours(1L)).thenReturn(leaveTotal);
        boolean result = payrollService.reconcilePayrollTotals(1L);
        assertFalse(result);
    }

    @Test
    void testExportDeliveryFailed_RetryWithBackoff() {
        PayrollExport export = new PayrollExport(1L, 1L, 40.0, "FAILED");
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(export));
        doNothing().when(payrollService).retryExportWithBackoff(1L);
        payrollService.retryExportWithBackoff(1L);
        verify(payrollService).retryExportWithBackoff(1L);
    }

    @Test
    void testAuditLogForExport_Success() {
        PayrollExport export = new PayrollExport(1L, 1L, 40.0, "PENDING");
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(export));
        AuditLog log = payrollService.getAuditLogForExport(1L);
        assertNotNull(log);
    }
}
