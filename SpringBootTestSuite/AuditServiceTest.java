package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditLog validAuditLog;

    @BeforeEach
    public void setUp() {
        validAuditLog = new AuditLog();
        validAuditLog.setId(1L);
        validAuditLog.setActor("admin");
        validAuditLog.setTimestamp(LocalDateTime.now());
        validAuditLog.setEntity("Employee");
        validAuditLog.setAction("CREATE");
        validAuditLog.setBefore(null);
        validAuditLog.setAfter("{"name":"John Doe"}");
    }

    @Test
    public void testLogAudit_ValidInput_SavesLog() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(validAuditLog);

        auditService.logAudit("admin", "Employee", "CREATE", null, "{"name":"John Doe"}");

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    public void testLogAudit_NullActor_ThrowsException() {
        assertThrows(Exception.class, () -> auditService.logAudit(null, "Employee", "CREATE", null, "{}"));
    }

    @Test
    public void testGetAuditLogs_ValidDateRange_ReturnsLogs() {
        when(auditLogRepository.findByEntityAndTimestampBetween(eq("Employee"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(validAuditLog));

        List<AuditLog> result = auditService.getAuditLogs("Employee", LocalDateTime.now().minusDays(1), LocalDateTime.now());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getActor());
    }

    @Test
    public void testGetAuditLogs_NoLogs_ReturnsEmptyList() {
        when(auditLogRepository.findByEntityAndTimestampBetween(eq("Employee"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<AuditLog> result = auditService.getAuditLogs("Employee", LocalDateTime.now().minusDays(1), LocalDateTime.now());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}