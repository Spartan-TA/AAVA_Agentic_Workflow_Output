package com.warehouse.employee.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "schedule_optimization")
public class ScheduleOptimization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "optimization_date", nullable = false)
    private LocalDate optimizationDate;

    private String details;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDate getOptimizationDate() { return optimizationDate; }
    public void setOptimizationDate(LocalDate optimizationDate) { this.optimizationDate = optimizationDate; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}