import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class AuditServiceTest {
    @Mock
    private AuditRepository auditRepository;
    @InjectMocks
    private AuditService auditService;
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
    void testLogAuditEntry_ValidInput() {
        AuditEntry entry = new AuditEntry("Employee", "CREATE", "John Doe", new Date(), "actor1", "before", "after");
        when(auditRepository.save(any(AuditEntry.class))).thenReturn(entry);
        AuditEntry result = auditService.logAuditEntry("Employee", "CREATE", "John Doe", "actor1", "before", "after");
        assertEquals("Employee", result.getEntity());
        assertEquals("CREATE", result.getAction());
    }

    @Test
    void testLogAuditEntry_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> auditService.logAuditEntry(null, "CREATE", "John Doe", "actor1", "before", "after"));
    }

    @Test
    void testLogAuditEntry_EmptyEntity() {
        assertThrows(ValidationException.class, () -> auditService.logAuditEntry("", "CREATE", "John Doe", "actor1", "before", "after"));
    }

    @Test
    void testGetAuditEntriesByDate_ValidInput() {
        AuditEntry entry = new AuditEntry("Employee", "CREATE", "John Doe", new Date(), "actor1", "before", "after");
        List<AuditEntry> entries = Arrays.asList(entry);
        when(auditRepository.findByDateRange(any(Date.class), any(Date.class))).thenReturn(entries);
        List<AuditEntry> result = auditService.getAuditEntriesByDate(new Date(), new Date());
        assertEquals(1, result.size());
    }

    @Test
    void testGetAuditEntriesByDate_InvalidDateRange() {
        Date start = new Date();
        Date end = new Date(System.currentTimeMillis() - 86400000);
        assertThrows(ValidationException.class, () -> auditService.getAuditEntriesByDate(start, end));
    }

    @Test
    void testLogAuditEntry_BoundaryValues() {
        AuditEntry minEntry = new AuditEntry("A", "CREATE", "A", new Date(), "actor1", "before", "after");
        AuditEntry maxEntry = new AuditEntry("A very long entity name exceeding normal limits", "UPDATE", "John Doe", new Date(), "actor1", "before", "after");
        when(auditRepository.save(any(AuditEntry.class))).thenReturn(minEntry).thenReturn(maxEntry);
        assertDoesNotThrow(() -> auditService.logAuditEntry("A", "CREATE", "A", "actor1", "before", "after"));
        assertDoesNotThrow(() -> auditService.logAuditEntry("A very long entity name exceeding normal limits", "UPDATE", "John Doe", "actor1", "before", "after"));
    }

    @Test
    void testTamperEvidentStorage_Valid() {
        AuditEntry entry = new AuditEntry("Employee", "CREATE", "John Doe", new Date(), "actor1", "before", "after");
        when(auditRepository.save(any(AuditEntry.class))).thenReturn(entry);
        AuditEntry result = auditService.logAuditEntry("Employee", "CREATE", "John Doe", "actor1", "before", "after");
        assertTrue(result.isTamperEvident());
    }
}
