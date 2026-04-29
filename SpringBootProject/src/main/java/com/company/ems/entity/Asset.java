package com.company.ems.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Set;

/**
 * Asset entity for equipment and asset assignment.
 */
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "condition_state")
    private String conditionState;

    @OneToMany(mappedBy = "asset")
    private Set<AssetAssignment> assignments;

    // Getters and setters omitted for brevity
}
