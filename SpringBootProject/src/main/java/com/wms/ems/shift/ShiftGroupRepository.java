package com.wms.ems.shift;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for ShiftGroup entity.
 */
public interface ShiftGroupRepository extends JpaRepository<ShiftGroup, Long> {
    ShiftGroup findByName(String name);
}
