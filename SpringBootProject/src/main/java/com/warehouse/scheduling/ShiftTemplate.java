package com.warehouse.scheduling;

import com.warehouse.employee.Warehouse;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * ShiftTemplate entity for recurring shift definitions.
 */
@Entity
@Table(name = "shift_template")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "recurrence_rule")
    private String recurrenceRule;

    @Column(name = "overtime_rule")
    private String overtimeRule;

    @Column(name = "blackout_dates")
    private String blackoutDates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @OneToMany(mappedBy = "shiftTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ShiftSchedule> shiftSchedules;

    // Getters and setters omitted for brevity
}
