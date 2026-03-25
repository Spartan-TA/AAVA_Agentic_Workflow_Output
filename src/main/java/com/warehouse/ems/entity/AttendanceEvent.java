package com.warehouse.ems.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private String type; // CLOCK_IN or CLOCK_OUT

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String deviceId;
    private String geoLocation;
}
