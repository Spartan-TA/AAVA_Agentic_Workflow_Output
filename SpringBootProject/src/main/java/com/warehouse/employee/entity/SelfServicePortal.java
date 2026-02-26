package com.warehouse.employee.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "self_service_portal")
public class SelfServicePortal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "access_date", nullable = false)
    private LocalDate accessDate;

    private String action;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDate getAccessDate() { return accessDate; }
    public void setAccessDate(LocalDate accessDate) { this.accessDate = accessDate; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}