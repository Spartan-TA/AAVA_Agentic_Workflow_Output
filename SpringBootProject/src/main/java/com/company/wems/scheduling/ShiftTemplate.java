package com.company.wems.scheduling;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import javax.persistence.*;
import java.time.LocalTime;
import java.util.Set;

@Data
@RequiredArgsConstructor
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

    @Column
    private String recurrenceRule; // e.g., "FREQ=WEEKLY;BYDAY=MO,WE,FR"

    @ElementCollection
    @CollectionTable(name = "shift_required_skills", joinColumns = @JoinColumn(name = "shift_template_id"))
    @Column(name = "skill")
    private Set<String> requiredSkills;

    @Column(nullable = false)
    private Integer minEmployees;

    @Column(nullable = false)
    private Integer maxEmployees;
}
