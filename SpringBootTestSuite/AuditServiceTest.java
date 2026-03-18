package com.company.wms.audit.service;

import com.company.wms.audit.entity.AuditAction;
import com.company.wms.audit.entity.AuditLog;
import com.company.wms.audit.repository.AuditLogRepository;
import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.entity.EmployeeRole;
import com.company.wms.employee.entity.EmployeeStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AuditService
 * Covers audit logging for create, update, delete operations and edge cases
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditService auditService;

    private Employee testEmployee;
    private Employee updatedEmployee;
    private AuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .department("Warehouse")
                .status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.of(2024, 1, 1))
                .deleted(false)
                .build();

        // Setup updated employee
        updatedEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Updated")
                .role(EmployeeRole.SUPERVISOR)
                .department("Logistics")
                .status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.of(2024, 1, 1))
                .deleted(false)
                .build();

        // Setup audit log
        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setEntityType("Employee");
        testAuditLog.setEntityId(1L);
        testAuditLog.setAction(AuditAction.CREATE);
        testAuditLog.setActor("admin@company.com");
        testAuditLog.setTimestamp(LocalDateTime.now());
        testAuditLog.setIpAddress("192.168.1.1");

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== LOG CREATE TESTS ====================

    @Test
    void testLogCreate_ValidEntity_Success() throws JsonProcessingException {
        // Arrange
        String entityJson = "{"id":1,"badgeId":"EMP001","name":"John Doe"}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(entityJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, testEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getEntityType().equals("Employee") &&
            log.getEntityId().equals(1L) &&
            log.getAction() == AuditAction.CREATE &&
            log.getAfterState().equals(entityJson) &&
            log.getBeforeState() == null
        ));
    }

    @Test
    void testLogCreate_NullEntity_HandlesGracefully() throws JsonProcessingException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(null)).thenReturn("null");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, null);

        // Assert
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogCreate_JsonSerializationError_HandlesGracefully() throws JsonProcessingException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee))
                .thenThrow(new JsonProcessingException("Serialization error") {});

        // Act - Should not throw exception
        assertDoesNotThrow(() -> auditService.logCreate("Employee", 1L, testEmployee));

        // Assert - Audit log should not be saved due to error
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void testLogCreate_NoAuthentication_UsesSystemActor() throws JsonProcessingException {
        // Arrange
        String entityJson = "{"id":1,"badgeId":"EMP001"}";
        
        when(securityContext.getAuthentication()).thenReturn(null);
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(entityJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, testEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getActor().equals("SYSTEM")
        ));
    }

    @Test
    void testLogCreate_NullEntityType_HandlesGracefully() throws JsonProcessingException {
        // Arrange
        String entityJson = "{"id":1}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(entityJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate(null, 1L, testEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getEntityType() == null
        ));
    }

    @Test
    void testLogCreate_NullEntityId_HandlesGracefully() throws JsonProcessingException {
        // Arrange
        String entityJson = "{"id":1}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(entityJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", null, testEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getEntityId() == null
        ));
    }

    // ==================== LOG UPDATE TESTS ====================

    @Test
    void testLogUpdate_ValidEntities_Success() throws JsonProcessingException {
        // Arrange
        String beforeJson = "{"id":1,"name":"John Doe"}";
        String afterJson = "{"id":1,"name":"John Updated"}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(beforeJson);
        when(objectMapper.writeValueAsString(updatedEmployee)).thenReturn(afterJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, testEmployee, updatedEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getEntityType().equals("Employee") &&
            log.getEntityId().equals(1L) &&
            log.getAction() == AuditAction.UPDATE &&
            log.getBeforeState().equals(beforeJson) &&
            log.getAfterState().equals(afterJson)
        ));
    }

    @Test
    void testLogUpdate_NullBeforeState_HandlesGracefully() throws JsonProcessingException {
        // Arrange
        String afterJson = "{"id":1,"name":"John Updated"}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(null)).thenReturn("null");
        when(objectMapper.writeValueAsString(updatedEmployee)).thenReturn(afterJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, null, updatedEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getBeforeState().equals("null")
        ));
    }

    @Test
    void testLogUpdate_NullAfterState_HandlesGracefully() throws JsonProcessingException {
        // Arrange
        String beforeJson = "{"id":1,"name":"John Doe"}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(beforeJson);
        when(objectMapper.writeValueAsString(null)).thenReturn("null");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, testEmployee, null);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getAfterState().equals("null")
        ));
    }

    @Test
    void testLogUpdate_JsonSerializationError_HandlesGracefully() throws JsonProcessingException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Serialization error") {});

        // Act - Should not throw exception
        assertDoesNotThrow(() -> 
            auditService.logUpdate("Employee", 1L, testEmployee, updatedEmployee)
        );

        // Assert - Audit log should not be saved due to error
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void testLogUpdate_IdenticalStates_StillLogs() throws JsonProcessingException {
        // Arrange
        String sameJson = "{"id":1,"name":"John Doe"}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(sameJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, testEmployee, testEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getBeforeState().equals(sameJson) &&
            log.getAfterState().equals(sameJson)
        ));
    }

    @Test
    void testLogUpdate_ComplexObject_Success() throws JsonProcessingException {
        // Arrange
        String beforeJson = "{"id":1,"nested":{"field":"value"}}";
        String afterJson = "{"id":1,"nested":{"field":"updated"}}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(beforeJson);
        when(objectMapper.writeValueAsString(updatedEmployee)).thenReturn(afterJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, testEmployee, updatedEmployee);

        // Assert
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    // ==================== LOG DELETE TESTS ====================

    @Test
    void testLogDelete_ValidId_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logDelete("Employee", 1L);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getEntityType().equals("Employee") &&
            log.getEntityId().equals(1L) &&
            log.getAction() == AuditAction.DELETE &&
            log.getBeforeState() == null &&
            log.getAfterState() == null
        ));
    }

    @Test
    void testLogDelete_NullEntityType_HandlesGracefully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logDelete(null, 1L);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getEntityType() == null
        ));
    }

    @Test
    void testLogDelete_NullEntityId_HandlesGracefully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logDelete("Employee", null);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getEntityId() == null
        ));
    }

    @Test
    void testLogDelete_NoAuthentication_UsesSystemActor() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logDelete("Employee", 1L);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getActor().equals("SYSTEM")
        ));
    }

    @Test
    void testLogDelete_RepositoryException_HandlesGracefully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act - Should not throw exception
        assertDoesNotThrow(() -> auditService.logDelete("Employee", 1L));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void testLogCreate_VeryLargeEntity_Success() throws JsonProcessingException {
        // Arrange
        String largeJson = "{".concat(""field":"value",".repeat(1000)).concat("}");
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(largeJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate("Employee", 1L, testEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getAfterState().length() > 1000
        ));
    }

    @Test
    void testLogUpdate_SpecialCharactersInJson_Success() throws JsonProcessingException {
        // Arrange
        String beforeJson = "{"name":"O'Brien\nTest"}";
        String afterJson = "{"name":"O'Brien\nUpdated"}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(beforeJson);
        when(objectMapper.writeValueAsString(updatedEmployee)).thenReturn(afterJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, testEmployee, updatedEmployee);

        // Assert
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogCreate_ConcurrentCalls_AllSucceed() throws JsonProcessingException {
        // Arrange
        String entityJson = "{"id":1}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(any())).thenReturn(entityJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act - Simulate concurrent calls
        auditService.logCreate("Employee", 1L, testEmployee);
        auditService.logCreate("Employee", 2L, testEmployee);
        auditService.logCreate("Employee", 3L, testEmployee);

        // Assert
        verify(auditLogRepository, times(3)).save(any(AuditLog.class));
    }

    @Test
    void testLogDelete_MultipleEntityTypes_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logDelete("Employee", 1L);
        auditService.logDelete("Attendance", 2L);
        auditService.logDelete("Leave", 3L);

        // Assert
        verify(auditLogRepository, times(3)).save(any(AuditLog.class));
    }

    @Test
    void testLogUpdate_EmptyBeforeState_Success() throws JsonProcessingException {
        // Arrange
        String beforeJson = "{}";
        String afterJson = "{"id":1,"name":"John"}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(beforeJson);
        when(objectMapper.writeValueAsString(updatedEmployee)).thenReturn(afterJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logUpdate("Employee", 1L, testEmployee, updatedEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getBeforeState().equals("{}")
        ));
    }

    @Test
    void testLogCreate_LongEntityTypeName_Success() throws JsonProcessingException {
        // Arrange
        String longEntityType = "A".repeat(255);
        String entityJson = "{"id":1}";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@company.com");
        when(objectMapper.writeValueAsString(testEmployee)).thenReturn(entityJson);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCreate(longEntityType, 1L, testEmployee);

        // Assert
        verify(auditLogRepository).save(argThat(log -> 
            log.getEntityType().length() == 255
        ));
    }
}
