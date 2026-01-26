package com.example.warehouse.scheduling.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employee_shifts")
public class EmployeeShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Long shiftId;

    @Column(nullable = false)
    private LocalDate shiftDate;

    // Constructors
    public EmployeeShift() {}

    public EmployeeShift(Long employeeId, Long shiftId, LocalDate shiftDate) {
        this.employeeId = employeeId;
        this.shiftId = shiftId;
        this.shiftDate = shiftDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }
}
