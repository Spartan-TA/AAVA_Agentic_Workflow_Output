package com.warehouse.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Shift entity CRUD operations.
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
}
