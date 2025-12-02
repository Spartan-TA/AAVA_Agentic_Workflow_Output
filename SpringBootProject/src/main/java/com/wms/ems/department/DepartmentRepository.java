package com.wms.ems.department;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Department entity.
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByName(String name);
}
