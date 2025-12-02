package com.wms.ems.role;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Role entity.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
