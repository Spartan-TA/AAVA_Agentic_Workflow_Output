package com.warehouse.employee.management.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class NotificationService {
    private final Map<String, Integer> rateLimitMap = new HashMap<>();
    private final int MAX_NOTIFICATIONS_PER_HOUR = 100;

    @Transactional
    public boolean sendEmail(String to, String subject, String body) {
        if (!canSend(to)) return false;
        // Stub for email delivery logic
        incrementRate(to);
        return true;
    }

    @Transactional
    public boolean sendSms(String to, String message) {
        if (!canSend(to)) return false;
        // Stub for SMS delivery logic
        incrementRate(to);
        return true;
    }

    public String getTemplate(String templateName) {
        // Stub for template retrieval
        return "Template: " + templateName;
    }

    private boolean canSend(String to) {
        return rateLimitMap.getOrDefault(to, 0) < MAX_NOTIFICATIONS_PER_HOUR;
    }

    private void incrementRate(String to) {
        rateLimitMap.put(to, rateLimitMap.getOrDefault(to, 0) + 1);
    }
}
