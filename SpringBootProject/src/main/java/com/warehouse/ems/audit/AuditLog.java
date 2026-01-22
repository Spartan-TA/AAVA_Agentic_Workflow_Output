package com.warehouse.ems.audit;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an audit log entry for compliance.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Actor is required.")
    @Column(nullable = false)
    private String actor;

    @NotNull(message = "Timestamp is required.")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotNull(message = "Entity is required.")
    @Column(nullable = false)
    private String entity;

    @NotNull(message = "Entity ID is required.")
    @Column(nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Action is required.")
    @Column(nullable = false)
    private Action action;

    @Lob
    private String before;

    @Lob
    private String after;

    public enum Action {
        CREATE, UPDATE, DELETE
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public String getBefore() { return before; }
    public void setBefore(String before) { this.before = before; }
    public String getAfter() { return after; }
    public void setAfter(String after) { this.after = after; }
}
