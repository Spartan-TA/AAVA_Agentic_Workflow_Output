package com.warehouse.ems.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AuditAspect {

    @AfterReturning(pointcut = "execution(* com.warehouse.ems.*.service.*.*(..))", returning = "result")
    public void logAfterService(JoinPoint joinPoint, Object result) {
        // Log method name, arguments, and result for audit trail
        log.info("AUDIT: {} called with args {} - result: {}", joinPoint.getSignature(), joinPoint.getArgs(), result);
    }

    @AfterThrowing(pointcut = "execution(* com.warehouse.ems.*.service.*.*(..))", throwing = "ex")
    public void logAfterException(JoinPoint joinPoint, Throwable ex) {
        log.warn("AUDIT: Exception in {} with args {} - exception: {}", joinPoint.getSignature(), joinPoint.getArgs(), ex.getMessage());
    }
}