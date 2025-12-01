package com.example.ems.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Integration entity.
 */
public class IntegrationDto {
    private Long id;

    @NotBlank(message = "Integration name is required")
    private String name;

    @NotBlank(message = "Type is required")
    private String type;

    private String status;
    private LocalDateTime lastSync;
    private String details;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastSync() { return lastSync; }
    public void setLastSync(LocalDateTime lastSync) { this.lastSync = lastSync; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}