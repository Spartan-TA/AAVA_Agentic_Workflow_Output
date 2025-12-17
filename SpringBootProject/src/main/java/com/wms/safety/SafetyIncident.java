package com.wms.safety;

import javax.persistence.*;
import java.time.LocalDateTime;

import com.wms.employee.Employee;

/**
 * Entity representing a safety incident or near-miss.
 */
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String severity; // MINOR, MAJOR, CRITICAL

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee involvedEmployee;

    @Column(nullable = false)
    private LocalDateTime incidentTime;

    @Column(nullable = false)
    private String status; // OPEN, INVESTIGATING, RESOLVED

    private String correctiveActions;

    // Getters and setters omitted for brevity
    // ...
}
