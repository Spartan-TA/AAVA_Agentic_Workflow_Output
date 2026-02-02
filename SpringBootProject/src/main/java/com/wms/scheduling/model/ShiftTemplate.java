package com.wms.scheduling.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

/**
 * Entity representing a shift template (e.g., Morning, Evening, Night)
 */
@Entity
@Table(name = "shift_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the shift (e.g., Morning, Night)
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Start time of the shift
     */
    @Column(nullable = false)
    private LocalTime startTime;

    /**
     * End time of the shift
     */
    @Column(nullable = false)
    private LocalTime endTime;

    /**
     * Is this shift active?
     */
    @Column(nullable = false)
    private boolean active;
}
