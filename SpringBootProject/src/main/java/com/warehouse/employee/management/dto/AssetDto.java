package com.warehouse.employee.management.dto;

import javax.validation.constraints.*;
import java.time.LocalDate;

public class AssetDto {
    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private Long assignedToId;

    private LocalDate checkoutDate;

    @NotBlank
    private String status;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public LocalDate getCheckoutDate() { return checkoutDate; }
    public void setCheckoutDate(LocalDate checkoutDate) { this.checkoutDate = checkoutDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
