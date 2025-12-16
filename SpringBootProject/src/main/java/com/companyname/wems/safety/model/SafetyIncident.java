package com.companyname.wems.safety.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "safety_incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDate incidentDate;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @NotBlank
    @Column(nullable = false)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @NotBlank
    @Column(nullable = false)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "incident_employees", joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "employee_id")
    private Set<Long> involvedEmployees;

    @NotBlank
    @Column(nullable = false)
    private String status; // OPEN, INVESTIGATING, RESOLVED

    private String investigationNotes;
}