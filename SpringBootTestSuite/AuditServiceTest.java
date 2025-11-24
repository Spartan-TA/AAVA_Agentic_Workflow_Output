package com.warehouse.employee.management.service;

import com.warehouse.employee.management.model.AuditLog;
import com.warehouse.employee.management.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AuditService.
 * Tests cover audit logging functionality with various scenarios.
 * Follows AAA (Arrange-Act-Assert) pattern for clarity.
 */
@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private String testEntityType;
    private Long testEntityId;
    private String testAction;
    private String testActor;
    private String testBeforeState;
    private String testAfterState;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test data
        testEntityType = "EMPLOYEE";
        testEntityId = 1L;
        testAction = "UPDATE";
        testActor = "admin";
        testBeforeState = "{"name":"John Doe","status":"ACTIVE"}";
        testAfterState = "{"name":"John Updated","status":"ACTIVE"}";
    }

    // ========== Tests for logAudit(String, Long, String, String, String, String) ==========

    @Test
    public void testLogAudit_ValidInput_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        // Act
        auditService.logAudit(testEntityType, testEntityId, testAction, testActor, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals(testEntityType, savedLog.getEntityType());
        assertEquals(testEntityId, savedLog.getEntityId());
        assertEquals(testAction, savedLog.getAction());
        assertEquals(testActor, savedLog.getActor());
        assertEquals(testBeforeState, savedLog.getBeforeState());
        assertEquals(testAfterState, savedLog.getAfterState());
        assertNotNull(savedLog.getTimestamp());
    }

    @Test
    public void testLogAudit_CreateAction_NullBeforeState() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit("EMPLOYEE", 1L, "CREATE", "admin", null, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertNull(savedLog.getBeforeState());
        assertEquals(testAfterState, savedLog.getAfterState());
        assertEquals("CREATE", savedLog.getAction());
    }

    @Test
    public void testLogAudit_DeleteAction_NullAfterState() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit("EMPLOYEE", 1L, "DELETE", "admin", testBeforeState, null);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals(testBeforeState, savedLog.getBeforeState());
        assertNull(savedLog.getAfterState());
        assertEquals("DELETE", savedLog.getAction());
    }

    @Test
    public void testLogAudit_NullEntityType_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(null, testEntityId, testAction, testActor, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertNull(savedLog.getEntityType());
    }

    @Test
    public void testLogAudit_NullEntityId_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, null, testAction, testActor, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertNull(savedLog.getEntityId());
    }

    @Test
    public void testLogAudit_EmptyEntityType_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit("", testEntityId, testAction, testActor, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals("", savedLog.getEntityType());
    }

    @Test
    public void testLogAudit_NullAction_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, null, testActor, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertNull(savedLog.getAction());
    }

    @Test
    public void testLogAudit_EmptyAction_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, "", testActor, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals("", savedLog.getAction());
    }

    @Test
    public void testLogAudit_NullActor_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, testAction, null, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertNull(savedLog.getActor());
    }

    @Test
    public void testLogAudit_EmptyActor_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, testAction, "", 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals("", savedLog.getActor());
    }

    @Test
    public void testLogAudit_EmptyBeforeState_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, testAction, testActor, 
                             "", testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals("", savedLog.getBeforeState());
    }

    @Test
    public void testLogAudit_EmptyAfterState_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, testAction, testActor, 
                             testBeforeState, "");

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals("", savedLog.getAfterState());
    }

    @Test
    public void testLogAudit_LargeJsonState_Success() {
        // Arrange
        String largeJson = "{"data":"" + "A".repeat(4000) + ""}";
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, testAction, testActor, 
                             largeJson, largeJson);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals(largeJson, savedLog.getBeforeState());
        assertEquals(largeJson, savedLog.getAfterState());
    }

    @Test
    public void testLogAudit_SpecialCharactersInState_Success() {
        // Arrange
        String specialChars = "{"name":"Test
	","special":"<>&"'"}";
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, testAction, testActor, 
                             specialChars, specialChars);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals(specialChars, savedLog.getBeforeState());
    }

    @Test
    public void testLogAudit_MultipleEntityTypes_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit("EMPLOYEE", 1L, "CREATE", "admin", null, testAfterState);
        auditService.logAudit("ATTENDANCE", 2L, "UPDATE", "supervisor", testBeforeState, testAfterState);
        auditService.logAudit("CERTIFICATION", 3L, "DELETE", "hr", testBeforeState, null);

        // Assert
        verify(auditLogRepository, times(3)).save(any(AuditLog.class));
    }

    @Test
    public void testLogAudit_ZeroEntityId_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, 0L, testAction, testActor, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals(0L, savedLog.getEntityId());
    }

    @Test
    public void testLogAudit_NegativeEntityId_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, -1L, testAction, testActor, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals(-1L, savedLog.getEntityId());
    }

    @Test
    public void testLogAudit_LargeEntityId_Success() {
        // Arrange
        Long largeId = Long.MAX_VALUE;
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, largeId, testAction, testActor, 
                             testBeforeState, testAfterState);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertEquals(largeId, savedLog.getEntityId());
    }

    @Test
    public void testLogAudit_TimestampIsSet_Success() {
        // Arrange
        LocalDateTime beforeCall = LocalDateTime.now();
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, testAction, testActor, 
                             testBeforeState, testAfterState);
        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertNotNull(savedLog.getTimestamp());
        assertTrue(savedLog.getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(savedLog.getTimestamp().isBefore(afterCall.plusSeconds(1)));
    }

    @Test
    public void testLogAudit_AllNullParameters_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(null, null, null, null, null, null);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertNull(savedLog.getEntityType());
        assertNull(savedLog.getEntityId());
        assertNull(savedLog.getAction());
        assertNull(savedLog.getActor());
        assertNull(savedLog.getBeforeState());
        assertNull(savedLog.getAfterState());
        assertNotNull(savedLog.getTimestamp());
    }

    @Test
    public void testLogAudit_DifferentActions_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        auditService.logAudit(testEntityType, testEntityId, "CREATE", testActor, null, testAfterState);
        auditService.logAudit(testEntityType, testEntityId, "UPDATE", testActor, testBeforeState, testAfterState);
        auditService.logAudit(testEntityType, testEntityId, "DELETE", testActor, testBeforeState, null);
        auditService.logAudit(testEntityType, testEntityId, "READ", testActor, testBeforeState, testAfterState);

        verify(auditLogRepository, times(4)).save(any(AuditLog.class));
    }

    @Test
    public void testLogAudit_DifferentActors_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAudit(testEntityType, testEntityId, testAction, "admin", testBeforeState, testAfterState);
        auditService.logAudit(testEntityType, testEntityId, testAction, "hr", testBeforeState, testAfterState);
        auditService.logAudit(testEntityType, testEntityId, testAction, "supervisor", testBeforeState, testAfterState);
        auditService.logAudit(testEntityType, testEntityId, testAction, "worker", testBeforeState, testAfterState);

        // Assert
        verify(auditLogRepository, times(4)).save(any(AuditLog.class));
    }

    @Test
    public void testLogAudit_RepositoryException_PropagatesException() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            auditService.logAudit(testEntityType, testEntityId, testAction, testActor, 
                                 testBeforeState, testAfterState));
    }
}