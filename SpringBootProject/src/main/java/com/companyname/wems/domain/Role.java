package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

/**
 * Role entity for role-based access control.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // ADMIN, HR, SUPERVISOR, WORKER

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}
