package com.wms.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Certification extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    // Getters and setters
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}
