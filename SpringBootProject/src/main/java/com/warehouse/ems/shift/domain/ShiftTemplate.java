package com.warehouse.ems.shift.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalTime;

/**
 * JPA Entity for Shift Template.
 * Defines reusable shift patterns for scheduling.
 */
@Entity
@Table(name = "shift_template")
public class ShiftTemplate {

    /** Unique identifier for shift template */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the shift template */
    @Column(name = "name", nullable = false)
    @NotNull
    @Size(min = 2, max = 50)
    private String name;

    /** Start time of the shift */
    @Column(name = "start_time", nullable = false)
    @NotNull
    private LocalTime startTime;

    /** End time of the shift */
    @Column(name = "end_time", nullable = false)
    @NotNull
    private LocalTime endTime;

    /** Description of the shift template */
    @Column(name = "description")
    @Size(max = 255)
    private String description;

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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
