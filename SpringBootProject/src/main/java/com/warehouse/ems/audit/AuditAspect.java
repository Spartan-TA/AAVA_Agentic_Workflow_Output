package com.warehouse.ems.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    @Autowired
    private AuditService auditService;

    @Pointcut("@annotation(com.warehouse.ems.audit.Auditable)")
    public void auditableMethods() {}

    @AfterReturning(pointcut = "auditableMethods()", returning = "result")
    public void audit(JoinPoint joinPoint, Object result) {
        String action = joinPoint.getSignature().getName();
        String performedBy = "system"; // Replace with actual user context
        String details = result != null ? result.toString() : "";
        auditService.logAction(action, performedBy, details);
    }
}