package com.company.warehouse.scheduling.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalTime;
import java.time.DayOfWeek;

/**
 * Entity representing a shift template.
 */
@Entity
@Table(name = "shift_templates", indexes = {
        @Index(name = "idx_shift_template_name", columnList = "name")
})
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DayOfWeek dayOfWeek;

    @CreatedDate
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @LastModifiedDate
    private java.time.LocalDateTime updatedAt;

    // Getters and setters omitted for brevity
    // ...
}
