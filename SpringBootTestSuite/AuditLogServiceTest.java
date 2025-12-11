package com.wms.employee.service;

import com.wms.employee.entity.AuditLog;
import com.wms.employee.repository.AuditLogRepository;
import com.wms.employee.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {}

    @Test
    void testLogCreate_ValidInput_Success() {
        AuditLog log = new AuditLog(null, "admin", null, "Employee", null, "after", "CREATE");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(log);

        AuditLog result = auditLogService.logCreate("Employee", 1L, "after");
        assertEquals("CREATE", result.getAction());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogUpdate_ValidInput_Success() {
        AuditLog log = new AuditLog(null, "admin", null, "Employee", "before", "after", "UPDATE");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(log);

        AuditLog result = auditLogService.logUpdate("Employee", 1L, "before", "after", "admin");
        assertEquals("UPDATE", result.getAction());
    }

    @Test
    void testLogDelete_ValidInput_Success() {
        AuditLog log = new AuditLog(null, "admin", null, "Employee", "before", null, "DELETE");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(log);

        AuditLog result = auditLogService.logDelete("Employee", 1L, "before");
        assertEquals("DELETE", result.getAction());
    }
}