package com.warehouse.ems.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @AfterReturning(pointcut = "@annotation(auditable)", argNames = "joinPoint,auditable")
    public void auditAction(JoinPoint joinPoint, Auditable auditable) {
        // In production, log to immutable audit table
        log.info("AUDIT: Action={}, Method={}, Args={}", auditable.action(), joinPoint.getSignature(), joinPoint.getArgs());
    }
}