package com.company.warehouse.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AuditLogService
 * Covers audit logging operations with various scenarios
 */
@DisplayName("Audit Log Service Tests")
public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ========== BASIC AUDIT LOGGING TESTS ==========

    @Test
    @DisplayName("Test log audit with valid data")
    public void testLogAuditWithValidData() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "CREATE";
        String actor = "admin.user";
        String beforeState = null;
        String afterState = "{"name":"John Doe","badgeId":"EMP001"}";

        AuditLog savedAuditLog = new AuditLog();
        savedAuditLog.setId(1L);
        savedAuditLog.setEntity(entity);
        savedAuditLog.setEntityId(entityId);
        savedAuditLog.setAction(action);
        savedAuditLog.setActor(actor);
        savedAuditLog.setBeforeState(beforeState);
        savedAuditLog.setAfterState(afterState);
        savedAuditLog.setTimestamp(LocalDateTime.now());

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedAuditLog);

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        
        AuditLog capturedLog = auditLogCaptor.getValue();
        assertEquals(entity, capturedLog.getEntity());
        assertEquals(entityId, capturedLog.getEntityId());
        assertEquals(action, capturedLog.getAction());
        assertEquals(actor, capturedLog.getActor());
        assertEquals(beforeState, capturedLog.getBeforeState());
        assertEquals(afterState, capturedLog.getAfterState());
    }

    @Test
    @DisplayName("Test log audit for CREATE action")
    public void testLogAuditForCreateAction() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "CREATE";
        String actor = "hr.user";
        String beforeState = null; // No before state for CREATE
        String afterState = "{"name":"Jane Doe"}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertEquals("CREATE", log.getAction());
        assertNull(log.getBeforeState());
        assertNotNull(log.getAfterState());
    }

    @Test
    @DisplayName("Test log audit for UPDATE action")
    public void testLogAuditForUpdateAction() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "UPDATE";
        String actor = "supervisor.user";
        String beforeState = "{"name":"John Doe","role":"WORKER"}";
        String afterState = "{"name":"John Doe","role":"SUPERVISOR"}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertEquals("UPDATE", log.getAction());
        assertNotNull(log.getBeforeState());
        assertNotNull(log.getAfterState());
    }

    @Test
    @DisplayName("Test log audit for DELETE action")
    public void testLogAuditForDeleteAction() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "DELETE";
        String actor = "admin.user";
        String beforeState = "{"name":"John Doe","status":"ACTIVE"}";
        String afterState = "{"name":"John Doe","status":"INACTIVE"}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertEquals("DELETE", log.getAction());
        assertNotNull(log.getBeforeState());
        assertNotNull(log.getAfterState());
    }

    // ========== NULL AND EMPTY VALUE TESTS ==========

    @Test
    @DisplayName("Test log audit with null entity")
    public void testLogAuditWithNullEntity() {
        // Arrange
        String entity = null;
        Long entityId = 1L;
        String action = "CREATE";
        String actor = "admin.user";
        String beforeState = null;
        String afterState = "{}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertNull(log.getEntity());
    }

    @Test
    @DisplayName("Test log audit with null entity ID")
    public void testLogAuditWithNullEntityId() {
        // Arrange
        String entity = "Employee";
        Long entityId = null;
        String action = "CREATE";
        String actor = "admin.user";
        String beforeState = null;
        String afterState = "{}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertNull(log.getEntityId());
    }

    @Test
    @DisplayName("Test log audit with null action")
    public void testLogAuditWithNullAction() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = null;
        String actor = "admin.user";
        String beforeState = null;
        String afterState = "{}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertNull(log.getAction());
    }

    @Test
    @DisplayName("Test log audit with null actor")
    public void testLogAuditWithNullActor() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "CREATE";
        String actor = null;
        String beforeState = null;
        String afterState = "{}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertNull(log.getActor());
    }

    @Test
    @DisplayName("Test log audit with empty strings")
    public void testLogAuditWithEmptyStrings() {
        // Arrange
        String entity = "";
        Long entityId = 1L;
        String action = "";
        String actor = "";
        String beforeState = "";
        String afterState = "";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertEquals("", log.getEntity());
        assertEquals("", log.getAction());
        assertEquals("", log.getActor());
    }

    // ========== DIFFERENT ENTITY TYPES TESTS ==========

    @Test
    @DisplayName("Test log audit for ClockEvent entity")
    public void testLogAuditForClockEvent() {
        // Arrange
        String entity = "ClockEvent";
        Long entityId = 100L;
        String action = "CREATE";
        String actor = "worker.user";
        String beforeState = null;
        String afterState = "{"type":"CLOCK_IN","timestamp":"2024-01-15T09:00:00"}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertEquals("ClockEvent", log.getEntity());
        assertEquals(100L, log.getEntityId());
    }

    @Test
    @DisplayName("Test log audit for ShiftAssignment entity")
    public void testLogAuditForShiftAssignment() {
        // Arrange
        String entity = "ShiftAssignment";
        Long entityId = 200L;
        String action = "UPDATE";
        String actor = "supervisor.user";
        String beforeState = "{"shiftId":1}";
        String afterState = "{"shiftId":2}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertEquals("ShiftAssignment", log.getEntity());
    }

    // ========== LARGE DATA TESTS ==========

    @Test
    @DisplayName("Test log audit with large before state")
    public void testLogAuditWithLargeBeforeState() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "UPDATE";
        String actor = "admin.user";
        String beforeState = "{".repeat(1000) + "}".repeat(1000); // Large JSON
        String afterState = "{}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertNotNull(log.getBeforeState());
        assertTrue(log.getBeforeState().length() > 1000);
    }

    @Test
    @DisplayName("Test log audit with large after state")
    public void testLogAuditWithLargeAfterState() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "UPDATE";
        String actor = "admin.user";
        String beforeState = "{}";
        String afterState = "{".repeat(1000) + "}".repeat(1000); // Large JSON

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertNotNull(log.getAfterState());
        assertTrue(log.getAfterState().length() > 1000);
    }

    // ========== SPECIAL CHARACTERS TESTS ==========

    @Test
    @DisplayName("Test log audit with special characters in actor")
    public void testLogAuditWithSpecialCharactersInActor() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "CREATE";
        String actor = "user@example.com";
        String beforeState = null;
        String afterState = "{}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertEquals("user@example.com", log.getActor());
    }

    @Test
    @DisplayName("Test log audit with JSON containing special characters")
    public void testLogAuditWithJsonSpecialCharacters() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "CREATE";
        String actor = "admin.user";
        String beforeState = null;
        String afterState = "{"name":"O'Brien","email":"test@example.com"}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertTrue(log.getAfterState().contains("O'Brien"));
    }

    // ========== BOUNDARY VALUE TESTS ==========

    @Test
    @DisplayName("Test log audit with minimum entity ID")
    public void testLogAuditWithMinimumEntityId() {
        // Arrange
        String entity = "Employee";
        Long entityId = 1L;
        String action = "CREATE";
        String actor = "admin.user";
        String beforeState = null;
        String afterState = "{}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertEquals(1L, log.getEntityId());
    }

    @Test
    @DisplayName("Test log audit with maximum entity ID")
    public void testLogAuditWithMaximumEntityId() {
        // Arrange
        String entity = "Employee";
        Long entityId = Long.MAX_VALUE;
        String action = "CREATE";
        String actor = "admin.user";
        String beforeState = null;
        String afterState = "{}";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log(entity, entityId, action, actor, beforeState, afterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog log = captor.getValue();
        assertEquals(Long.MAX_VALUE, log.getEntityId());
    }

    // ========== MULTIPLE CALLS TESTS ==========

    @Test
    @DisplayName("Test multiple audit log calls")
    public void testMultipleAuditLogCalls() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditLogService.log("Employee", 1L, "CREATE", "user1", null, "{}");
        auditLogService.log("Employee", 1L, "UPDATE", "user2", "{}", "{}");
        auditLogService.log("Employee", 1L, "DELETE", "user3", "{}", null);

        // Assert
        verify(auditLogRepository, times(3)).save(any(AuditLog.class));
    }
}