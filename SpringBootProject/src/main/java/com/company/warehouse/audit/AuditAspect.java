package com.company.warehouse.audit;

import com.company.warehouse.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for audit logging using AOP.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @AfterReturning(pointcut = "execution(* com.company.warehouse.employee.service.EmployeeService.*(..))", returning = "result")
    public void logEmployeeActions(JoinPoint joinPoint, Object result) {
        // Example: log actor, entity, action, before, after
        String actor = "system"; // Replace with actual user context
        String entity = "Employee";
        String action = joinPoint.getSignature().getName();
        String before = ""; // Could serialize previous state
        String after = result != null ? result.toString() : "";
        auditService.log(actor, entity, action, before, after);
    }
}