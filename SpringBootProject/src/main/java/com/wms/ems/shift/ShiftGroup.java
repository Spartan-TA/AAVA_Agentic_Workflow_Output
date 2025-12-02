package com.wms.ems.shift;

import jakarta.persistence.*;

/**
 * ShiftGroup entity for employee shift assignment.
 */
@Entity
@Table(name = "shift_group")
public class ShiftGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    // Getters and setters omitted for brevity
}
