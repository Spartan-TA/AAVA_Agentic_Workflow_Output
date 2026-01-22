package com.warehouse.ems.asset;

import com.warehouse.ems.employee.Employee;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing assignment of equipment/assets to employees.
 */
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    @NotNull(message = "Equipment is required.")
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required.")
    private Employee employee;

    @NotNull(message = "Checkout time is required.")
    @Column(nullable = false)
    private LocalDateTime checkoutTime;

    private LocalDateTime returnTime;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required.")
    @Column(nullable = false)
    private Status status;

    public enum Status {
        CHECKED_OUT, RETURNED, OVERDUE
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDateTime getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(LocalDateTime checkoutTime) { this.checkoutTime = checkoutTime; }
    public LocalDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
