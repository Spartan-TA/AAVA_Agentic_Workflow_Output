package com.company.wms.employee.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Entity representing a warehouse employee.
 */
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String badgeId;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String role;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String department;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String shiftGroup;

    @PastOrPresent
    @Column(nullable = false)
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
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

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id != null && id.equals(employee.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + ''' +
                ", badgeId='" + badgeId + ''' +
                ", role='" + role + ''' +
                ", department='" + department + ''' +
                ", shiftGroup='" + shiftGroup + ''' +
                ", hireDate=" + hireDate +
                ", status='" + status + ''' +
                ", deleted=" + deleted +
                '}';
    }
}
