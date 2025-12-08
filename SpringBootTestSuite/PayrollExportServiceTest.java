import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class PayrollExportServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private LeaveRepository leaveRepository;
    @InjectMocks
    private PayrollExportService payrollExportService;
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
    void testGeneratePayrollExport_ValidInput() {
        List<Attendance> attendances = Arrays.asList(
            new Attendance(null, new Date(), new Date(), "device1", "geo1")
        );
        List<LeaveRequest> leaves = Arrays.asList(
            new LeaveRequest(null, "PTO", new Date(), new Date(), "Approved")
        );
        when(attendanceRepository.findApprovedAttendances(any(Date.class), any(Date.class))).thenReturn(attendances);
        when(leaveRepository.findApprovedLeaves(any(Date.class), any(Date.class))).thenReturn(leaves);
        String csv = payrollExportService.generatePayrollExport(new Date(), new Date());
        assertNotNull(csv);
        assertTrue(csv.contains("PTO"));
    }

    @Test
    void testGeneratePayrollExport_EmptyData() {
        when(attendanceRepository.findApprovedAttendances(any(Date.class), any(Date.class))).thenReturn(Collections.emptyList());
        when(leaveRepository.findApprovedLeaves(any(Date.class), any(Date.class))).thenReturn(Collections.emptyList());
        String csv = payrollExportService.generatePayrollExport(new Date(), new Date());
        assertNotNull(csv);
        assertTrue(csv.isEmpty() || csv.length() < 10);
    }

    @Test
    void testGeneratePayrollExport_InvalidDateRange() {
        Date start = new Date();
        Date end = new Date(System.currentTimeMillis() - 86400000);
        assertThrows(ValidationException.class, () -> payrollExportService.generatePayrollExport(start, end));
    }

    @Test
    void testTransformData_ValidInput() {
        List<Attendance> attendances = Arrays.asList(
            new Attendance(null, new Date(), new Date(), "device1", "geo1")
        );
        String csv = payrollExportService.transformData(attendances);
        assertNotNull(csv);
    }

    @Test
    void testTransformData_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> payrollExportService.transformData(null));
    }

    @Test
    void testGeneratePayrollExport_BoundaryValues() {
        List<Attendance> attendances = Arrays.asList(
            new Attendance(null, new Date(), new Date(), "device1", "geo1")
        );
        when(attendanceRepository.findApprovedAttendances(any(Date.class), any(Date.class))).thenReturn(attendances);
        when(leaveRepository.findApprovedLeaves(any(Date.class), any(Date.class))).thenReturn(Collections.emptyList());
        String csv = payrollExportService.generatePayrollExport(new Date(), new Date());
        assertDoesNotThrow(() -> payrollExportService.generatePayrollExport(new Date(), new Date()));
    }
}
