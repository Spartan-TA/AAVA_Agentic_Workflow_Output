package com.warehouse.ems.scheduling;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

/**
 * Entity representing a shift template.
 */
@Entity
@Table(name = "shift_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private String description;
}
