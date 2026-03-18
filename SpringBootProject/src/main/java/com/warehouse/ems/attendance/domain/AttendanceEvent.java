package com.warehouse.ems.attendance.domain;

import com.warehouse.ems.employee.domain.Employee;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * JPA Entity for Attendance Event.
 * Tracks employee clock-in and clock-out events.
 */
@Entity
@Table(name = "attendance_event")
public class AttendanceEvent {

    /** Unique identifier for attendance event */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Employee associated with the attendance event */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull
    private Employee employee;

    /** Clock-in timestamp */
    @Column(name = "clock_in_time", nullable = false)
    @NotNull
    private LocalDateTime clockInTime;

    /** Clock-out timestamp */
    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    /** Attendance status (e.g., PRESENT, ABSENT, LATE) */
    @Column(name = "status", nullable = false)
    @NotNull
    private String status;

    /** Notes or remarks for the attendance event */
    @Column(name = "notes")
    private String notes;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDateTime getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }

    public LocalDateTime getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(LocalDateTime clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
