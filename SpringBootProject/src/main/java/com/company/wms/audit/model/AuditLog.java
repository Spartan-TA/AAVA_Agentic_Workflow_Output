package com.company.wms.audit.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an audit log entry for tracking system events and changes.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who performed the action.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * The action performed (e.g., CREATE, UPDATE, DELETE).
     */
    @Column(nullable = false)
    private String action;

    /**
     * The entity affected by the action.
     */
    @Column(name = "entity_name", nullable = false)
    private String entityName;

    /**
     * The ID of the entity affected.
     */
    @Column(name = "entity_id")
    private String entityId;

    /**
     * Additional details about the action.
     */
    @Column(length = 2000)
    private String details;

    /**
     * Timestamp of when the action occurred.
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Constructors, getters, setters, equals, hashCode, toString

    public AuditLog() {}

    public AuditLog(Long userId, String action, String entityName, String entityId, String details, LocalDateTime timestamp) {
        this.userId = userId;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.details = details;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return id != null && id.equals(auditLog.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", action='" + action + ''' +
                ", entityName='" + entityName + ''' +
                ", entityId='" + entityId + ''' +
                ", details='" + details + ''' +
                ", timestamp=" + timestamp +
                '}';
    }
}
