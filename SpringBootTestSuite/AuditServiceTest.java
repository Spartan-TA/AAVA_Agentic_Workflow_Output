package SpringBootTestSuite;

import com.example.warehouse.audit.AuditLog;
import com.example.warehouse.audit.AuditService;
import com.example.warehouse.audit.AuditRepository;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
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
    public void logAuditEvent_ValidInput_ReturnsAuditLog() {
        AuditLog log = new AuditLog();
        log.setEntity("Employee");
        log.setAction("CREATE");
        log.setTimestamp(LocalDateTime.now());
        when(auditRepository.save(any())).thenReturn(log);
        AuditLog result = auditService.logAuditEvent(log);
        assertNotNull(result);
        assertEquals("Employee", result.getEntity());
    }

    @Test
    public void logAuditEvent_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> auditService.logAuditEvent(null));
    }

    @Test
    public void getAuditLogById_ValidId_ReturnsAuditLog() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        when(auditRepository.findById(1L)).thenReturn(Optional.of(log));
        AuditLog result = auditService.getAuditLogById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void getAuditLogById_InvalidId_ThrowsResourceNotFoundException() {
        when(auditRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> auditService.getAuditLogById(99L));
    }

    @Test
    public void getAllAuditLogs_ReturnsList() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        when(auditRepository.findAll()).thenReturn(Collections.singletonList(log));
        List<AuditLog> result = auditService.getAllAuditLogs();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getAllAuditLogs_Empty_ReturnsEmptyList() {
        when(auditRepository.findAll()).thenReturn(Collections.emptyList());
        List<AuditLog> result = auditService.getAllAuditLogs();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
