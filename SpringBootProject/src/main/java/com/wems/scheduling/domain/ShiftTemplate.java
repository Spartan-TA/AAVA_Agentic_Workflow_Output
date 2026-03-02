package com.wems.scheduling.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shift_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrence;
    
    private Integer overtimeThresholdHours;
    private Double overtimeMultiplier;
    private String description;
    private boolean active = true;
    
    @ElementCollection
    @CollectionTable(name = "shift_template_days")
    @Column(name = "day_of_week")
    private Set<Integer> daysOfWeek = new HashSet<>();
}
