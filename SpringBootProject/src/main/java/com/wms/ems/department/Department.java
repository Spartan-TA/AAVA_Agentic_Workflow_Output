package com.wms.ems.department;

import jakarta.persistence.*;

/**
 * Department entity for employee assignment.
 */
@Entity
@Table(name = "department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    // Getters and setters omitted for brevity
}
