package com.wms.scheduling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

/**
 * Entity representing a shift template.
 */
@Entity
@Table(name = "shift_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String recurrence; // e.g., DAILY, WEEKLY

    @Column(name = "overtime_rule")
    private String overtimeRule;
}
