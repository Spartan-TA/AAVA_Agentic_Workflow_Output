package com.example.ems.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Report entity.
 */
public class ReportDto {
    private Long id;

    @NotBlank(message = "Report name is required")
    private String name;

    @NotBlank(message = "Type is required")
    private String type;

    private String status;
    private LocalDateTime generatedAt;
    private String content;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}