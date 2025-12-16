import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class PayrollExportServiceTest {
    @Mock
    private PayrollExportRepository payrollExportRepository;

    @InjectMocks
    private PayrollExportService payrollExportService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGeneratePayrollExport_CSVFormat() {
        PayrollExport export = new PayrollExport("CSV", "sftp", "SUCCESS");
        when(payrollExportRepository.save(any())).thenReturn(export);
        PayrollExport result = payrollExportService.generateExport("CSV", "sftp");
        assertEquals("CSV", result.getFormat());
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    public void testGeneratePayrollExport_JSONFormat() {
        PayrollExport export = new PayrollExport("JSON", "api", "SUCCESS");
        when(payrollExportRepository.save(any())).thenReturn(export);
        PayrollExport result = payrollExportService.generateExport("JSON", "api");
        assertEquals("JSON", result.getFormat());
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    public void testGeneratePayrollExport_InvalidFormat_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> payrollExportService.generateExport("XML", "sftp"));
    }

    @Test
    public void testDeliveryMethods_SFTP() {
        PayrollExport export = new PayrollExport("CSV", "sftp", "SUCCESS");
        when(payrollExportRepository.save(any())).thenReturn(export);
        PayrollExport result = payrollExportService.generateExport("CSV", "sftp");
        assertEquals("sftp", result.getDeliveryMethod());
    }

    @Test
    public void testDeliveryMethods_API() {
        PayrollExport export = new PayrollExport("JSON", "api", "SUCCESS");
        when(payrollExportRepository.save(any())).thenReturn(export);
        PayrollExport result = payrollExportService.generateExport("JSON", "api");
        assertEquals("api", result.getDeliveryMethod());
    }

    @Test
    public void testExportHistoryLogging() {
        PayrollExport export1 = new PayrollExport("CSV", "sftp", "SUCCESS");
        PayrollExport export2 = new PayrollExport("JSON", "api", "FAILED");
        when(payrollExportRepository.findAll()).thenReturn(Arrays.asList(export1, export2));
        List<PayrollExport> history = payrollExportService.getExportHistory();
        assertEquals(2, history.size());
        assertEquals("FAILED", history.get(1).getStatus());
    }
}
