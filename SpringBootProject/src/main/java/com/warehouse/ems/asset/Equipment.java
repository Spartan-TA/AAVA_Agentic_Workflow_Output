package com.warehouse.ems.asset;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Entity representing equipment/assets in the warehouse.
 */
@Entity
@Table(name = "equipment", uniqueConstraints = @UniqueConstraint(columnNames = "serialNumber"))
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Equipment type is required.")
    @Column(nullable = false)
    private Type type;

    @NotNull(message = "Serial number is required.")
    @Size(min = 2, max = 64)
    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Condition is required.")
    @Column(nullable = false)
    private Condition condition;

    @NotNull(message = "Last maintenance date is required.")
    @Column(nullable = false)
    private LocalDate lastMaintenanceDate;

    public enum Type {
        SCANNER, FORKLIFT, PPE
    }

    public enum Condition {
        GOOD, FAIR, POOR, OUT_OF_SERVICE
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
}
