package com.company.wms.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Aspect for auditing sensitive operations across the application.
 * Logs all create, update, and delete operations with actor and timestamp.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
@Aspect
@Component
@Slf4j
public class AuditAspect {

    /**
     * Pointcut for all service layer methods
     */
    @Pointcut("execution(* com.company.wms..service.*.*(..))")
    public void serviceMethods() {}

    /**
     * Pointcut for create operations
     */
    @Pointcut("execution(* com.company.wms..service.*.create*(..))")
    public void createOperations() {}

    /**
     * Pointcut for update operations
     */
    @Pointcut("execution(* com.company.wms..service.*.update*(..))")
    public void updateOperations() {}

    /**
     * Pointcut for delete operations
     */
    @Pointcut("execution(* com.company.wms..service.*.delete*(..))")
    public void deleteOperations() {}

    /**
     * Log all service method calls
     */
    @Before("serviceMethods()")
    public void logServiceMethodCall(JoinPoint joinPoint) {
        String actor = getCurrentUser();
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        
        log.debug("[AUDIT] User: {} | Method: {} | Args: {}", 
                 actor, method, Arrays.toString(args));
    }

    /**
     * Log create operations with result
     */
    @AfterReturning(pointcut = "createOperations()", returning = "result")
    public void logCreateOperation(JoinPoint joinPoint, Object result) {
        String actor = getCurrentUser();
        String method = joinPoint.getSignature().toShortString();
        
        log.info("[AUDIT-CREATE] User: {} | Method: {} | Timestamp: {} | Result: {}",
                actor, method, LocalDateTime.now(), result);
    }

    /**
     * Log update operations with result
     */
    @AfterReturning(pointcut = "updateOperations()", returning = "result")
    public void logUpdateOperation(JoinPoint joinPoint, Object result) {
        String actor = getCurrentUser();
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        
        log.info("[AUDIT-UPDATE] User: {} | Method: {} | Timestamp: {} | Args: {} | Result: {}",
                actor, method, LocalDateTime.now(), Arrays.toString(args), result);
    }

    /**
     * Log delete operations
     */
    @AfterReturning(pointcut = "deleteOperations()")
    public void logDeleteOperation(JoinPoint joinPoint) {
        String actor = getCurrentUser();
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        
        log.info("[AUDIT-DELETE] User: {} | Method: {} | Timestamp: {} | Args: {}",
                actor, method, LocalDateTime.now(), Arrays.toString(args));
    }

    /**
     * Log exceptions in service methods
     */
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "exception")
    public void logServiceException(JoinPoint joinPoint, Throwable exception) {
        String actor = getCurrentUser();
        String method = joinPoint.getSignature().toShortString();
        
        log.error("[AUDIT-ERROR] User: {} | Method: {} | Timestamp: {} | Exception: {}",
                 actor, method, LocalDateTime.now(), exception.getMessage());
    }

    /**
     * Get current authenticated user
     */
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}