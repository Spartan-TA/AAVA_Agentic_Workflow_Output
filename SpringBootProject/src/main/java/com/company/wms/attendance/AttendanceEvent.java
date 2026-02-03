package com.company.wms.attendance;

import com.company.wms.employee.Employee;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event (clock-in/clock-out) for an employee.
 */
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AttendanceType type;

    @NotNull
    private LocalDateTime timestamp;

    @Column(length = 64)
    private String deviceId;

    @Embedded
    private GeoLocation geoLocation;

    @NotNull
    private Boolean correctionRequested = false;

    public AttendanceEvent() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public AttendanceType getType() { return type; }
    public void setType(AttendanceType type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public GeoLocation getGeoLocation() { return geoLocation; }
    public void setGeoLocation(GeoLocation geoLocation) { this.geoLocation = geoLocation; }

    public Boolean getCorrectionRequested() { return correctionRequested; }
    public void setCorrectionRequested(Boolean correctionRequested) { this.correctionRequested = correctionRequested; }
}
