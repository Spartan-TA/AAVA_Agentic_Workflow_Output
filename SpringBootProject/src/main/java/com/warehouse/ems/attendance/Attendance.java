package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.scheduling.Shift;
import com.warehouse.ems.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Schema(description = "Attendance entity for employee clock-in/out tracking")
public class Attendance extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Attendance record ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @Schema(description = "Employee associated with this attendance record")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    @Schema(description = "Shift associated with this attendance record")
    private Shift shift;

    @NotNull
    @Schema(description = "Clock-in timestamp")
    private LocalDateTime clockIn;

    @Schema(description = "Clock-out timestamp")
    private LocalDateTime clockOut;

    @Schema(description = "Device ID used for clock-in/out")
    private String deviceId;

    @Schema(description = "Geolocation at clock-in/out (lat,long)")
    private String geolocation;

    @Schema(description = "Total hours worked for this attendance record")
    private Double hoursWorked;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Shift getShift() { return shift; }
    public void setShift(Shift shift) { this.shift = shift; }

    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }

    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getGeolocation() { return geolocation; }
    public void setGeolocation(String geolocation) { this.geolocation = geolocation; }

    public Double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
}
