package com.warehouseems.attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for attendance events.
 */
@Entity
@Table(name = "attendance_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceEventType type;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String deviceId;
    private String location;
    private String ipAddress;
    private boolean approved = false;
    private String notes;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.timestamp == null) this.timestamp = LocalDateTime.now();
    }
}
