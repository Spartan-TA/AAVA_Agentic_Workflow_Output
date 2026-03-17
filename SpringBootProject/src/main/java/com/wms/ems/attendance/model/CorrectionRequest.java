package com.wms.ems.attendance.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "correction_requests")
public class CorrectionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long attendanceEventId;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        PENDING,
        APPROVED,
        DENIED
    }

    public CorrectionRequest() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAttendanceEventId() { return attendanceEventId; }
    public void setAttendanceEventId(Long attendanceEventId) { this.attendanceEventId = attendanceEventId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}