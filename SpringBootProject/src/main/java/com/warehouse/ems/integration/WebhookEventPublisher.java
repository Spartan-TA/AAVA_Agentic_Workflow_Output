package com.warehouse.ems.integration;

import org.springframework.stereotype.Component;

@Component
public class WebhookEventPublisher {
    public void publishEvent(String eventType, String payload) {
        // Logic to send webhook event to external systems
    }
}