package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class WebhookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;
    private String payload;
    private LocalDateTime triggeredAt;
    private String status; // e.g., DELIVERED, FAILED
    private String targetUrl;
    private String deliveryMethod; // e.g., HTTP, SFTP
    private String errorMessage;

    public WebhookEvent() {}

    // Getters and setters omitted for brevity
}
