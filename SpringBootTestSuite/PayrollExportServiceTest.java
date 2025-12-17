package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * PayrollExportServiceTest - Comprehensive unit tests for PayrollExportService covering file generation, mapping, delivery, boundaries, and edge cases.
 */
public class PayrollExportServiceTest {
    private PayrollExportService payrollService;

    @BeforeEach
    public void setUp() {
        payrollService = new PayrollExportService();
    }

    @Test
    public void testGeneratePayrollFileValidPeriod() {
        PayrollPeriod period = new PayrollPeriod("2024-01-01", "2024-01-15");
        assertDoesNotThrow(() -> payrollService.generatePayrollFile(period));
    }

    @Test
    public void testMapToProviderFormatADP() {
        PayrollFile file = new PayrollFile();
        assertTrue(payrollService.mapToProviderFormat(file, "ADP"));
    }

    @Test
    public void testMapToProviderFormatPaychex() {
        PayrollFile file = new PayrollFile();
        assertTrue(payrollService.mapToProviderFormat(file, "Paychex"));
    }

    @Test
    public void testDeliverViaSFTP() {
        PayrollFile file = new PayrollFile();
        assertTrue(payrollService.deliverViaSFTP(file));
    }

    @Test
    public void testDeliverViaAPI() {
        PayrollFile file = new PayrollFile();
        assertTrue(payrollService.deliverViaAPI(file));
    }

    @Test
    public void testReconcilePayrollTotals() {
        PayrollFile file = new PayrollFile();
        assertTrue(payrollService.reconcilePayrollTotals(file));
    }

    @Test
    public void testGetExportHistory() {
        List<PayrollExportHistory> history = payrollService.getExportHistory();
        assertNotNull(history);
    }

    @Test
    public void testRetryFailedExport() {
        int exportId = 1;
        assertTrue(payrollService.retryFailedExport(exportId));
    }

    @Test
    public void testAuditPayrollExport() {
        int exportId = 2;
        assertTrue(payrollService.auditPayrollExport(exportId));
    }

    @Test
    public void testValidatePayrollData() {
        PayrollFile file = new PayrollFile();
        assertTrue(payrollService.validatePayrollData(file));
    }

    @Test
    public void testHandleExportErrors() {
        PayrollFile file = new PayrollFile();
        assertFalse(payrollService.handleExportErrors(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-01-01", "2024-12-31"})
    public void testBoundaryPayPeriods(String dateStr) {
        PayrollPeriod period = new PayrollPeriod(dateStr, dateStr);
        assertDoesNotThrow(() -> payrollService.generatePayrollFile(period));
    }

    @Test
    public void testZeroHours() {
        PayrollFile file = new PayrollFile();
        file.setTotalHours(0);
        assertTrue(payrollService.validatePayrollData(file));
    }

    @Test
    public void testNegativeAdjustments() {
        PayrollFile file = new PayrollFile();
        file.setAdjustment(-100);
        assertFalse(payrollService.validatePayrollData(file));
    }

    @Test
    public void testMissingData() {
        PayrollFile file = null;
        assertThrows(NullPointerException.class, () -> payrollService.validatePayrollData(file));
    }
}
