package com.warehouse.employee.management.employee.repository;

import com.warehouse.employee.management.employee.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {
    // Custom query methods if needed
}
