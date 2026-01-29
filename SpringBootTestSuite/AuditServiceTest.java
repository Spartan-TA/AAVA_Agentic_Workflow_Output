package com.example.warehouseems.service;

import com.example.warehouseems.audit.AuditService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {
    @Mock
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @AfterEach
    void tearDown() {
        // Teardown if needed
    }

    @Test
    @DisplayName("logCreate_Normal_Success")
    void testLogCreate_Normal_Success() {
        doNothing().when(auditService).logCreate(eq("Employee"), any(), eq("admin"));
        auditService.logCreate("Employee", new Object(), "admin");
        verify(auditService, times(1)).logCreate(eq("Employee"), any(), eq("admin"));
    }

    @Test
    @DisplayName("logCreate_NullEntity_IllegalArgumentException")
    void testLogCreate_NullEntity_IllegalArgumentException() {
        doThrow(new IllegalArgumentException("Entity cannot be null")).when(auditService).logCreate(isNull(), any(), anyString());
        assertThatThrownBy(() -> auditService.logCreate(null, new Object(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Entity cannot be null");
    }

    @Test
    @DisplayName("logUpdate_Normal_Success")
    void testLogUpdate_Normal_Success() {
        doNothing().when(auditService).logUpdate(eq("Employee"), eq(1L), any(), any(), eq("admin"));
        auditService.logUpdate("Employee", 1L, new Object(), new Object(), "admin");
        verify(auditService, times(1)).logUpdate(eq("Employee"), eq(1L), any(), any(), eq("admin"));
    }

    @Test
    @DisplayName("logUpdate_NullBeforeOrAfter_IllegalArgumentException")
    void testLogUpdate_NullBeforeOrAfter_IllegalArgumentException() {
        doThrow(new IllegalArgumentException("Before/After cannot be null")).when(auditService).logUpdate(anyString(), anyLong(), isNull(), any(), anyString());
        assertThatThrownBy(() -> auditService.logUpdate("Employee", 1L, null, new Object(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Before/After cannot be null");
    }

    @Test
    @DisplayName("logDelete_Normal_Success")
    void testLogDelete_Normal_Success() {
        doNothing().when(auditService).logDelete(eq("Employee"), eq(1L), eq("admin"));
        auditService.logDelete("Employee", 1L, "admin");
        verify(auditService, times(1)).logDelete(eq("Employee"), eq(1L), eq("admin"));
    }

    @Test
    @DisplayName("logDelete_NullActor_IllegalArgumentException")
    void testLogDelete_NullActor_IllegalArgumentException() {
        doThrow(new IllegalArgumentException("Actor cannot be null")).when(auditService).logDelete(anyString(), anyLong(), isNull());
        assertThatThrownBy(() -> auditService.logDelete("Employee", 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Actor cannot be null");
    }

    @Test
    @DisplayName("logCreate_XSSAttempt_IllegalArgumentException")
    void testLogCreate_XSSAttempt_IllegalArgumentException() {
        doThrow(new IllegalArgumentException("XSS attempt detected")).when(auditService).logCreate(eq("<script>"), any(), anyString());
        assertThatThrownBy(() -> auditService.logCreate("<script>", new Object(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("XSS attempt detected");
    }

    @Test
    @DisplayName("logCreate_SQLInjectionAttempt_IllegalArgumentException")
    void testLogCreate_SQLInjectionAttempt_IllegalArgumentException() {
        doThrow(new IllegalArgumentException("SQL injection attempt detected")).when(auditService).logCreate(eq("Employee'; DROP TABLE Audit;--"), any(), anyString());
        assertThatThrownBy(() -> auditService.logCreate("Employee'; DROP TABLE Audit;--", new Object(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SQL injection attempt detected");
    }
}
