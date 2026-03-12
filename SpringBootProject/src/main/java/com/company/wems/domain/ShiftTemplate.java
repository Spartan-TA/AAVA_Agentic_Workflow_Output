package com.company.wems.domain;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 20)
    private String recurrencePattern; // DAILY, WEEKLY, etc.

    @Column(length = 100)
    private String department;

    // Constructors, getters, setters
    public ShiftTemplate() {}
    public ShiftTemplate(String name, LocalTime startTime, LocalTime endTime, String recurrencePattern, String department) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.recurrencePattern = recurrencePattern;
        this.department = department;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getRecurrencePattern() { return recurrencePattern; }
    public void setRecurrencePattern(String recurrencePattern) { this.recurrencePattern = recurrencePattern; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
