package com.warehouse.management.repository;

import com.warehouse.management.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    // Additional query methods can be defined here
}
