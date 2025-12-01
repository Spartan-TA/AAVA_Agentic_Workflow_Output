package com.wms.ems.scheduling.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ShiftTemplate entity representing a reusable shift pattern.
 */
@Entity
@Table(name = "shift_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_minutes")
    private Integer breakMinutes;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "shiftTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftAssignment> shiftAssignments;
}