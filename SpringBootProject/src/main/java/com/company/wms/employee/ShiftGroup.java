package com.company.wms.employee;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * ShiftGroup entity.
 */
@Entity
@Table(name = "shift_groups")
public class ShiftGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters and setters omitted for brevity
}
