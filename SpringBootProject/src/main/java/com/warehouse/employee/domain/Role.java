package com.warehouse.employee.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity for RBAC.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // ADMIN, HR, SUPERVISOR, WORKER

    private String description;
}
