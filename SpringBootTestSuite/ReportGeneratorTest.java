import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReportGeneratorTest {
    private ReportGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new ReportGenerator();
    }

    @Test
    public void testGenerateReport_Valid() {
        ReportCriteria criteria = new ReportCriteria("attendance", "2024-07-01", "2024-07-31");
        assertDoesNotThrow(() -> generator.generateReport(criteria));
    }

    @Test
    public void testGenerateReport_NullCriteria() {
        assertThrows(IllegalArgumentException.class, () -> generator.generateReport(null));
    }

    @Test
    public void testGenerateReport_InvalidType() {
        ReportCriteria criteria = new ReportCriteria("unknown", "2024-07-01", "2024-07-31");
        assertThrows(InvalidReportTypeException.class, () -> generator.generateReport(criteria));
    }

    @Test
    public void testExportToPDF_Valid() {
        Report report = generator.generateReport(new ReportCriteria("attendance", "2024-07-01", "2024-07-31"));
        assertNotNull(generator.exportToPDF(report));
    }

    @Test
    public void testExportToCSV_Valid() {
        Report report = generator.generateReport(new ReportCriteria("attendance", "2024-07-01", "2024-07-31"));
        assertNotNull(generator.exportToCSV(report));
    }

    @Test
    public void testExportToCSV_InvalidFormat() {
        Report report = generator.generateReport(new ReportCriteria("attendance", "2024-07-01", "2024-07-31"));
        assertThrows(InvalidFormatException.class, () -> generator.exportToCSV(report, "xml"));
    }

    @Test
    public void testScheduleReport_Valid() {
        assertDoesNotThrow(() -> generator.scheduleReport("2024-07-31", "attendance"));
    }

    @Test
    public void testScheduleReport_InvalidType() {
        assertThrows(InvalidReportTypeException.class, () -> generator.scheduleReport("2024-07-31", "unknown"));
    }

    @Test
    public void testApplyFilters_Valid() {
        Report report = generator.generateReport(new ReportCriteria("attendance", "2024-07-01", "2024-07-31"));
        assertTrue(generator.applyFilters(report, "department=HR"));
    }

    @Test
    public void testApplyFilters_InvalidFilter() {
        Report report = generator.generateReport(new ReportCriteria("attendance", "2024-07-01", "2024-07-31"));
        assertThrows(InvalidFilterException.class, () -> generator.applyFilters(report, "invalidfilter"));
    }
}