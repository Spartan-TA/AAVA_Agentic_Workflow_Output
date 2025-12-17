package com.wms.scheduling;

import javax.persistence.*;
import java.time.LocalTime;

/**
 * Entity representing a recurring shift template.
 */
@Entity
@Table(name = "shift_templates")
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
    private String rotationPattern; // e.g., "Mon-Fri", "2-2-3"

    private Boolean overtimeAllowed;
    private Boolean blackoutDate;

    // Getters and setters omitted for brevity
    // ...
}
