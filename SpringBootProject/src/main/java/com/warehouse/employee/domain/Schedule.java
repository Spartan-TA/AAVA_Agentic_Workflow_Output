package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Schedule entity for employee shift assignments.
 */
@Entity
@Table(name = "schedules", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_schedule_date", columnNames = {"employee_id", "scheduleDate"})
}, indexes = {
        @Index(name = "idx_schedule_employee", columnList = "employee_id"),
        @Index(name = "idx_schedule_shift", columnList = "shift_id"),
        @Index(name = "idx_schedule_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee assigned to this schedule.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    /**
     * The shift assigned to this schedule.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    @JsonIgnore
    private Shift shift;

    @NotNull
    private LocalDate scheduleDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @Size(max = 500)
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Schedule status.
     */
    public enum Status {
        SCHEDULED, COMPLETED, CANCELLED
    }
}
