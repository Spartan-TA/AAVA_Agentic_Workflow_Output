package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(length = 2000)
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String locale;
    private String status; // e.g., ACTIVE, EXPIRED

    public Announcement() {}

    // Getters and setters omitted for brevity
}
