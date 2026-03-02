package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class AuditServiceImplTest {

    @Mock
    private AuditRepository auditRepository;
    @InjectMocks
    private AuditServiceImpl auditService;

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
    @DisplayName("logAudit - valid input - audit logged")
    void testLogAudit_ValidInput_AuditLogged() {
        AuditEntry entry = new AuditEntry(null, "Employee", 1L, "UPDATE", "Before", "After", "user1");
        when(auditRepository.save(any())).thenAnswer(i -> {
            AuditEntry e = i.getArgument(0);
            e.setId(1L);
            return e;
        });
        AuditEntry result = auditService.logAudit(entry);
        assertNotNull(result.getId());
        assertEquals("Employee", result.getEntityType());
    }

    @Test
    @DisplayName("getAuditLogs - returns list")
    void testGetAuditLogs_ReturnsList() {
        List<AuditEntry> logs = Arrays.asList(new AuditEntry(1L, "Employee", 1L, "UPDATE", "Before", "After", "user1"));
        when(auditRepository.findAll()).thenReturn(logs);
        List<AuditEntry> result = auditService.getAuditLogs();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("exportAuditLogs - returns bytes")
    void testExportAuditLogs_ReturnsBytes() {
        List<AuditEntry> logs = Arrays.asList(new AuditEntry(1L, "Employee", 1L, "UPDATE", "Before", "After", "user1"));
        when(auditRepository.findAll()).thenReturn(logs);
        byte[] export = auditService.exportAuditLogs();
        assertNotNull(export);
        assertTrue(export.length > 0);
    }

    @Test
    @DisplayName("getAuditsByEntity - returns list")
    void testGetAuditsByEntity_ReturnsList() {
        List<AuditEntry> logs = Arrays.asList(new AuditEntry(1L, "Employee", 1L, "UPDATE", "Before", "After", "user1"));
        when(auditRepository.findByEntityTypeAndEntityId("Employee", 1L)).thenReturn(logs);
        List<AuditEntry> result = auditService.getAuditsByEntity("Employee", 1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("logAudit - null entry - throws exception")
    void testLogAudit_NullEntry_ThrowsException() {
        assertThrows(InvalidAuditEntryException.class, () -> auditService.logAudit(null));
    }

    @Test
    @DisplayName("getAuditLogs - no logs - returns empty list")
    void testGetAuditLogs_NoLogs_ReturnsEmptyList() {
        when(auditRepository.findAll()).thenReturn(Collections.emptyList());
        List<AuditEntry> result = auditService.getAuditLogs();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAuditsByEntity - no logs - returns empty list")
    void testGetAuditsByEntity_NoLogs_ReturnsEmptyList() {
        when(auditRepository.findByEntityTypeAndEntityId("Asset", 2L)).thenReturn(Collections.emptyList());
        List<AuditEntry> result = auditService.getAuditsByEntity("Asset", 2L);
        assertTrue(result.isEmpty());
    }
}