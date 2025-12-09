package com.warehouse.ems.domain;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Set;

/**
 * Entity representing a shift template for scheduling.
 */
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime start;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime end;

    @Column(name = "recurrence_rule")
    private String recurrenceRule;

    @ManyToMany
    @JoinTable(
        name = "shift_assignments",
        joinColumns = @JoinColumn(name = "shift_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> assignedEmployees;

    // Getters and setters omitted for brevity
}
