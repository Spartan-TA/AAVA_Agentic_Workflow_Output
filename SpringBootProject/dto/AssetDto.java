package com.example.ems.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Data Transfer Object for Asset entity.
 */
public class AssetDto {
    private Long id;

    @NotBlank(message = "Asset name is required")
    private String name;

    @NotBlank(message = "Asset type is required")
    private String type;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    private String status;
    private String assignedTo;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}