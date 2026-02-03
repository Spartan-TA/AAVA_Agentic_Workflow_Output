package com.wms.core.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String assetTag;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // FORKLIFT, SCANNER, PPE

    @Column(nullable = false)
    private String status; // AVAILABLE, ASSIGNED, MAINTENANCE, RETIRED

    @Column
    private String condition; // GOOD, FAIR, POOR

    @Column(name = "required_certification")
    private String requiredCertification;
}