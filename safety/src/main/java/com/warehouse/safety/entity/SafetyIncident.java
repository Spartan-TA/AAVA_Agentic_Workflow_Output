package com.warehouse.safety.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
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
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @NotBlank
    private String location;

    @NotBlank
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @ElementCollection
    @CollectionTable(name = "incident_employees", joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "employee_id")
    private Set<Long> involvedEmployees;

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        REPORTED, INVESTIGATING, RESOLVED, CLOSED
    }
}
