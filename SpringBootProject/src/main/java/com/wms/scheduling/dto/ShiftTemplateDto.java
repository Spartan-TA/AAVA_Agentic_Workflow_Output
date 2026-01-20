package com.wms.scheduling.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalTime;

/**
 * Data Transfer Object for ShiftTemplate entity.
 * Used for REST API requests and responses.
 */
public class ShiftTemplateDto implements Serializable {

    private Long id;

    @NotBlank(message = "Shift name is required")
    private String name;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    // Constructors
    public ShiftTemplateDto() {}

    public ShiftTemplateDto(Long id, String name, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Optionally, add mapping from Entity
    public ShiftTemplateDto(com.wms.scheduling.domain.ShiftTemplate template) {
        this.id = template.getId();
        this.name = template.getName();
        this.startTime = template.getStartTime();
        this.endTime = template.getEndTime();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
