@SpringBootTest
public class ReportingServiceTest {
    @MockBean private ReportRepository reportRepository;
    @Autowired private ReportingService reportingService;

    @Test
    void testGenerateAttendanceReport_ValidInput_Success() {
        ReportFilter filter = new ReportFilter();
        Report report = new Report();
        when(reportRepository.generateAttendanceReport(filter)).thenReturn(report);
        Report result = reportingService.generateAttendanceReport(filter);
        assertNotNull(result);
    }

    @Test
    void testGenerateOvertimeReport_ValidInput_Success() {
        ReportFilter filter = new ReportFilter();
        Report report = new Report();
        when(reportRepository.generateOvertimeReport(filter)).thenReturn(report);
        Report result = reportingService.generateOvertimeReport(filter);
        assertNotNull(result);
    }

    @Test
    void testGenerateLeaveReport_ValidInput_Success() {
        ReportFilter filter = new ReportFilter();
        Report report = new Report();
        when(reportRepository.generateLeaveReport(filter)).thenReturn(report);
        Report result = reportingService.generateLeaveReport(filter);
        assertNotNull(result);
    }

    @Test
    void testExportReportToCsv_ValidInput_Success() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(new Report()));
        byte[] csv = reportingService.exportReportToCsv(1L);
        assertNotNull(csv);
    }

    @Test
    void testExportReportToPdf_ValidInput_Success() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(new Report()));
        byte[] pdf = reportingService.exportReportToPdf(1L);
        assertNotNull(pdf);
    }
}