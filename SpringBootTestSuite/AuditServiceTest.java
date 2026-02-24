package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.*;

class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;
    @InjectMocks
    private AuditService auditService;

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
    void testLogChange_Valid() {
        AuditLog log = new AuditLog("User", 1L, "admin", "UPDATE", "old", "new");
        when(auditRepository.save(any(AuditLog.class))).thenReturn(log);
        AuditLog result = auditService.logChange("User", 1L, "admin", "UPDATE", "old", "new");
        assertNotNull(result);
        assertEquals("UPDATE", result.getAction());
    }

    @Test
    void testLogChange_NullActor() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            auditService.logChange("User", 1L, null, "UPDATE", "old", "new"));
        assertEquals("Actor cannot be null", ex.getMessage());
    }

    @Test
    void testLogChange_EmptyBeforeAfter() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            auditService.logChange("User", 1L, "admin", "UPDATE", "", ""));
        assertEquals("Before/After values cannot be empty", ex.getMessage());
    }

    @Test
    void testQueryAuditLogs_NoResults() {
        when(auditRepository.query(any())).thenReturn(Collections.emptyList());
        List<AuditLog> result = auditService.queryAuditLogs(new HashMap<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void testExportAuditTrail_InvalidDateRange() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            auditService.exportAuditTrail(LocalDate.now().plusDays(1), LocalDate.now(), "CSV"));
        assertEquals("Invalid date range", ex.getMessage());
    }

    @Test
    void testExportAuditTrail_TamperDetection() {
        when(auditRepository.findByDateRange(any(), any())).thenReturn(Arrays.asList(new AuditLog("User", 1L, "admin", "UPDATE", "old", "tampered")));
        Exception ex = assertThrows(SecurityException.class, () ->
            auditService.exportAuditTrail(LocalDate.now(), LocalDate.now().plusDays(1), "CSV"));
        assertEquals("Audit trail tampering detected", ex.getMessage());
    }
}