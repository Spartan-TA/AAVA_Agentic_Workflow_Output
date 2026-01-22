package com.wms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AuditLog extends BaseEntity {
    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private String details;

    // Getters and setters
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
