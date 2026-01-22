package com.wms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AttendanceEvent extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private String type; // IN, OUT, BREAK, etc.

    // Getters and setters
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
