package com.example.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @AfterReturning(pointcut = "execution(* com.example..*Service.*(..))", returning = "result")
    public void logServiceMethods(JoinPoint joinPoint, Object result) {
        AuditLog log = new AuditLog();
        log.setAction(joinPoint.getSignature().getName());
        log.setUsername("system"); // Replace with actual user context
        log.setTimestamp(LocalDateTime.now());
        log.setDetails("Method executed: " + joinPoint.getSignature());
        auditLogRepository.save(log);
    }
}