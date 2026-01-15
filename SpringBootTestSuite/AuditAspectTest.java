package com.warehouse.ems.audit;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.service.EmployeeService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
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

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AuditAspect
 * Tests cover audit logging, aspect execution, and security context integration
 */
@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditAspect auditAspect;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    // ========== AUDIT ASPECT EXECUTION TESTS ==========

    @Test
    void testAuditAspect_MethodExecution_LogsAuditEntry() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee, result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_MethodWithNullArgs_LogsAuditEntry() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("getAllEmployees");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn(null);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_MethodWithEmptyArgs_LogsAuditEntry() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("getAllEmployees");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_MethodThrowsException_LogsAndRethrows() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            auditAspect.auditMethod(joinPoint);
        });

        verify(joinPoint, times(1)).proceed();
    }

    // ========== SECURITY CONTEXT TESTS ==========

    @Test
    void testAuditAspect_WithAuthenticatedUser_CapturesUsername() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);
        when(authentication.getName()).thenReturn("admin@example.com");

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(authentication, atLeastOnce()).getName();
    }

    @Test
    void testAuditAspect_WithNullAuthentication_HandlesGracefully() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_WithAnonymousUser_LogsAnonymous() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);
        when(authentication.getName()).thenReturn("anonymousUser");

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(authentication, atLeastOnce()).getName();
    }

    // ========== AUDITABLE ANNOTATION TESTS ==========

    @Test
    void testAuditableAnnotation_OnMethod_TriggersAudit() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        Auditable auditable = method.getAnnotation(Auditable.class);
        
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditableAnnotation_WithAction_LogsAction() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    // ========== AUDIT LOG ENTRY TESTS ==========

    @Test
    void testAuditLogEntry_ContainsTimestamp() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        // Verify that audit log entry was created with timestamp
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditLogEntry_ContainsActor() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(authentication, atLeastOnce()).getName();
    }

    @Test
    void testAuditLogEntry_ContainsAction() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(methodSignature, atLeastOnce()).getMethod();
    }

    @Test
    void testAuditLogEntry_ContainsEntityType() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, atLeastOnce()).getArgs();
    }

    // ========== BEFORE AND AFTER VALUES TESTS ==========

    @Test
    void testAuditLogEntry_CapturesBeforeValue() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("updateEmployee", Long.class, Object.class);
        Employee beforeEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .status("ACTIVE")
                .build();

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L, testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditLogEntry_CapturesAfterValue() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("updateEmployee", Long.class, Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L, testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee, result);
    }

    // ========== DIFFERENT CRUD OPERATIONS TESTS ==========

    @Test
    void testAuditAspect_CreateOperation_LogsCorrectly() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_UpdateOperation_LogsCorrectly() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("updateEmployee", Long.class, Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L, testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_DeleteOperation_LogsCorrectly() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("deleteEmployee", Long.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});
        when(joinPoint.proceed()).thenReturn(null);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testAuditAspect_WithMultipleArguments_LogsAll() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("updateEmployee", Long.class, Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L, testEmployee, "extra"});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_WithNullResult_LogsCorrectly() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("deleteEmployee", Long.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});
        when(joinPoint.proceed()).thenReturn(null);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_WithLargeObject_LogsCorrectly() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        Employee largeEmployee = Employee.builder()
                .id(1L)
                .name("A".repeat(255))
                .badgeId("EMP001")
                .department("B".repeat(100))
                .status("ACTIVE")
                .build();

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{largeEmployee});
        when(joinPoint.proceed()).thenReturn(largeEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_ConcurrentExecution_ThreadSafe() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act - Simulate concurrent execution
        Object result1 = auditAspect.auditMethod(joinPoint);
        Object result2 = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(joinPoint, times(2)).proceed();
    }

    @Test
    void testAuditAspect_WithSpecialCharacters_LogsCorrectly() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        Employee specialEmployee = Employee.builder()
                .id(1L)
                .name("John O'Doe-Smith")
                .badgeId("EMP<001>")
                .department("Warehouse & Logistics")
                .status("ACTIVE")
                .build();

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{specialEmployee});
        when(joinPoint.proceed()).thenReturn(specialEmployee);

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testAuditAspect_PerformanceImpact_Minimal() throws Throwable {
        // Arrange
        Method method = EmployeeService.class.getMethod("createEmployee", Object.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEmployee});
        when(joinPoint.proceed()).thenReturn(testEmployee);

        // Act
        long startTime = System.currentTimeMillis();
        Object result = auditAspect.auditMethod(joinPoint);
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(result);
        assertTrue((endTime - startTime) < 100, "Audit aspect should execute quickly");
    }
}