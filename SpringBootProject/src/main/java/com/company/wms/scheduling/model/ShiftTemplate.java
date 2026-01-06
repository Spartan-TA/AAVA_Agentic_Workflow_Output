package com.company.wms.scheduling.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

/**
 * Entity representing a shift template for scheduling.
 */
@Entity
@Table(name = "shift_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Boolean active;
}
