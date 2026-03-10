package com.example.warehouse.scheduling;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private int durationMinutes;

    public ShiftTemplate() {}

    public ShiftTemplate(String name, LocalTime startTime, LocalTime endTime, int durationMinutes) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
    }

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

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
