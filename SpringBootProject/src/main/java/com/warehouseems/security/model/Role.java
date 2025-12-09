package com.warehouseems.security.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Set;

/**
 * Role entity for RBAC.
 */
@Entity
@Table(name = "roles", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, unique = true)
    private String name; // ADMIN, HR, SUPERVISOR, WORKER

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }
}
