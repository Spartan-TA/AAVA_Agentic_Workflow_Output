package com.company.wem.asset.dto;

import com.company.wem.asset.entity.Asset;
import com.company.wem.employee.entity.Employee;

/**
 * Data Transfer Object for Asset.
 */
public class AssetDto {
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private Long assignedToEmployeeId;

    public static AssetDto fromEntity(Asset asset) {
        AssetDto dto = new AssetDto();
        dto.id = asset.getId();
        dto.type = asset.getType();
        dto.serialNumber = asset.getSerialNumber();
        dto.condition = asset.getCondition();
        Employee assigned = asset.getAssignedTo();
        dto.assignedToEmployeeId = assigned != null ? assigned.getId() : null;
        return dto;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public Long getAssignedToEmployeeId() { return assignedToEmployeeId; }
    public void setAssignedToEmployeeId(Long assignedToEmployeeId) { this.assignedToEmployeeId = assignedToEmployeeId; }
}