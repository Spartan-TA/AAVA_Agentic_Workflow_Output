package com.warehouse.management.scheduling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.Set;

/**
 * Shift entity for scheduling templates and assignments.
 */
@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shift_days_of_week", joinColumns = @JoinColumn(name = "shift_id"))
    @Column(name = "day_of_week")
    private Set<String> daysOfWeek;

    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
}
