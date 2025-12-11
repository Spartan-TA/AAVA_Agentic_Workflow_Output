package com.warehouse.employee.management.attendance.entity;

import com.warehouse.employee.management.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Attendance JPA entity for clock-in/out events.
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

    @Column(nullable = false)
    private LocalDateTime clockInTime;

    private LocalDateTime clockOutTime;

    private String deviceInfo;
    private String geofenceData;
    private boolean missedPunch;
    private boolean correctionPending;
}
