package com.warehouseems.safety;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "safety_incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String severity;

    private String location;

    @Column(nullable = false)
    private String status;
}
