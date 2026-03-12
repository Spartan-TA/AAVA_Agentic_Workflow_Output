package com.wms.audit.service;

import com.wms.audit.domain.AuditLog;
import com.wms.audit.dto.AuditLogDto;
import com.wms.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AuditService
 * Tests cover audit logging, compliance tracking, data integrity, and edge cases
 */
@DisplayName("Audit Service Tests")
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLog testAuditLog;
    private AuditLogDto auditLogDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test audit log
        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setEntity("Employee");
        testAuditLog.setEntityId(1L);
        testAuditLog.setAction("UPDATE");
        testAuditLog.setActor("admin@example.com");
        testAuditLog.setTimestamp(LocalDateTime.now());
        testAuditLog.setBefore("{"name":"John Doe","status":"ACTIVE"}");
        testAuditLog.setAfter("{"name":"John Doe","status":"INACTIVE"}");

        // Setup DTO
        auditLogDto = new AuditLogDto();
        auditLogDto.setEntity("Employee");
        auditLogDto.setEntityId(1L);
        auditLogDto.setAction("UPDATE");
        auditLogDto.setActor("admin@example.com");
        auditLogDto.setBefore("{"name":"John Doe","status":"ACTIVE"}");
        auditLogDto.setAfter("{"name":"John Doe","status":"INACTIVE"}");
    }

    // ========== LOG CHANGE TESTS ==========

    @Test
    @DisplayName("Test log change with valid data")
    public void testLogChange_ValidData_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogDto result = auditService.logChange(auditLogDto);

        // Assert
        assertNotNull(result);
        assertEquals("Employee", result.getEntity());
        assertEquals("UPDATE", result.getAction());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Test log change with null entity throws exception")
    public void testLogChange_NullEntity_ThrowsException() {
        // Arrange
        auditLogDto.setEntity(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logChange(auditLogDto);
        });
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Test log change with empty entity throws exception")
    public void testLogChange_EmptyEntity_ThrowsException() {
        // Arrange
        auditLogDto.setEntity("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logChange(auditLogDto);
        });
    }

    @Test
    @DisplayName("Test log change with null entity ID throws exception")
    public void testLogChange_NullEntityId_ThrowsException() {
        // Arrange
        auditLogDto.setEntityId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logChange(auditLogDto);
        });
    }

    @Test
    @DisplayName("Test log change with null action throws exception")
    public void testLogChange_NullAction_ThrowsException() {
        // Arrange
        auditLogDto.setAction(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logChange(auditLogDto);
        });
    }

    @Test
    @DisplayName("Test log change with empty action throws exception")
    public void testLogChange_EmptyAction_ThrowsException() {
        // Arrange
        auditLogDto.setAction("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logChange(auditLogDto);
        });
    }

    @Test
    @DisplayName("Test log change with null actor throws exception")
    public void testLogChange_NullActor_ThrowsException() {
        // Arrange
        auditLogDto.setActor(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logChange(auditLogDto);
        });
    }

    @Test
    @DisplayName("Test log change with empty actor throws exception")
    public void testLogChange_EmptyActor_ThrowsException() {
        // Arrange
        auditLogDto.setActor("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logChange(auditLogDto);
        });
    }

    @Test
    @DisplayName("Test log change with null before state")
    public void testLogChange_NullBefore_Success() {
        // Arrange
        auditLogDto.setBefore(null);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogDto result = auditService.logChange(auditLogDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test log change with null after state")
    public void testLogChange_NullAfter_Success() {
        // Arrange
        auditLogDto.setAfter(null);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogDto result = auditService.logChange(auditLogDto);

        // Assert
        assertNotNull(result);
    }

    // ========== GET AUDIT LOGS TESTS ==========

    @Test
    @DisplayName("Test get audit logs by entity")
    public void testGetAuditLogsByEntity_Success() {
        // Arrange
        when(auditLogRepository.findByEntity("Employee"))
                .thenReturn(Arrays.asList(testAuditLog));

        // Act
        List<AuditLogDto> result = auditService.getAuditLogsByEntity("Employee");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get audit logs by entity with null entity throws exception")
    public void testGetAuditLogsByEntity_NullEntity_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogsByEntity(null);
        });
    }

    @Test
    @DisplayName("Test get audit logs by entity and entity ID")
    public void testGetAuditLogsByEntityAndId_Success() {
        // Arrange
        when(auditLogRepository.findByEntityAndEntityId("Employee", 1L))
                .thenReturn(Arrays.asList(testAuditLog));

        // Act
        List<AuditLogDto> result = auditService.getAuditLogsByEntityAndId("Employee", 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get audit logs by entity and ID with null entity throws exception")
    public void testGetAuditLogsByEntityAndId_NullEntity_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogsByEntityAndId(null, 1L);
        });
    }

    @Test
    @DisplayName("Test get audit logs by entity and ID with null ID throws exception")
    public void testGetAuditLogsByEntityAndId_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogsByEntityAndId("Employee", null);
        });
    }

    @Test
    @DisplayName("Test get audit logs by actor")
    public void testGetAuditLogsByActor_Success() {
        // Arrange
        when(auditLogRepository.findByActor("admin@example.com"))
                .thenReturn(Arrays.asList(testAuditLog));

        // Act
        List<AuditLogDto> result = auditService.getAuditLogsByActor("admin@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get audit logs by actor with null actor throws exception")
    public void testGetAuditLogsByActor_NullActor_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogsByActor(null);
        });
    }

    @Test
    @DisplayName("Test get audit logs by date range")
    public void testGetAuditLogsByDateRange_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(auditLogRepository.findByTimestampBetween(startDate, endDate))
                .thenReturn(Arrays.asList(testAuditLog));

        // Act
        List<AuditLogDto> result = auditService.getAuditLogsByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get audit logs by date range with null start date throws exception")
    public void testGetAuditLogsByDateRange_NullStartDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogsByDateRange(null, LocalDateTime.now());
        });
    }

    @Test
    @DisplayName("Test get audit logs by date range with null end date throws exception")
    public void testGetAuditLogsByDateRange_NullEndDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogsByDateRange(LocalDateTime.now(), null);
        });
    }

    @Test
    @DisplayName("Test get audit logs by date range with end before start throws exception")
    public void testGetAuditLogsByDateRange_EndBeforeStart_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogsByDateRange(LocalDateTime.now(), LocalDateTime.now().minusDays(1));
        });
    }

    @Test
    @DisplayName("Test get audit logs by action")
    public void testGetAuditLogsByAction_Success() {
        // Arrange
        when(auditLogRepository.findByAction("UPDATE"))
                .thenReturn(Arrays.asList(testAuditLog));

        // Act
        List<AuditLogDto> result = auditService.getAuditLogsByAction("UPDATE");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get audit logs by action with null action throws exception")
    public void testGetAuditLogsByAction_NullAction_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogsByAction(null);
        });
    }

    // ========== EXPORT AUDIT LOGS TESTS ==========

    @Test
    @DisplayName("Test export audit logs for date range")
    public void testExportAuditLogs_ValidDateRange_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(auditLogRepository.findByTimestampBetween(startDate, endDate))
                .thenReturn(Arrays.asList(testAuditLog));

        // Act
        byte[] result = auditService.exportAuditLogs(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Test export audit logs with null start date throws exception")
    public void testExportAuditLogs_NullStartDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.exportAuditLogs(null, LocalDateTime.now());
        });
    }

    @Test
    @DisplayName("Test export audit logs with null end date throws exception")
    public void testExportAuditLogs_NullEndDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.exportAuditLogs(LocalDateTime.now(), null);
        });
    }

    // ========== VALIDATE COVERAGE TESTS ==========

    @Test
    @DisplayName("Test validate audit coverage for entity")
    public void testValidateAuditCoverage_Success() {
        // Arrange
        when(auditLogRepository.findByEntity("Employee"))
                .thenReturn(Arrays.asList(testAuditLog));

        // Act
        boolean result = auditService.validateAuditCoverage("Employee");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Test validate audit coverage with null entity throws exception")
    public void testValidateAuditCoverage_NullEntity_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.validateAuditCoverage(null);
        });
    }

    @Test
    @DisplayName("Test validate audit coverage with no logs returns false")
    public void testValidateAuditCoverage_NoLogs_ReturnsFalse() {
        // Arrange
        when(auditLogRepository.findByEntity("Employee"))
                .thenReturn(Arrays.asList());

        // Act
        boolean result = auditService.validateAuditCoverage("Employee");

        // Assert
        assertFalse(result);
    }

    // ========== GET AUDIT LOG BY ID TESTS ==========

    @Test
    @DisplayName("Test get audit log by valid ID")
    public void testGetAuditLogById_ValidId_Success() {
        // Arrange
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(testAuditLog));

        // Act
        AuditLogDto result = auditService.getAuditLogById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Employee", result.getEntity());
    }

    @Test
    @DisplayName("Test get audit log by null ID throws exception")
    public void testGetAuditLogById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogById(null);
        });
    }

    @Test
    @DisplayName("Test get audit log by non-existent ID throws exception")
    public void testGetAuditLogById_NonExistentId_ThrowsException() {
        // Arrange
        when(auditLogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.getAuditLogById(999L);
        });
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test log change with all action types")
    public void testLogChange_AllActionTypes_Success() {
        // Test each action type
        String[] actions = {"CREATE", "UPDATE", "DELETE", "READ"};
        
        for (String action : actions) {
            // Arrange
            auditLogDto.setAction(action);
            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

            // Act
            AuditLogDto result = auditService.logChange(auditLogDto);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Test log change with very long before state")
    public void testLogChange_LongBeforeState_Success() {
        // Arrange
        String longState = "{"data":"" + "A".repeat(10000) + ""}";
        auditLogDto.setBefore(longState);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogDto result = auditService.logChange(auditLogDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test log change with very long after state")
    public void testLogChange_LongAfterState_Success() {
        // Arrange
        String longState = "{"data":"" + "A".repeat(10000) + ""}";
        auditLogDto.setAfter(longState);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogDto result = auditService.logChange(auditLogDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get audit logs by date range for full year")
    public void testGetAuditLogsByDateRange_FullYear_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = LocalDateTime.now();
        when(auditLogRepository.findByTimestampBetween(startDate, endDate))
                .thenReturn(Arrays.asList(testAuditLog));

        // Act
        List<AuditLogDto> result = auditService.getAuditLogsByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get audit logs returns empty list when no logs")
    public void testGetAuditLogsByEntity_NoLogs_ReturnsEmptyList() {
        // Arrange
        when(auditLogRepository.findByEntity("Employee"))
                .thenReturn(Arrays.asList());

        // Act
        List<AuditLogDto> result = auditService.getAuditLogsByEntity("Employee");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Test log change with special characters in entity name")
    public void testLogChange_SpecialCharactersInEntity_Success() {
        // Arrange
        auditLogDto.setEntity("Employee-Record");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogDto result = auditService.logChange(auditLogDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test log change with email actor")
    public void testLogChange_EmailActor_Success() {
        // Arrange
        auditLogDto.setActor("user@example.com");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogDto result = auditService.logChange(auditLogDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test log change with system actor")
    public void testLogChange_SystemActor_Success() {
        // Arrange
        auditLogDto.setActor("SYSTEM");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogDto result = auditService.logChange(auditLogDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get audit logs by date range with same start and end date")
    public void testGetAuditLogsByDateRange_SameDate_Success() {
        // Arrange
        LocalDateTime date = LocalDateTime.now();
        when(auditLogRepository.findByTimestampBetween(date, date))
                .thenReturn(Arrays.asList(testAuditLog));

        // Act
        List<AuditLogDto> result = auditService.getAuditLogsByDateRange(date, date);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test export audit logs with large dataset")
    public void testExportAuditLogs_LargeDataset_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> largeLogs = Arrays.asList(
            testAuditLog, testAuditLog, testAuditLog, testAuditLog, testAuditLog,
            testAuditLog, testAuditLog, testAuditLog, testAuditLog, testAuditLog
        );
        when(auditLogRepository.findByTimestampBetween(startDate, endDate))
                .thenReturn(largeLogs);

        // Act
        byte[] result = auditService.exportAuditLogs(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Test log change with negative entity ID")
    public void testLogChange_NegativeEntityId_ThrowsException() {
        // Arrange
        auditLogDto.setEntityId(-1L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logChange(auditLogDto);
        });
    }

    @Test
    @DisplayName("Test log change with zero entity ID")
    public void testLogChange_ZeroEntityId_ThrowsException() {
        // Arrange
        auditLogDto.setEntityId(0L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.logChange(auditLogDto);
        });
    }
}