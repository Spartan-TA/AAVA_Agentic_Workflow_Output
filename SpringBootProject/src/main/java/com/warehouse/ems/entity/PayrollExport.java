package com.warehouse.ems.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_exports")
public class PayrollExport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate exportDate;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status; // e.g., PENDING, COMPLETED

    @Column(nullable = false)
    private Boolean deleted = false;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDate getExportDate() { return exportDate; }
    public void setExportDate(LocalDate exportDate) { this.exportDate = exportDate; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}
