package com.warehouse.ems.asset;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO for Equipment with validation annotations.
 */
public class EquipmentDto {
    private Long id;

    @NotNull(message = "Type is required.")
    private String type;

    @NotNull(message = "Serial number is required.")
    @Size(min = 2, max = 64, message = "Serial number must be between 2 and 64 characters.")
    private String serialNumber;

    @NotNull(message = "Condition is required.")
    private String condition;

    @NotNull(message = "Last maintenance date is required.")
    private LocalDate lastMaintenanceDate;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
}
