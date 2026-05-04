package com.warehouse.management.audit;

import com.warehouse.management.audit.AuditService;
import com.warehouse.management.audit.AuditLog;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditLog = new AuditLog(1L, "CREATE", "Employee", "John Doe", "ADMIN", new java.util.Date(), "before", "after");
    }

    @Test
    void testLogAuditEntry_Valid() {
        when(auditRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        AuditLog result = auditService.logAuditEntry("CREATE", "Employee", "John Doe", "ADMIN", "before", "after");
        assertNotNull(result);
        assertEquals("CREATE", result.getAction());
    }

    @Test
    void testExportAuditLog_Valid() {
        when(auditRepository.findByDateRange(any(), any())).thenReturn(java.util.Arrays.asList(auditLog));
        String export = auditService.exportAuditLog(new java.util.Date(), new java.util.Date());
        assertNotNull(export);
        assertTrue(export.contains("CREATE"));
    }
}