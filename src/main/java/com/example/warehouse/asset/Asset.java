package com.example.warehouse.asset;

import javax.persistence.*;

@Entity
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private String serialNumber;
    private String status;
    private Long assignedEmployeeId;

    public Asset() {}

    public Asset(String name, String type, String serialNumber, String status, Long assignedEmployeeId) {
        this.name = name;
        this.type = type;
        this.serialNumber = serialNumber;
        this.status = status;
        this.assignedEmployeeId = assignedEmployeeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAssignedEmployeeId() {
        return assignedEmployeeId;
    }

    public void setAssignedEmployeeId(Long assignedEmployeeId) {
        this.assignedEmployeeId = assignedEmployeeId;
    }
}
