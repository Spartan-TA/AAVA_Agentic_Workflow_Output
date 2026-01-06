package com.company.wms.asset.model;

import com.company.wms.employee.model.Employee;
import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing the assignment of an asset to an employee.
 */
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The asset being assigned.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    /**
     * The employee to whom the asset is assigned.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Date the asset was assigned.
     */
    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    /**
     * Date the asset was returned, if applicable.
     */
    @Column(name = "returned_date")
    private LocalDate returnedDate;

    // Constructors, getters, setters, equals, hashCode, toString

    public AssetAssignment() {}

    public AssetAssignment(Asset asset, Employee employee, LocalDate assignedDate, LocalDate returnedDate) {
        this.asset = asset;
        this.employee = employee;
        this.assignedDate = assignedDate;
        this.returnedDate = returnedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetAssignment that = (AssetAssignment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "AssetAssignment{" +
                "id=" + id +
                ", asset=" + (asset != null ? asset.getId() : null) +
                ", employee=" + (employee != null ? employee.getId() : null) +
                ", assignedDate=" + assignedDate +
                ", returnedDate=" + returnedDate +
                '}';
    }
}
