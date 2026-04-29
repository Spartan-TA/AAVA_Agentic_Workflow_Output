package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SchedulingSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String suggestionType; // e.g., SHIFT_SWAP, OVERTIME
    private String description;
    private LocalDateTime createdAt;
    private String status; // e.g., PENDING, ACCEPTED, REJECTED
    private String actor;

    public SchedulingSuggestion() {}

    // Getters and setters omitted for brevity
}
