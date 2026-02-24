package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.*;

class PayrollExportServiceTest {

    @Mock
    private PayrollRepository payrollRepository;
    @Mock
    private SftpClient sftpClient;
    @InjectMocks
    private PayrollExportService payrollExportService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testGenerateExport_Valid() {
        List<PayrollData> data = Arrays.asList(new PayrollData());
        when(payrollRepository.findByPeriod(any(), any())).thenReturn(data);
        List<PayrollData> result = payrollExportService.generateExport(LocalDate.now(), LocalDate.now().plusDays(7));
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGenerateExport_EmptyPeriod() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            payrollExportService.generateExport(null, null));
        assertEquals("Period cannot be empty", ex.getMessage());
    }

    @Test
    void testGenerateExport_InvalidDateRange() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            payrollExportService.generateExport(LocalDate.now().plusDays(1), LocalDate.now()));
        assertEquals("Start date must be before end date", ex.getMessage());
    }

    @Test
    void testMapToProviderFormat_UnsupportedProvider() {
        Exception ex = assertThrows(UnsupportedOperationException.class, () ->
            payrollExportService.mapToProviderFormat(new ArrayList<>(), "UNKNOWN"));
        assertEquals("Provider format not supported", ex.getMessage());
    }

    @Test
    void testDeliverViaSftp_ConnectionFailure() {
        doThrow(new RuntimeException("SFTP connection failed")).when(sftpClient).upload(any(), any());
        Exception ex = assertThrows(RuntimeException.class, () ->
            payrollExportService.deliverViaSftp("file.csv", new SftpConfig()));
        assertEquals("SFTP connection failed", ex.getMessage());
    }

    @Test
    void testReconcileData_Mismatch() {
        when(payrollRepository.findExportById(1L)).thenReturn(Optional.of(new PayrollExport(1L, false)));
        boolean result = payrollExportService.reconcileData(1L);
        assertFalse(result);
    }
}