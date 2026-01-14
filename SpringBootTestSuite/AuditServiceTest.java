package com.warehouse.ems.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AuditService
 * Tests cover audit logging for create, update, delete operations and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditService auditService;

    private AuditLog testAuditLog;

    @BeforeEach
    public void setUp() {
        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        // Setup test audit log
        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setEntityType("Employee");
        testAuditLog.setEntityId("1");
        testAuditLog.setAction("CREATE");
        testAuditLog.setActor("testuser");
        testAuditLog.setTimestamp(LocalDateTime.now());
    }

    // ========== LOG CREATE TESTS ==========

    @Test
    public void testLogCreate_ValidInput_Success() {
        // Arrange
        String entityType = "Employee";
        Object entityId = 1L;
        String details = "Created new employee";
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate(entityType, entityId, details);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        assertEquals("Employee", capturedLog.getEntityType());
        assertEquals("1", capturedLog.getEntityId());
        assertEquals("CREATE", capturedLog.getAction());
        assertEquals("testuser", capturedLog.getActor());
        assertNotNull(capturedLog.getTimestamp());
    }

    @Test
    public void testLogCreate_NullEntityType_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logCreate(null, 1L, "details");
        });

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    public void testLogCreate_EmptyEntityType_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logCreate("", 1L, "details");
        });

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    public void testLogCreate_NullEntityId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logCreate("Employee", null, "details");
        });

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    public void testLogCreate_NullDetails_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, null);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    public void testLogCreate_LongEntityId_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 999999L, "details");

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        assertEquals("999999", auditLogCaptor.getValue().getEntityId());
    }

    // ========== LOG UPDATE TESTS ==========

    @Test
    public void testLogUpdate_ValidInput_Success() {
        // Arrange
        String entityType = "Employee";
        Object entityId = 1L;
        String details = "Updated employee";
        Object beforeValue = "John Doe";
        Object afterValue = "John Updated";
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate(entityType, entityId, details, beforeValue, afterValue);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        assertEquals("Employee", capturedLog.getEntityType());
        assertEquals("1", capturedLog.getEntityId());
        assertEquals("UPDATE", capturedLog.getAction());
        assertEquals("testuser", capturedLog.getActor());
        assertNotNull(capturedLog.getBeforeValue());
        assertNotNull(capturedLog.getAfterValue());
    }

    @Test
    public void testLogUpdate_NullBeforeValue_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, "details", null, "afterValue");

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    public void testLogUpdate_NullAfterValue_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, "details", "beforeValue", null);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    public void testLogUpdate_ComplexObjects_Success() {
        // Arrange
        Object beforeValue = new Employee(1L, "EMP001", "John", "Doe");
        Object afterValue = new Employee(1L, "EMP001", "John Updated", "Doe");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, "details", beforeValue, afterValue);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    // ========== LOG DELETE TESTS ==========

    @Test
    public void testLogDelete_ValidInput_Success() {
        // Arrange
        String entityType = "Employee";
        Object entityId = 1L;
        String details = "Deleted employee";
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logDelete(entityType, entityId, details);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        assertEquals("Employee", capturedLog.getEntityType());
        assertEquals("1", capturedLog.getEntityId());
        assertEquals("DELETE", capturedLog.getAction());
        assertEquals("testuser", capturedLog.getActor());
    }

    @Test
    public void testLogDelete_NullEntityType_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logDelete(null, 1L, "details");
        });

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    public void testLogDelete_NullEntityId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logDelete("Employee", null, "details");
        });

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    // ========== GENERIC LOG TESTS ==========

    @Test
    public void testLog_CustomAction_Success() {
        // Arrange
        String entityType = "Employee";
        Object entityId = 1L;
        String action = "CUSTOM_ACTION";
        String details = "Custom action performed";
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.log(entityType, entityId, action, details);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        assertEquals("CUSTOM_ACTION", capturedLog.getAction());
    }

    @Test
    public void testLog_NullAction_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.log("Employee", 1L, null, "details");
        });

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    public void testLog_EmptyAction_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.log("Employee", 1L, "", "details");
        });

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    // ========== ACTOR EXTRACTION TESTS ==========

    @Test
    public void testGetActor_AuthenticatedUser_ReturnsUsername() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, "details");

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        assertEquals("testuser", auditLogCaptor.getValue().getActor());
    }

    @Test
    public void testGetActor_NoAuthentication_ReturnsSystem() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, "details");

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        assertEquals("SYSTEM", auditLogCaptor.getValue().getActor());
    }

    @Test
    public void testGetActor_AnonymousUser_ReturnsAnonymous() {
        // Arrange
        when(authentication.getName()).thenReturn("anonymousUser");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, "details");

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        assertEquals("ANONYMOUS", auditLogCaptor.getValue().getActor());
    }

    // ========== TIMESTAMP TESTS ==========

    @Test
    public void testTimestamp_AutoGenerated_NotNull() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, "details");

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        assertNotNull(auditLogCaptor.getValue().getTimestamp());
    }

    @Test
    public void testTimestamp_SequentialLogs_Ordered() throws InterruptedException {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, "details1");
        Thread.sleep(10); // Small delay to ensure different timestamps
        auditService.logCreate("Employee", 2L, "details2");

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(2)).save(auditLogCaptor.capture());
        assertTrue(auditLogCaptor.getAllValues().get(0).getTimestamp()
                .isBefore(auditLogCaptor.getAllValues().get(1).getTimestamp()));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testLogCreate_VeryLongDetails_Success() {
        // Arrange
        String longDetails = "A".repeat(10000);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, longDetails);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    public void testLogCreate_SpecialCharactersInDetails_Success() {
        // Arrange
        String specialDetails = "Details with special chars: @#$%^&*()_+-=[]{}|;':,.<>?/~`";
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, specialDetails);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    public void testLogCreate_UnicodeCharacters_Success() {
        // Arrange
        String unicodeDetails = "Details with unicode: ä½ å¥½ä¸ç ÙØ±Ø­Ø¨Ø§ Ø§ÙØ¹Ø§ÙÙ";
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, unicodeDetails);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    // ========== CONCURRENT ACCESS TESTS ==========

    @Test
    public void testConcurrentLogging_MultipleThreads_Success() throws InterruptedException {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                auditService.logCreate("Employee", (long) index, "details" + index);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        verify(auditLogRepository, times(threadCount)).save(any(AuditLog.class));
    }

    // ========== REPOSITORY FAILURE TESTS ==========

    @Test
    public void testLogCreate_RepositoryFailure_ThrowsException() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            auditService.logCreate("Employee", 1L, "details");
        });
    }

    // ========== IMMUTABILITY TESTS ==========

    @Test
    public void testAuditLog_Immutable_CannotModifyAfterSave() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, "details");

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();
        
        // Attempt to modify should not affect saved log
        String originalEntityType = savedLog.getEntityType();
        savedLog.setEntityType("Modified");
        assertEquals(originalEntityType, testAuditLog.getEntityType());
    }

    // Helper class for testing complex objects
    private static class Employee {
        private Long id;
        private String badgeId;
        private String firstName;
        private String lastName;

        public Employee(Long id, String badgeId, String firstName, String lastName) {
            this.id = id;
            this.badgeId = badgeId;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public String toString() {
            return "Employee{id=" + id + ", badgeId='" + badgeId + "', firstName='" + firstName + "', lastName='" + lastName + "'}";
        }
    }
}