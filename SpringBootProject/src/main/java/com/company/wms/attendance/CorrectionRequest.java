package com.company.wms.attendance;

import com.company.wms.employee.Employee;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing a correction request for an attendance event.
 */
@Entity
@Table(name = "correction_requests")
public class CorrectionRequest {

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
    private LocalDateTime originalTimestamp;

    @NotNull
    private LocalDateTime requestedTimestamp;

    @Column(length = 512)
    private String reason;

    @NotNull
    private Boolean resolved = false;

    private LocalDateTime resolvedAt;

    public CorrectionRequest() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public AttendanceType getType() { return type; }
    public void setType(AttendanceType type) { this.type = type; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

    public LocalDateTime getRequestedTimestamp() { return requestedTimestamp; }
    public void setRequestedTimestamp(LocalDateTime requestedTimestamp) { this.requestedTimestamp = requestedTimestamp; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
