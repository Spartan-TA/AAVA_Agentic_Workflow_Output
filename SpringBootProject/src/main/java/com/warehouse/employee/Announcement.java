package com.warehouse.employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Announcement entity for dashboard announcements.
 */
@Entity
@Table(name = "announcement")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "visible_from", nullable = false)
    private LocalDateTime visibleFrom;

    @Column(name = "visible_to")
    private LocalDateTime visibleTo;

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Getters and setters omitted for brevity
}
