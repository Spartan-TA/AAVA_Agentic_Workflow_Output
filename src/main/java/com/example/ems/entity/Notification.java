package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // e.g., EMAIL, SMS, IN_APP
    private String channel;
    private String recipient;
    private String subject;
    @Column(length = 2000)
    private String message;
    private LocalDateTime sentAt;
    private String status; // e.g., SENT, FAILED, PENDING
    private String deliveryStatus;
    private String locale;

    public Notification() {}

    // Getters and setters omitted for brevity
}
