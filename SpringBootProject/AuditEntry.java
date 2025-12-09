package com.example.warehousemanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an immutable audit log entry.
 */
@Entity
@Table(name = "audit_entries")
public class AuditEntry {
    public enum ActionType {
        CREATE, UPDATE, DELETE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actor;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    @Lob
    @Column(name = "before_value")
    private String beforeValue;

    @Lob
    @Column(name = "after_value")
    private String afterValue;

    // No setters for immutable fields except via constructor
    public AuditEntry() {}

    public AuditEntry(String actor, LocalDateTime timestamp, String entityType, String entityId, ActionType action, String beforeValue, String afterValue) {
        this.actor = actor;
        this.timestamp = timestamp;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }

    public Long getId() { return id; }
    public String getActor() { return actor; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public ActionType getAction() { return action; }
    public String getBeforeValue() { return beforeValue; }
    public String getAfterValue() { return afterValue; }
}
