import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class PayrollFileGeneratorTest {
    private PayrollFileGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new PayrollFileGenerator();
    }

    @Test
    public void testGeneratePayrollFile_Valid() {
        PayrollData data = new PayrollData("emp1", 40, 20.0);
        assertDoesNotThrow(() -> generator.generatePayrollFile(data, "2024-07-01", "2024-07-31"));
    }

    @Test
    public void testGeneratePayrollFile_EmptyDateRange() {
        PayrollData data = new PayrollData("emp2", 40, 20.0);
        assertThrows(IllegalArgumentException.class, () -> generator.generatePayrollFile(data, "", ""));
    }

    @Test
    public void testExportToCSV_Valid() {
        PayrollData data = new PayrollData("emp3", 40, 20.0);
        generator.generatePayrollFile(data, "2024-07-01", "2024-07-31");
        assertNotNull(generator.exportToCSV());
    }

    @Test
    public void testExportToCSV_InvalidFormat() {
        PayrollData data = new PayrollData("emp4", 40, 20.0);
        generator.generatePayrollFile(data, "2024-07-01", "2024-07-31");
        assertThrows(InvalidFormatException.class, () -> generator.exportToCSV("xml"));
    }

    @Test
    public void testValidatePayrollData_NullRecords() {
        assertThrows(IllegalArgumentException.class, () -> generator.validatePayrollData(null));
    }

    @Test
    public void testValidatePayrollData_Valid() {
        PayrollData data = new PayrollData("emp5", 40, 20.0);
        assertTrue(generator.validatePayrollData(data));
    }

    @Test
    public void testScheduleExport_Valid() {
        assertDoesNotThrow(() -> generator.scheduleExport("2024-07-31", "csv"));
    }

    @Test
    public void testScheduleExport_InvalidFormat() {
        assertThrows(InvalidFormatException.class, () -> generator.scheduleExport("2024-07-31", "xml"));
    }

    @Test
    public void testHandleExportError_Valid() {
        Exception ex = new Exception("Export failed");
        assertTrue(generator.handleExportError(ex));
    }

    @Test
    public void testHandleExportError_NullException() {
        assertThrows(IllegalArgumentException.class, () -> generator.handleExportError(null));
    }
}