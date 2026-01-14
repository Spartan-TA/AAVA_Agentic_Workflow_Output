package com.warehouse.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

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

    @NotNull
    @Column(name = "clock_in_time", nullable = false)
    private LocalDateTime clockInTime;

    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "geofence_location")
    private String geofenceLocation;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;

    @Column(name = "correction_approved")
    private Boolean correctionApproved = false;
}
