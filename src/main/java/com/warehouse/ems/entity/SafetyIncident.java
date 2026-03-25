package com.warehouse.ems.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private String description;

    private String location;
    private String severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private Employee reportedBy;

    @Column(nullable = false)
    private String status; // OPEN, INVESTIGATING, RESOLVED
}
