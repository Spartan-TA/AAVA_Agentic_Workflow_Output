package com.warehouseems.employee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse employee master data.
 * Supports soft-delete and unique badgeId enforcement.
 */
@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Size(max = 20)
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String role;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String department;

    @Size(max = 50)
    private String shiftGroup;

    @PastOrPresent
    @Column(nullable = false)
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false)
    private String status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getShiftGroup() { return shiftGroup; }
    public void setShiftGroup(String shiftGroup) { this.shiftGroup = shiftGroup; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
