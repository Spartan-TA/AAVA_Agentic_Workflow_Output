package com.warehouse.ems.asset;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for AssetAssignment with validation annotations.
 */
public class AssetAssignmentDto {
    private Long id;

    @NotNull(message = "Equipment ID is required.")
    private Long equipmentId;

    @NotNull(message = "Employee ID is required.")
    private Long employeeId;

    @NotNull(message = "Checkout time is required.")
    private LocalDateTime checkoutTime;

    private LocalDateTime returnTime;

    @NotNull(message = "Status is required.")
    private String status;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDateTime getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(LocalDateTime checkoutTime) { this.checkoutTime = checkoutTime; }
    public LocalDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
