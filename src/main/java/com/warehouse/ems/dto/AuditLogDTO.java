package com.warehouse.ems.dto;

import java.time.LocalDateTime;

public class AuditLogDTO {
    private Long id;
    private String entity;
    private Long entityId;
    private String action;
    private Long actor;
    private LocalDateTime timestamp;
    private String beforeState;
    private String afterState;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getActor() { return actor; }
    public void setActor(Long actor) { this.actor = actor; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getBeforeState() { return beforeState; }
    public void setBeforeState(String beforeState) { this.beforeState = beforeState; }
    public String getAfterState() { return afterState; }
    public void setAfterState(String afterState) { this.afterState = afterState; }
}
