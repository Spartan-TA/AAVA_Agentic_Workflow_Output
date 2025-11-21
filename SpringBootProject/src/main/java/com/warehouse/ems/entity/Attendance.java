package com.warehouse.ems.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "device")
    private String device;

    @Column(name = "geofence_valid")
    private Boolean geofenceValid;

    @Column(name = "hours_worked")
    private BigDecimal hoursWorked;

    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;
}