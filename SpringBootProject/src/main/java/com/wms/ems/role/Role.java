package com.wms.ems.role;

import jakarta.persistence.*;

/**
 * Role entity for RBAC and employee assignment.
 */
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    // Getters and setters omitted for brevity
}
