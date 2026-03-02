package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Attendance entity for employee time tracking.
 */
@Entity
@Table(name = "attendances", indexes = {
        @Index(name = "idx_attendance_employee", columnList = "employee_id"),
        @Index(name = "idx_attendance_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee for this attendance record.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @NotNull
    private LocalDateTime clockInTime;

    private LocalDateTime clockOutTime;

    /**
     * Calculated field for hours worked.
     */
    @Min(0)
    private Double hoursWorked;

    private Boolean geofenceValidated;

    @Size(max = 255)
    private String deviceInfo;

    /**
     * The shift for this attendance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    @JsonIgnore
    private Shift shift;

    @Size(max = 50)
    private String status;

    @Size(max = 500)
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
