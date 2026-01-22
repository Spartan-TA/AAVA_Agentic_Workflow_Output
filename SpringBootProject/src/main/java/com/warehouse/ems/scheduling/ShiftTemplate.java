package com.warehouse.ems.scheduling;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * Entity representing a shift template for scheduling.
 */
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Shift name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Shift start time is required")
    @Column(nullable = false)
    private LocalTime start;

    @NotNull(message = "Shift end time is required")
    @Column(nullable = false)
    private LocalTime end;

    @NotNull(message = "Recurring flag is required")
    @Column(nullable = false)
    private Boolean recurring;

    // Getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public LocalTime getStart() {
        return start;
    }
    public void setStart(LocalTime start) {
        this.start = start;
    }
    public LocalTime getEnd() {
        return end;
    }
    public void setEnd(LocalTime end) {
        this.end = end;
    }
    public Boolean getRecurring() {
        return recurring;
    }
    public void setRecurring(Boolean recurring) {
        this.recurring = recurring;
    }
}
