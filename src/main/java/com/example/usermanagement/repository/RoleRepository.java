package com.example.usermanagement.repository;

import com.example.usermanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Role entity.
 * Provides CRUD operations and custom queries for roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Find role by name.
     * @param name Role name
     * @return Role entity
     */
    Role findByName(String name);
}
