package com.warehouse.employeemgmt.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Attendance Entity - Time and attendance tracking
 * 
 * Tracks employee clock-in/clock-out events with device and location capture.
 * Supports geofence validation, hours calculation, and missed punch workflows.
 * 
 * Features:
 * - Clock-in/out timestamp tracking
 * - Device ID capture for audit trail
 * - Location tracking (optional geofence validation)
 * - Status management (PRESENT, ABSENT, LATE, MISSED_PUNCH)
 * - Automatic hours calculation
 * - Correction workflow support
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(length = 200)
    private String location;

    @Column(nullable = false, length = 20)
    private String status; // PRESENT, ABSENT, LATE, MISSED_PUNCH

    @Column(name = "hours_worked")
    private Double hoursWorked;

    @Column(name = "requires_correction")
    private boolean requiresCorrection = false;

    @Column(name = "correction_reason", length = 500)
    private String correctionReason;

    @Column(name = "approved_by")
    private Long approvedBy;

    /**
     * Calculate hours worked between clock-in and clock-out
     */
    public void calculateHours() {
        if (clockIn != null && clockOut != null) {
            Duration duration = Duration.between(clockIn, clockOut);
            this.hoursWorked = duration.toMinutes() / 60.0;
        }
    }
}