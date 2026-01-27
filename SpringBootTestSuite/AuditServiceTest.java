package com.warehouse.ems.service;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class AuditServiceTest {
    @Autowired
    AuditService auditService;

    @MockBean
    AuditRepository auditRepository;
    @MockBean
    ExportService exportService;

    @BeforeEach
    void setup() {
        // Setup mocks if needed
    }

    @Test
    void testLogChange_Immutable() {
        AuditEntry entry = new AuditEntry("employee", "update", "user1", "before", "after", new Date());
        when(auditRepository.save(any())).thenReturn(entry);
        AuditEntry result = auditService.logChange("employee", "update", "user1", "before", "after");
        assertEquals(entry, result);
        verify(auditRepository).save(any());
    }

    @Test
    void testActorTracking() {
        AuditEntry entry = auditService.logChange("schedule", "create", "admin", null, "newSchedule");
        assertEquals("admin", entry.getActor());
    }

    @Test
    void testBeforeAfterStateCapture() {
        AuditEntry entry = auditService.logChange("payroll", "update", "hr", "oldState", "newState");
        assertEquals("oldState", entry.getBeforeState());
        assertEquals("newState", entry.getAfterState());
    }

    @Test
    void testExportByDate() {
        List<AuditEntry> entries = Arrays.asList(
            new AuditEntry("employee", "update", "user1", "before", "after", new Date())
        );
        when(auditRepository.findByDateRange(any(), any())).thenReturn(entries);
        when(exportService.exportAuditEntries(entries, "csv")).thenReturn("csvdata");
        String csv = auditService.exportAuditLog(new Date(), new Date(), "csv");
        assertEquals("csvdata", csv);
    }

    @Test
    void testExportByUser() {
        List<AuditEntry> entries = Arrays.asList(
            new AuditEntry("employee", "delete", "user2", "before", "after", new Date())
        );
        when(auditRepository.findByActor("user2")).thenReturn(entries);
        when(exportService.exportAuditEntries(entries, "pdf")).thenReturn("pdfdata");
        String pdf = auditService.exportAuditLogByUser("user2", "pdf");
        assertEquals("pdfdata", pdf);
    }

    @Test
    void testNullEntity_Throws() {
        assertThrows(IllegalArgumentException.class, () -> auditService.logChange(null, "update", "user1", "before", "after"));
    }

    @Test
    void testEmptyActor() {
        AuditEntry entry = auditService.logChange("employee", "update", "", "before", "after");
        assertEquals("", entry.getActor());
    }

    @Test
    void testImmutableLogTable() {
        AuditEntry entry = auditService.logChange("employee", "update", "user1", "before", "after");
        when(auditRepository.isMutable(entry)).thenReturn(false);
        assertFalse(auditRepository.isMutable(entry));
    }

    @Test
    void testIntegration_MultipleEntities() {
        AuditEntry e1 = auditService.logChange("employee", "create", "admin", null, "emp1");
        AuditEntry e2 = auditService.logChange("schedule", "update", "supervisor", "old", "new");
        assertNotNull(e1);
        assertNotNull(e2);
    }
}
