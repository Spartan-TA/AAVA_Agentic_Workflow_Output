package com.warehouse.audit.interceptor;

import com.warehouse.audit.entity.AuditLog;
import com.warehouse.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class AuditInterceptor implements HandlerInterceptor {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String username = request.getRemoteUser() != null ? request.getRemoteUser() : "anonymous";
        AuditLog log = AuditLog.builder()
                .action(request.getMethod())
                .entity(request.getRequestURI())
                .entityId(0L)
                .username(username)
                .timestamp(LocalDateTime.now())
                .details("Request: " + request.getRequestURI())
                .build();
        auditLogRepository.save(log);
        return true;
    }
}
