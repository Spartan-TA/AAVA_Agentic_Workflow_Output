package com.warehouse.management.employee;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

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

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<Employee> employees;
}
