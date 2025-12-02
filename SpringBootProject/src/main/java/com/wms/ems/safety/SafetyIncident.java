package com.wms.ems.safety;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    private List<Long> involvedEmployeeIds;

    @Column(nullable = false)
    private String severity;

    private String location;
    private String description;

    @Column(nullable = false)
    private String status; // Open, Investigating, Resolved

    @Column(nullable = false)
    private LocalDate reportedDate;

    // Getters and setters omitted for brevity
}
