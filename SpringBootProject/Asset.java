package com.example.warehousemanagement.entity;

import javax.persistence.*;

/**
 * Entity representing an Asset in the warehouse.
 */
@Entity
@Table(name = "assets")
public class Asset {
    public enum AssetType {
        SCANNER, FORKLIFT, PPE
    }

    public enum Condition {
        NEW, GOOD, FAIR, POOR, RETIRED
    }

    public enum Status {
        AVAILABLE, IN_USE, MAINTENANCE, RETIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;

    @Column(name = "serial_number", unique = true, nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_assignee_id")
    private Employee currentAssignee;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AssetType getType() { return type; }
    public void setType(AssetType type) { this.type = type; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Employee getCurrentAssignee() { return currentAssignee; }
    public void setCurrentAssignee(Employee currentAssignee) { this.currentAssignee = currentAssignee; }
}
